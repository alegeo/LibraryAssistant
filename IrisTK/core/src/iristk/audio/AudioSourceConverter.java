package iristk.audio;

import javax.sound.sampled.AudioFormat;

public class AudioSourceConverter implements AudioSource {

	private AudioSource source;
	private AudioFormat format;
	private float resampleRatio;
	private byte[] readBuffer = null;
	
	public AudioSourceConverter(AudioSource source, int sampleRate) {
		this.source = source;
		this.format = new AudioFormat(
				source.getAudioFormat().getEncoding(),
				sampleRate,
				source.getAudioFormat().getSampleSizeInBits(),
				source.getAudioFormat().getChannels(),
				source.getAudioFormat().getFrameSize(),
				sampleRate,
				source.getAudioFormat().isBigEndian());
		resampleRatio = source.getAudioFormat().getSampleRate() / sampleRate;
	}
	
	@Override
	public void start() {
		source.start();
	}

	@Override
	public void stop() {
		source.stop();
	}

	@Override
	public int read(byte[] buffer, int pos, int len) {
		int readLen = (int) (len * resampleRatio);
		if (readBuffer == null || readBuffer.length != readLen) {
			readBuffer = new byte[readLen];
		}
		int actLen = source.read(readBuffer, 0, readBuffer.length);
		if (actLen < 1) {
			return actLen;
		}
		AudioUtil.resample(readBuffer, 0, actLen, buffer, pos, format.getFrameSize(), (int) source.getAudioFormat().getSampleRate(), (int) format.getSampleRate());
		return (int) (actLen / resampleRatio);
	}

	@Override
	public AudioFormat getAudioFormat() {
		return format;
	}

	@Override
	public void close() {
		source.close();
	}

	@Override
	public int getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

}
