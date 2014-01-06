package iristk.speech.util;

import iristk.audio.AudioTarget;
import iristk.audio.AudioUtil;

import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;

public class EnergyVAD {
	
	public static final int WINSIZE = 21;
	public static final int THRESH_UPDATE_INTERVAL = 100;
	public static final double ADAPT_RATE = 0.2;
	
	public static final int SPEECH = 1;
	public static final int SILENCE = 0;
	
	public static final int DEFAULT_DELTA_SIL = 5;
	public static final int DEFAULT_DELTA_SPEECH = 20;

	public int deltaSil = DEFAULT_DELTA_SIL;
	public int deltaSpeech = DEFAULT_DELTA_SPEECH;
	
	private int[] stateWindow = new int[WINSIZE];
	{
		Arrays.fill(stateWindow, SILENCE);
	}
	private int stateWindowPos = 0;
	private int threshUpdateCount = THRESH_UPDATE_INTERVAL;
	
	private double prevSample = 0.0;
	private double noiseLevel = 20.0;
	
	private boolean calibrating = false;
	
	private long streamPos;
	
	int[] histogram = new int[100];
	{
		Arrays.fill(histogram, 0);
	}

	private ArrayList<VADListener> vadListeners = new ArrayList<VADListener>();
	
	private int state = SILENCE;
	//private long silFrames = endSil;
	private final AudioFormat audioFormat;
	private double[] inputSamples = null;
	private double[] frameBuffer = new double[2048];
	private int frameBufferPos = 0;
	private final float sampleRate;
	private final int sampleSize;
		
	public EnergyVAD(AudioFormat audioFormat) {
		this.audioFormat = audioFormat;
		this.sampleRate = audioFormat.getSampleRate();
		this.sampleSize = audioFormat.getSampleSizeInBits() / 8;
	}
	
	public void addVADListener(VADListener vadListener) {
		this.vadListeners.add(vadListener);
	}
	
	public void setNoiseLevel(double noiseLevel) {
		this.noiseLevel = noiseLevel;
	}
	
	public void setDeltaSil(int deltaSil) {
		this.deltaSil = deltaSil;
	}
	
	public void setDeltaSpeech(int deltaSpeech) {
		this.deltaSpeech = deltaSpeech;
	}
	
    private int power(double[] samples, int pos, int length) {
        double sumOfSquares = 0.0f;
        for (int i = 0; i < length; i++) {
            double sample = samples[i + pos] - prevSample;
            sumOfSquares += (sample * sample);
            prevSample = samples[i + pos];
        }
        double power = (10.0 * (Math.log10(sumOfSquares) - Math.log10(length))) + 0.5;
        if (power < 0) power = 1.0;
        return (int) power;
    }
    
    private void updateThresholds(int power) {
       histogram[power] += 1;
    	
	   threshUpdateCount--;
	   if (calibrating) {  // threshUpdateCount == 0 || 
		   double lastNoiseLevel = noiseLevel;
			
		   int max = 0;
		   for (int i = 0; i < histogram.length; i++) {
			   if (histogram[i] > histogram[max]) {
				   max = i;
			   }
		   }
		   
		   noiseLevel = noiseLevel + ADAPT_RATE * (max - noiseLevel) + 0.5;
		   
		   if (calibrating && Math.abs(noiseLevel - lastNoiseLevel) < 0.1) {
			   //System.out.println("Stopped calibrating");
			   calibrating = false;
		   }
		   threshUpdateCount = THRESH_UPDATE_INTERVAL;
		   
		   //for (int i = 0; i < histogram.length; i++) {
		//	   histogram[i] -= histogram[i] >> 3;
		 //  }
		   
		   //System.out.println("Noise level: " + noiseLevel);
	   }
	  
    }
    
    public void processSamples(double[] samples, boolean scale) {
    	System.arraycopy(samples, 0, frameBuffer, frameBufferPos, samples.length);
    	frameBufferPos += samples.length;
    	double[] frame = new double[160];
    	
    	while (frameBufferPos >= 160) {    	
    		System.arraycopy(frameBuffer, 0, frame, 0, frame.length);
    		System.arraycopy(frameBuffer, 160, frameBuffer, 0, frameBufferPos - 160);
    		frameBufferPos -= 160;
	    	
	    	if (scale)
	    		AudioUtil.scaleDoubles(frame, Short.MAX_VALUE);
	    	processFrame(frame);
    	}
    	
    }
    
	public void processSamples(byte[] samples, int pos, int length) {
		if (inputSamples  == null || inputSamples.length != length / sampleSize) {
			inputSamples = new double[length / sampleSize];
		}
		AudioUtil.bytesToDoubles(audioFormat, samples, pos, length, inputSamples, 0);
		processSamples(inputSamples, true);
	}
    
    private void processFrame(double[] frame) {
    	streamPos += 160;
    	    	
    	int power = power(frame, 0, 160);
    	
	   int newState;
	   if (state == SPEECH) {
		   if (power <= (noiseLevel + deltaSil)) {
			   newState = SILENCE;
		   } else {
			   newState = SPEECH;
		   }
	   } else {
		   if (power > (noiseLevel + deltaSpeech)) {
			   newState = SPEECH;
		   } else {
			   newState = SILENCE;
		   }			   
	   }
	   
	   //updateThresholds(power);
	   stateWindow[stateWindowPos] = newState;
	   stateWindowPos++;
	   if (stateWindowPos >= WINSIZE)
		   stateWindowPos = 0;
	   
	   int[] stateCount = new int[2];
	   stateCount[SPEECH] = 0;
	   stateCount[SILENCE] = 0;
	   for (int i = 0; i < WINSIZE; i++) {
		   stateCount[stateWindow[i]] ++;
	   }
	   //System.out.println(stateCount[SPEECH] + " " + stateCount[SILENCE]);
	   if (stateCount[SPEECH] > stateCount[SILENCE]) {
		   state  = SPEECH;
		   //silFrames  = 0;
	   } else {
		   state = SILENCE;
		   //silFrames++;
	   }
	   
	   for (VADListener listener : vadListeners) {
		   listener.vadEvent(streamPos, state == SPEECH, power);
	   }
    	
    }
    
    public boolean isSpeech() {
    	//return (!calibrating && (state == SPEECH || silFrames < endSil));
    	return (!calibrating && (state == SPEECH));
    }
	
    public AudioTarget getAudioTarget() {
    	return new AudioTarget() {

			@Override
			public void start() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void flush() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public int write(byte[] buffer, int pos, int len) {
				processSamples(buffer, pos, len);
				return len;
			}
			
			@Override
			public AudioFormat getAudioFormat() {
				return audioFormat;
			}

			@Override
			public void close() {
			}
    		
    	};
    }
    
}
