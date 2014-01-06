package iristk.audio;

import iristk.util.BlockingByteQueue;

import java.io.File;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;

public class AudioSourceReader implements Runnable {

	private AudioSource audioSource;
	private Thread runningThread = null;
	boolean running = false;
	private ArrayList<AudioTarget> targets = new ArrayList<AudioTarget>();
	private ArrayList<AudioListener> listeners = new ArrayList<AudioListener>();
	
	private int bufferSize;

	public AudioSourceReader(AudioSource audioSource) {
		this.audioSource = audioSource;
		// Set the buffer to 10 ms
		this.bufferSize = (int) ((audioSource.getAudioFormat().getSampleRate() * audioSource.getAudioFormat().getFrameSize()) / 100);
	}
	
	public AudioSourceReader(AudioSource audioSource, int bufferSize) {
		this.audioSource = audioSource;
		this.bufferSize = bufferSize;
	}

	public void runAsync() {
		runningThread = new Thread(this);
		runningThread.start();
	}
	
	@Override
	public void run() {
		run(-1);
	}
	
	public void run(float seconds) {
		run((int)(seconds * audioSource.getAudioFormat().getSampleRate() * audioSource.getAudioFormat().getChannels()));
	}
	
	public void run(final int samples) {
		if (running)
			throw new IllegalStateException("AudioSourceReader already running");
		running = true;
		byte[] buffer = new byte[bufferSize];
		byte[] cBuffer = new byte[bufferSize];
		for (AudioTarget target : targets) {
			target.start();
		}
		audioSource.start();
		long pos = 0;
		int clen, cpos;
		int nChannels = audioSource.getAudioFormat().getChannels();
		while (running) {
			int len = audioSource.read(buffer, 0, buffer.length);
			if (len > 0) {
				for (AudioTarget target : targets) {
					target.write(buffer, 0, len);
				}
				for (AudioListener listener : listeners) {
					if (listener.listening) {
						if (listener.channel == -1) {
							listener.byteQueue.write(buffer, 0, len);
						} else {
							clen = len / nChannels;
							cpos = 0;
							for (int i = 0; i < len; i += nChannels * 2) {
								cBuffer[cpos] = buffer[i+listener.channel*2];
								cBuffer[cpos+1] = buffer[i+listener.channel*2+1];
								cpos += 2;
							}
							listener.byteQueue.write(cBuffer, 0, clen);
						}
					}
				}
			}
			if (len < buffer.length)
				running = false;
			pos += len / audioSource.getAudioFormat().getFrameSize();
			if (samples > -1 && pos >= samples) 
				running = false;
		}
		audioSource.stop();
		for (AudioTarget target : targets) {
			target.stop();
		}
	}

	public void stop() {
		running = false;
		if (runningThread != null) {
			try {
				runningThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			runningThread = null;
		}
	}

	public boolean isRunning() {
		return running;
	}

	public AudioSource getAudioSource() {
		return audioSource;
	}

	public AudioSource newAudioListener() {
		AudioListener listener = new AudioListener();
		listeners.add(listener);
		return listener;
	}

	public AudioSource newAudioListener(int channel) {
		AudioListener listener = new AudioListener(channel);
		listeners.add(listener);
		return listener;
	}

	public void addAudioTarget(AudioTarget audioTarget) {
		targets.add(audioTarget);
	}
	
	private class AudioListener implements AudioSource {

		private BlockingByteQueue byteQueue = new BlockingByteQueue();
		private boolean listening = false;
		private AudioFormat audioFormat; 
		private final int channel;
		private int sourcePos = 0;

		public AudioListener(int channel) {
			this.audioFormat = new AudioFormat(
					audioSource.getAudioFormat().getEncoding(),
					audioSource.getAudioFormat().getSampleRate(),
					audioSource.getAudioFormat().getSampleSizeInBits(),
					1,
					audioSource.getAudioFormat().getFrameSize(),
					audioSource.getAudioFormat().getFrameRate(),
					audioSource.getAudioFormat().isBigEndian());
			this.channel = channel;
		}

		public AudioListener() {
			this.audioFormat = audioSource.getAudioFormat();
			this.channel = -1;
		}

		@Override
		public void start() {
			byteQueue.reset();
			listening = true;
		}

		@Override
		public void stop() {
			listening = false;
		}

		@Override
		public int read(byte[] buffer, int pos, int len) {
			try {
				sourcePos += len / audioFormat.getFrameSize();
				return byteQueue.read(buffer, pos, len);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return -1;
			}
		}

		@Override
		public AudioFormat getAudioFormat() {
			return this.audioFormat;
		}

		@Override
		public void close() {
		}

		@Override
		public int getPosition() {
			return sourcePos;
		}
		
	}
	

	public static void main(String[] args) throws Exception {
		//AudioSource source = new ASIOAudioSource(1, 48000);
		AudioSource source = new AudioSourceConverter(new PortAudioSource(8000, 2), 16000);
		
		//FileAudioSource source = new FileAudioSource(new File("Cereproc/prerec/chess_dialogue/mhm_ack_06.wav"));
	
		AudioSourceReader reader = new AudioSourceReader(source);
		
		FileAudioTarget target = new FileAudioTarget(source.getAudioFormat());
		//PortAudioTarget target = new PortAudioTarget(source.getAudioFormat());
		target.startRecording(new File("d:/test.wav"));
		reader.addAudioTarget(target);
		reader.run(2.0f);
		target.stopRecording();
		source.close();
	}


}
