package iristk.audio;

import javax.sound.sampled.AudioFormat;

public interface AudioTarget {

	void start();
	
	void stop();

	void flush();
	
	int write(byte[] buffer, int pos, int len);
	
	AudioFormat getAudioFormat();

	void close();
	
}
