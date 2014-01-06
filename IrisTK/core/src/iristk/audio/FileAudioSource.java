package iristk.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.*;

public class FileAudioSource implements AudioSource {

	private Sound sound;
	private int soundPos = 0;

	public FileAudioSource(File file) throws UnsupportedAudioFileException, IOException {
		sound = new Sound(file);
	}
	
	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public int read(byte[] buffer, int pos, int len) {
		if (soundPos + len >= sound.getData().length) {
			len = sound.getData().length - soundPos;
		}
		if (len > 0) {
			System.arraycopy(sound.getData(), soundPos, buffer, pos, len);
			soundPos += len;
		}
		return len;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return sound.getAudioFormat();
	}

	@Override
	public void close() {
	}

	@Override
	public int getPosition() {
		return soundPos / getAudioFormat().getFrameSize();
	}
	
}
