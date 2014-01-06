package iristk.audio;

import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;

public class AudioTargetMonitor implements AudioTarget {

	private AudioTarget target;
	private ArrayList<AudioTargetListener> targets = new ArrayList<AudioTargetListener>();

	public AudioTargetMonitor(AudioTarget target) {
		this.target = target;
	}
	
	@Override
	public void start() {
		this.target.start();
		for (AudioTargetListener target : targets) {
			target.start();
		}
	}

	@Override
	public void stop() {
		this.target.stop();
		for (AudioTargetListener target : targets) {
			target.stop();
		}
	}

	@Override
	public int write(byte[] buffer, int pos, int len) {
		len = this.target.write(buffer, pos, len);
		for (AudioTargetListener target : targets) {
			target.write(buffer, pos, len);
		}
		return len;
	}
	
	@Override
	public AudioFormat getAudioFormat() {
		return target.getAudioFormat();
	}

	@Override
	public void close() {
		this.target.close();
		for (AudioTargetListener target : targets) {
			target.close();
		}
	}

	public void addTarget(AudioTarget target, boolean padSilence) {
		targets.add(new AudioTargetListener(target, padSilence));
	}

	@Override
	public void flush() {
		this.target.flush();
		for (AudioTargetListener target : targets) {
			target.flush();
		}
	}

	private class AudioTargetListener implements Runnable {
		
		private boolean running = false;
		private final boolean padSilence;
		private final AudioTarget target;
		private boolean inSound = false;
		private long recordingPos = 0;
		private Thread thread;

		public AudioTargetListener(AudioTarget target, boolean padSilence) {
			this.target = target;
			this.padSilence = padSilence;
			if (padSilence) {
				target.start();
				thread = new Thread(this);
				thread.start();
			}
		}

		public void flush() {
			target.flush();
		}

		@Override
		public void run() {
			try {
				running = true;
				long startTime = System.currentTimeMillis();
				int frameSize = (int) ((getAudioFormat().getFrameSize() * getAudioFormat().getFrameRate()) / 100);
				byte[] silence = new byte[320];
				Arrays.fill(silence, (byte)0);
				while (running) {
					long timePos = (System.currentTimeMillis() - startTime) / 10;
					int framePos = (int) (recordingPos / frameSize);
					while (!inSound && framePos < timePos) {
						write(silence, 0, silence.length);
						framePos = (int) (recordingPos / frameSize);
					}
					Thread.sleep(10);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void close() {
			if (thread != null) {
				running = false;
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			target.close();
		}

		public void start() {
			inSound = true;
			if (!padSilence) {
				target.start();
			}
		}

		public void stop() {
			inSound = false;
			if (!padSilence) {
				target.stop();
			}
		}

		public int write(byte[] buffer, int pos, int len) {
			recordingPos += len;
			return target.write(buffer, pos, len);
		}

	}
	
}
