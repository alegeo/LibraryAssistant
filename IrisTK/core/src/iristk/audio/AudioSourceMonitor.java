package iristk.audio;

import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;

public class AudioSourceMonitor implements AudioSource {

	private AudioSource source;
	private ArrayList<AudioTarget> targets = new ArrayList<AudioTarget>();

	public AudioSourceMonitor(AudioSource source) {
		this.source = source;
	}
	
	@Override
	public void start() {
		source.start();
		for (AudioTarget target : targets) {
			target.start();
		}
	}

	@Override
	public void stop() {
		source.stop();
		for (AudioTarget target : targets) {
			target.stop();
		}
	}

	@Override
	public int read(byte[] buffer, int pos, int len) {
		len = source.read(buffer, pos, len);
		for (AudioTarget target : targets) {
			target.write(buffer, pos, len);
		}
		return len;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return source.getAudioFormat();
	}

	@Override
	public void close() {
		source.close();
		for (AudioTarget target : targets) {
			target.close();
		}
	}

	public void addTarget(AudioTarget target) {
		targets.add(target);
	}

	@Override
	public int getPosition() {
		return source.getPosition();
	}

}
