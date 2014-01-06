package iristk.audio;

import javax.sound.sampled.*;

public class JSoundAudioSource implements AudioSource {

	TargetDataLine line;
	private AudioFormat format;
	
	public JSoundAudioSource() {
		this(8000, 1);
	}

	public JSoundAudioSource(int sampleRate, int nChannels) {
		this(new AudioFormat(sampleRate, 16, nChannels, true, false));
	}
	
	public JSoundAudioSource(AudioFormat format) {
		this.format = format;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); 
		if (!AudioSystem.isLineSupported(info)) {
			throw new RuntimeException("Could not find audio device");
		}
		try {
		    line = (TargetDataLine) AudioSystem.getLine(info);
		    line.open(format, 1600);
		} catch (LineUnavailableException ex) {
			throw new RuntimeException("Could not open audio device");
		}
	}

	@Override
	public void start() {
		line.start();
	}
	
	@Override
	public void stop() {
		line.stop();
	}

	@Override
	public int read(byte[] buffer, int pos, int len) {
		line.read(buffer, pos, len);
		return len;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return format;
	}

	@Override
	public void close() {
		line.close();
	}

	@Override
	public int getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

}
