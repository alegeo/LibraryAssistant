package iristk.audio;

import javax.sound.sampled.AudioFormat;

public class ByteAudioSource implements AudioSource {

	private byte[] sound;
	private int soundPos = 0;
	private AudioFormat audioFormat;

	public ByteAudioSource(byte[] data, AudioFormat audioFormat) {
		this.sound = data;
		this.audioFormat = audioFormat;
	}
	
	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public int read(byte[] buffer, int pos, int len) {
		if (soundPos + len >= sound.length) {
			len = sound.length - soundPos;
		}
		if (len > 0) {
			System.arraycopy(sound, soundPos, buffer, pos, len);
			soundPos += len;
		}
		return len;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	@Override
	public void close() {
	}

	@Override
	public int getPosition() {
		return soundPos / getAudioFormat().getFrameSize();
	}

}
