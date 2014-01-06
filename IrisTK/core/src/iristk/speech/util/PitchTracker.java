package iristk.speech.util;

import iristk.audio.AudioTarget;
import iristk.audio.AudioUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFormat;

public class PitchTracker {

	private final double threshold = 0.15;
	private final double globalThreshold = 0.5;
	private final double energyThreshold = 100;
	private final double minPitchHz = 50;
	private final double maxPitchHz = 500;
	private final int frameRate = 100;
	
	private int minTau;
	private int maxTau;
	
	private final int bufferSize;
	private final int bufferStepSize;
	private final int overlapSize;
	private final double sampleRate;
	
	private long samplesProcessed = 0;
	
	/**
	 * The original input buffer
	 */
	private final double[] inputBuffer;

	/**
	 * The buffer that stores the calculated values.
	 * It is exactly half the size of the input buffer.
	 */
	private double[] yinBuffer;
	private PitchSmoother pitchBuffer;
	private final PitchHandler pitchHandler;
	private PitchNormalizer pitchNormalizer = new PitchNormalizer();

	// TODO: this should of course not be hardcoded
	private PitchNormData pitchNormData = PitchNormData.gabriel;
	private AudioFormat audioFormat;
	
	private double[] inputSamples = null;
	private final int sampleSize;
	
	public PitchTracker(PitchHandler pitchHandler, AudioFormat audioFormat) {
		this.audioFormat = audioFormat;
		this.sampleRate = audioFormat.getSampleRate();
		this.sampleSize = audioFormat.getSampleSizeInBits() / 8;
		this.pitchHandler = pitchHandler;
		minTau = (int) (sampleRate / maxPitchHz);
		maxTau = (int) (sampleRate / minPitchHz);
		bufferSize = maxTau * 2;
		bufferStepSize = (int) (sampleRate / frameRate);
		overlapSize = (bufferSize - bufferStepSize); 
		inputBuffer = new double[bufferSize];
		reset();
	}
		
	public void setNormalization(PitchNormData pitchNormData) {
		this.pitchNormData = pitchNormData;
	}

	private double signalPower() {
        double sumOfSquares = 0.0f;
        for (int i = 0; i < inputBuffer.length; i++) {
            double sample = inputBuffer[i];
            sumOfSquares += sample * sample;
        }
        return  Math.sqrt(sumOfSquares/inputBuffer.length);
    }
	
	/**
	 * Implements the difference function as described
	 * in step 2 of the YIN paper
	 */
	private void difference(){
		int j,tau;
		double delta;
		for(tau=0;tau < yinBuffer.length;tau++){
			yinBuffer[tau] = 0;
		}
		for(tau = 1 ; tau < yinBuffer.length ; tau++){
			for(j = 0 ; j < yinBuffer.length ; j++){
				delta = inputBuffer[j] - inputBuffer[j+tau];
				yinBuffer[tau] += delta * delta;
			}
		}
	}
	

	/**
	 * The cumulative mean normalized difference function
	 * as described in step 3 of the YIN paper
	 * <br><code>
	 * yinBuffer[0] == yinBuffer[1] = 1
	 * </code>
	 *
	 */
	private void cumulativeMeanNormalizedDifference(){
		int tau;
		yinBuffer[0] = 1;
		//Very small optimization in comparison with AUBIO
		//start the running sum with the correct value:
		//the first value of the yinBuffer
		double runningSum = yinBuffer[1];
		//yinBuffer[1] is always 1
		yinBuffer[1] = 1;
		//now start at tau = 2
		for(tau = 2 ; tau < yinBuffer.length ; tau++){
			runningSum += yinBuffer[tau];
			yinBuffer[tau] *= tau / runningSum;
		}
	}

	/**
	 * Implements step 4 of the YIN paper
	 */
	private int absoluteThreshold(double[] yinBuffer, int min, int max){
		//Uses another loop construct
		//than the AUBIO implementation
		for(int tau = min;tau<max;tau++){
			if(yinBuffer[tau] < threshold){
				while(tau+1 < yinBuffer.length &&
						yinBuffer[tau+1] < yinBuffer[tau])
					tau++;
				return tau;
			}
		}
		double bestConf = 1.0f;
		Integer best = -1;
		for(int tau = min; tau < max; tau++){
			if (yinBuffer[tau] < bestConf && yinBuffer[tau] < globalThreshold) {
				best = tau;
				bestConf = yinBuffer[tau];
			}
		}
		return best;
	}

