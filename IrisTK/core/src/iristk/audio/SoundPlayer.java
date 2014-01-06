package iristk.audio;

import javax.sound.sampled.AudioFormat;

public class SoundPlayer {
	
	private boolean playing;
	
	private Thread playThread;
	private AudioTarget audioTarget;
	private AudioFormat audioFormat;

	public SoundPlayer(AudioFormat format) {
		this(new PortAudioTarget(format));
	}
	
	public SoundPlayer(AudioTarget audioTarget) {
		this.audioTarget = audioTarget;
		this.audioFormat = audioTarget.getAudioFormat();
	}
	
	public void stop() {
		playing = false;
		if (isPlaying()) {
			try {
				playThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int play(Sound sound, int msecPos, int msecLength) {
		if (msecPos < 0)
			msecPos = 0;
		playing = true;
		audioTarget.start();
		int startPos = (int) (msecPos * (audioFormat.getSampleRate() / 1000) * audioFormat.getFrameSize());
		int endPos;
		if (msecLength < 0)
			endPos = sound.getData().length;
		else
			endPos = startPos + (int) (msecLength
					* (audioFormat.getSampleRate() / 1000) * audioFormat.getFrameSize());
		int pos = startPos;
		int frameSize = 320;
		int len = frameSize;
		PLAYING: {
			for (; pos < endPos; pos += frameSize) {
				if (endPos - pos < frameSize)
					len = endPos - pos;
				audioTarget.write(sound.getData(), pos, len);
				if (!playing)
					break PLAYING;
			}
			audioTarget.flush();
		}
		audioTarget.stop();
		playing = false;
		return pos;
	}
	
	public boolean isPlaying() {
		return (playThread != null && playThread.isAlive());
	}
	
	public int play(Sound sound, int msecPos) {
		return play(sound, msecPos, -1);
	}

	public int play(Sound sound) {
		return play(sound, 0);
	}
	
	public void playAsync(Sound sound) {
		playAsync(sound, 0, null);
	}

	public void playAsync(Sound sound, final CallbackDelegate callback) {
		playAsync(sound, 0, callback);
	}

	public void playAsync(Sound sound, final int msecPos) {
		playAsync(sound, msecPos, null);
	}

	public void playAsync(final Sound sound, final int msecPos, final CallbackDelegate callback) {
		playAsync(sound, msecPos, -1, callback);
	}
	
	public void playAsync(final Sound sound, final int msecPos, final int length, final CallbackDelegate callback) {
		stop();
		playThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int endPos = play(sound, msecPos, length);
				if (callback != null)
					callback.callback(endPos);
			}
		});
		playThread.setName("SoundPlayer");
		playThread.start();
	}
	
	public void waitForPlayingDone() {
		try {
			playThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public interface CallbackDelegate {
		public void callback(int endPos);
	}

	public void playAsync(Sound sound, int start, int length) {
		// TODO Auto-generated method stub
		
	}

}
