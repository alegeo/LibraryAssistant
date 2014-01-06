package iristk.audio;

import iristk.audio.AudioSource;

import java.io.File;
import java.io.FileNotFoundException;
import javax.sound.sampled.AudioFormat;

public class RecordableAudioSource implements AudioSource {

	private FileAudioTarget recorder;
	private AudioSourceReader reader;
	private AudioSource outSource;
	private AudioSource source;

	public RecordableAudioSource(AudioSource source) {
		this.source = source;
		this.reader = new AudioSourceReader(source);
		this.recorder = new FileAudioTarget(source.getAudioFormat());
		reader.addAudioTarget(recorder);
		this.outSource = reader.newAudioListener();
		reader.runAsync();
	}
	
	public void startRecording(File recordFile) throws FileNotFoundException {
		recorder.startRecording(recordFile);
	}
	
	public void stopRecording() {
		recorder.stopRecording();
	}

	@Override
	public void start() {
		outSource.start();
	}

	@Override
	public void stop() {
		outSource.stop();
	}

	@Override
	public int read(byte[] buffer, int pos, int len) {
		return outSource.read(buffer, pos, len);
	}

	@Override
	public AudioFormat getAudioFormat() {
		return source.getAudioFormat();
	}

	@Override
	public void close() {
		reader.stop();
	}

	public boolean isRecording() {
		return recorder.isRecording();
	}

	@Override
	public int getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