	/**
	 * Implements step 5 of the YIN paper. It refines the estimated tau value
	 * using parabolic interpolation. This is needed to detect higher
	 * frequencies more precisely.
	 *
	 * @param tauEstimate
	 *            the estimated tau value.
	 * @return a better, more precise tau value.
	 */
	private double parabolicInterpolation(int tauEstimate, double[] yinBuffer) {
		double s0, s1, s2;
		int x0 = (tauEstimate < 1) ? tauEstimate : tauEstimate - 1;
		int x2 = (tauEstimate + 1 < yinBuffer.length) ? tauEstimate + 1 : tauEstimate;
		if (x0 == tauEstimate)
			return (yinBuffer[tauEstimate] <= yinBuffer[x2]) ? tauEstimate : x2;
		if (x2 == tauEstimate)
			return (yinBuffer[tauEstimate] <= yinBuffer[x0]) ? tauEstimate : x0;
		s0 = yinBuffer[x0];
		s1 = yinBuffer[tauEstimate];
		s2 = yinBuffer[x2];
		//fixed AUBIO implementation, thanks to Karl Helgason:
		//(2.0f * s1 - s2 - s0) was incorrectly multiplied with -1
		return tauEstimate + 0.5f * (s2 - s0 ) / (2.0f * s1 - s2 - s0);
	}

	/**
	 * The main flow of the YIN algorithm. Returns a pitch value in Hz or -1 if
	 * no pitch is detected using the current values of the input buffer.
	 *
	 * @return a pitch value in Hz or -1 if no pitch is detected.
	 */
	private double getPitch(double signalPower) {

		if (signalPower < energyThreshold) {
			return -1;
		}
		
		int tauEstimate = -1;
		double pitchInHertz = -1;

		//step 2
		difference();

		//step 3
		cumulativeMeanNormalizedDifference();

		//step 4
		tauEstimate = absoluteThreshold(yinBuffer, minTau, maxTau);

		//step 5
		if(tauEstimate != -1){
			 double betterTau = parabolicInterpolation(tauEstimate, yinBuffer);

			//step 6
			//TODO Implement optimization for the YIN algorithm.
			//0.77% => 0.5% error rate,
			//using the data of the YIN paper
			//bestLocalEstimate()

			//conversion to Hz
			pitchInHertz = sampleRate/betterTau;
		}

		return pitchInHertz;
	}

	public double getConf(int tau) {
		return yinBuffer[tau];
	}

	public void processSamples(byte[] samples) {
		this.processSamples(samples, 0, samples.length);
	}
	
	public void processSamples(byte[] samples, int pos, int length) {
		if (inputSamples == null || inputSamples.length != length / sampleSize) {
			inputSamples = new double[length / sampleSize];
		}
		AudioUtil.bytesToDoubles(audioFormat, samples, pos, length, inputSamples, 0);
		processSamples(inputSamples);
	}
	
	public void processSamples(double[] samples) {
		processSamples(samples, 0, samples.length);
	}

	// Samples are assumed to be from -1.0 to 1.0
	public void processSamples(double[] samples, int pos, int length) {	
		int samplesPos = 0;
		
		while (samplesPos < length) {
			int samplesLeft = length - samplesPos;
			int samplesToFill = bufferStepSize;
			if (samplesProcessed % bufferStepSize != 0) {
				samplesToFill = (int) (bufferStepSize - (samplesProcessed % bufferStepSize));
			} 
			if (samplesToFill > samplesLeft) {
				samplesToFill = samplesLeft;
			}
			System.arraycopy(inputBuffer, samplesToFill, inputBuffer, 0, bufferSize - samplesToFill);
			System.arraycopy(samples, pos + samplesPos, inputBuffer, bufferSize - samplesToFill, samplesToFill);
			AudioUtil.scaleDoubles(inputBuffer, bufferSize - samplesToFill, samplesToFill, 32768);
			samplesPos += samplesToFill;
			samplesProcessed += samplesToFill;
			
			if (samplesProcessed % bufferStepSize == 0) {
				
				yinBuffer = new double[maxTau];
				double signalPower = signalPower();
				double pitch = getPitch(signalPower);
				double conf = -1;
				if (pitch > -1) {
					conf = getConf((int) (sampleRate / pitch));
				}
		
				pitchBuffer.add(new PitchData(pitch, signalPower, conf, (samplesProcessed - 480) / sampleRate, yinBuffer));
				
				PitchData smoothed = pitchBuffer.getSmoothed();
				if (smoothed != null) {
					smoothed = pitchNormalizer.add(smoothed);
					pitchHandler.handleDetectedPitch(smoothed);
				}
			}
		}
	}

	public void reset() {
		pitchBuffer = new PitchSmoother();
		pitchNormalizer.reset();
		samplesProcessed = 0;
		Arrays.fill(inputBuffer, 0.0f);
	}

