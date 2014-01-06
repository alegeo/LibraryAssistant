package iristk.audio;

import iristk.audio.AudioTarget;

import java.io.File;
import java.io.FileNotFoundException;
import javax.sound.sampled.AudioFormat;

public class RecordableAudioTarget implements AudioTarget {

	private FileAudioTarget recorder;
	private AudioTargetMonitor monitor;
	
	public RecordableAudioTarget(AudioTarget target) {
		monitor = new AudioTargetMonitor(target);
		recorder = new FileAudioTarget(monitor.getAudioFormat());
		monitor.addTarget(recorder, true);
	}
	
	public void startRecording(File recordFile) throws FileNotFoundException {
		recorder.startRecording(recordFile);
	}
	
	public void stopRecording() {
		recorder.stopRecording();
	}

	@Override
	public void start() {
		monitor.start();
	}

	@Override
	public void stop() {
		monitor.stop();
	}

	@Override
	public void flush() {
		monitor.flush();
	}

	@Override
	public int write(byte[] buffer, int pos, int len) {
		return monitor.write(buffer, pos, len);
	}
	
	@Override
	public AudioFormat getAudioFormat() {
		return monitor.getAudioFormat();
	}

	@Override
	public void close() {
		monitor.close();
	}
	
}
