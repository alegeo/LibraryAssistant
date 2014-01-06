package iristk.audio;

import iristk.speech.util.EnergyVAD;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

import javax.sound.sampled.AudioFormat;

public class AudioSourceGate implements AudioSource {

	private AudioSource audioSource;
	//private int threshold;
	private boolean locked = false;
	private boolean gateOpen = false;
	//private double[] samples = null;
	private int lag;
	private EnergyVAD vad;
	private LinkedList<byte[]> queue = new LinkedList<>();
	private int queueSize;

	public AudioSourceGate(AudioSource audioSource) {
		this.audioSource = audioSource;
		//this.threshold = threshold;
		this.vad = new EnergyVAD(audioSource.getAudioFormat());
		lag = EnergyVAD.WINSIZE * 10; 
	}
	
	@Override
	public void start() {
		audioSource.start();
	}

	@Override
	public void stop() {
		audioSource.stop();
		queue.clear();
		queueSize = 0;
	}

	private int msec(int bytes) {
		return (int) ((1000 * bytes) / (audioSource.getAudioFormat().getFrameRate() * audioSource.getAudioFormat().getFrameSize()));
	}
	
	@Override
	public int read(byte[] buffer, int pos, int len) {
		while (queueSize < lag) {
			byte [] readBuf = new byte[len];
			audioSource.read(readBuf, 0, len);
			vad.processSamples(readBuf, 0, len);
			queue.add(readBuf);
			queueSize += msec(len);
		}
		byte qBuf[] = queue.removeFirst();
		queueSize -= msec(qBuf.length);
		if ((!locked && !vad.isSpeech()) || (locked && !gateOpen)) {
			Arrays.fill(buffer, 0, len, (byte)0);
		} else {
			System.arraycopy(qBuf, 0, buffer, pos, len);
		}
		return len;
		/*
		if (samples == null || samples.length != len / 2)
			samples = new double[len / 2];
		int rlen = audioSource.read(buffer, pos, len);
		AudioUtil.bytesToDoubles(getAudioFormat(), buffer, pos, len, samples, 0);
		int pow = AudioUtil.power(samples, 0, samples.length);
		if ((!locked && pow < threshold) || (locked && !gateOpen)) {
			Arrays.fill(buffer, 0, rlen, (byte)0);
		}
		return rlen;
		*/
	}

	@Override
	public AudioFormat getAudioFormat() {
		return audioSource.getAudioFormat();
	}

	@Override
	public void close() {
		audioSource.close();
	}

	@Override
	public int getPosition() {
		return audioSource.getPosition();
	}
	
	public static void main(String[] args) throws Exception {
		AudioSource source = new AudioSourceGate(new PortAudioSource(16000, 1));
		AudioSourceReader reader = new AudioSourceReader(source);
		reader.addAudioTarget(new FileAudioTarget(source.getAudioFormat(), new File("c:\\test")));
		reader.run(10f);
	}

	public void lockGate(boolean gateOpen) {
		locked = true;
		this.gateOpen = gateOpen;
	}

	public void releaseGate() {
		locked = false;
	}
	
}