	public static double mean(List<PitchData> data) {
		double mean = 0;
		for (PitchData pd : data) {
			mean += pd.pitchCent / data.size();
		}
		return mean;
	}
	
	public static double stdev(List<PitchData> data, double mean) {
		double variance = 0;
		for (PitchData pd : data) {
			variance += Math.pow((pd.pitchCent - mean), 2) / data.size();
		}
		return Math.sqrt(variance);
	}
	
	private class PitchNormalizer {
		
		private static final int minBufferSize = 10;
		private static final int maxBufferSize = 200;
		
		private ArrayList<PitchData> buffer = new ArrayList<PitchData>();
						
		public PitchNormalizer() {
		}
		
		public PitchData add(PitchData pd) {
			if (pd.conf > -1) {
				
				PitchNormData pnd = pitchNormData;
				
				if (pnd == null) {
					buffer.add(pd);
					
					if (buffer.size() >= minBufferSize) {
						if (buffer.size() > maxBufferSize) 
							buffer.remove(0);
											
						double mean = mean(buffer);
						pnd = new PitchNormData(mean, stdev(buffer, mean));
		 			}
				}
				
				double pitchZ = 0;
				
				if (pnd != null) {
					double margin = 1.0f;
					if (pitchNormData != null)
						margin = 2f;
					
					if (Math.abs(pd.pitchCent - pnd.getMeanPitch()) > (pnd.getStdevPitch() * margin)) {
						return new PitchData(-1, pd.signalPower, -1, pd.time);
					}
					
					pitchZ = ((pd.pitchCent - pnd.getMeanPitch()) / pnd.getStdevPitch());
				}
				
				return new PitchData(pd.pitchHz, pd.signalPower, pd.conf, pd.time, pitchZ);
			} else {
				return new PitchData(pd.pitchHz, pd.signalPower, pd.conf, pd.time);
			}
		}
		
		public void reset() {
			buffer.clear();
		}
		
	}
	
	private class PitchSmoother {

		private static final int bufferSize = 5;
		
		private ArrayList<PitchData> buffer = new ArrayList<PitchData>();
		
		public void add(PitchData pitchData) {
			buffer.add(pitchData);
			if (buffer.size() > bufferSize) {
				buffer.remove(0);
			}
		}

		public PitchData getSmoothed() {
			int minSize = (bufferSize + 1) / 2;
			if (buffer.size() < minSize)
				return null;
			if (buffer.size() < bufferSize) 
				return null; //buffer.get(buffer.size() - minSize);
			int midI = (bufferSize - 1) / 2;
			PitchData midPD = buffer.get(midI);
			PitchData result = midPD;
			double bestConf = 1.0f;
			int bestI = -1;
			for (int i = 0; i < bufferSize; i++) {
				PitchData pd = buffer.get(i);
				if (pd.conf > -1 && pd.conf < bestConf) {
					bestI = i;
					bestConf = pd.conf;
				}
			}
			if (bestI > -1 && bestI != midI) {
				PitchData best = buffer.get(bestI);
				int min = Math.max(minTau, (int) (0.8 * (sampleRate / best.pitchHz)));
				int max = Math.min(maxTau, (int) (1.2 * (sampleRate / best.pitchHz)));
				int tau = absoluteThreshold(midPD.yinBuffer, min, max);
				if (tau == -1) {
					//System.out.println("Muting value");
					result = new PitchData(-1, midPD.signalPower, -1, midPD.time, midPD.yinBuffer);
				} else {
					//double betterTau = parabolicInterpolation(tau, midData.yinBuffer);
					double pitch = sampleRate / tau;
					double conf = midPD.yinBuffer[tau];
					result = new PitchData(pitch, midPD.signalPower, conf, midPD.time, midPD.yinBuffer);
				}
			}
			/*
			if (result.conf > -1) {
				CHECK_LONER: {
					for (int i = 0; i < bufferSize; i++) {
						if (i == midI)
							continue;
						PitchData pd = buffer.get(i);
						if (pd.conf > -1 && Math.abs(pd.pitch - result.pitch) < 20) {
							//System.out.println("Saved by " + i + " " + pd.pitch);
							break CHECK_LONER;
						}
					}
					result = new PitchData(-1, -1, midPD.pos, midPD.yinBuffer);
					//System.out.println("Removing value");
				}
			}
			*/
			return result;
		}

	}

	public AudioTarget getAudioTarget() {
		return new AudioTarget() {
			@Override
			public void start() {
			}

			@Override
			public void stop() {
			}

			@Override
			public void flush() {
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
