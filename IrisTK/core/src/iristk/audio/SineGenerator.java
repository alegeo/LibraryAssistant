package iristk.audio;

import javax.sound.sampled.AudioFormat;

public class SineGenerator implements AudioSource {
	
	private AudioFormat format;
	double phase = 0.0;
	double phaseIncrement = 0.01;

	public SineGenerator(AudioFormat format, double freq) {
		this.format = format;
		setFreq(freq);
	}
	
	public SineGenerator(double freq) {
		this(new AudioFormat(16000, 16, 1, true, false), freq);
	}
	
	public void setFreq(double freq) {
		phaseIncrement = freq * Math.PI * 2.0 / format.getSampleRate();
	}
	
	double next() {
		double value = Math.sin(phase);
		phase += phaseIncrement;
		if (phase > Math.PI) {
			phase -= Math.PI * 2.0;
		}
		return value;
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public int read(byte[] buffer, int pos, int len) {
		int byteSize = format.getSampleSizeInBits() / 8;
		int doubles = len / byteSize;
		double[] dBuffer = new double[doubles]; 
		for (int i = 0; i < doubles; i += 1) {
			dBuffer[i] = next();
		}
		AudioUtil.doublesToBytes(format, dBuffer, 0, doubles, buffer, pos);
		return len;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return format;
	}

	@Override
	public void close() {
	}

	@Override
	public int getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}
}
