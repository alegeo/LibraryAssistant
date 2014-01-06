package iristk.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat.Encoding;

public class JSoundAudioTarget implements AudioTarget {

	private AudioFormat format;
	private SourceDataLine outputLine;

	public JSoundAudioTarget(AudioFormat format) {
		this.format = format;
		System.out.println(format);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format); 
		if (!AudioSystem.isLineSupported(info)) {
			throw new RuntimeException("Could not find audio device");
		}
		try {
			outputLine = (SourceDataLine) AudioSystem.getLine(info);
			outputLine.open(format, 1600);
		} catch (LineUnavailableException ex) {
			throw new RuntimeException("Could not open audio device");
		}
	}
	
	public JSoundAudioTarget(int sampleRate, int channelCount) {
		this(new AudioFormat(Encoding.PCM_SIGNED, sampleRate, 16, channelCount, 2, sampleRate, false));
	}

	@Override
	public void start() {
		outputLine.start();
	}

	@Override
	public void stop() {
		outputLine.stop();
	}

	@Override
	public int write(byte[] buffer, int pos, int len) {
		outputLine.write(buffer, pos, len);
		return len;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return format;
	}

	@Override
	public void flush() {
		outputLine.drain();
	}

	@Override
	public void close() {
		outputLine.close();
	}

}
