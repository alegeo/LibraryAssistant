package iristk.audio;

import javax.sound.sampled.AudioFormat;

public interface AudioSource {

	public abstract void start();
	
	public abstract void stop();
	
	public abstract int read(byte[] buffer, int pos, int len);
	
	public abstract AudioFormat getAudioFormat();
	
	public abstract void close();

	/**
	 * @return The audio source position in samples
	 */
	public abstract int getPosition();
	
}
