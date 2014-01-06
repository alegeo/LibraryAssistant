package iristk.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class FileAudioTarget implements AudioTarget {

	private AudioFormat format;
	private FileOutputStream currentOut;
	private File currentFile;
	private File currentFileTmp;
	private boolean recording = false;
	private File recordingPath = null;
	private int recN = 0;

	public FileAudioTarget(AudioFormat format) {
		this.format = format;
	}
	
	public FileAudioTarget(AudioFormat format, File recordingPath) {
		this.format = format;
		this.recordingPath  = recordingPath;
		recordingPath.mkdirs();
	}

	@Override
	public void start() {
		if (recordingPath != null) {
			while (true) {
				recN++;
				File file = new File(recordingPath, "rec-" + recN + ".wav");
				File tmpFile = new File(recordingPath, "rec-" + recN + ".wav.tmp");
				if (!file.exists() && !tmpFile.exists()) {
					startRecording(file);
					break;
				}
			}
		}
	}

	public synchronized void startRecording(File file) {
		this.recording = true;
		this.currentFile = file;
		this.currentFileTmp = new File(file.getAbsolutePath() + ".tmp");
		try {
			currentOut = new FileOutputStream(currentFileTmp);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void stopRecording() {
		try {
			currentOut.flush();
			currentOut.close();
		    FileInputStream fi = new FileInputStream(currentFileTmp);
		    AudioInputStream ai = new AudioInputStream(fi, format, currentFileTmp.length() / 2);
		    AudioSystem.write(ai, AudioFileFormat.Type.WAVE, currentFile);
		    fi.close();
		    currentFileTmp.delete();
		    currentOut = null;
			recording = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() {
		if (recordingPath != null) {
			stopRecording();
		}
	}

	@Override
	public void flush() {
		if (currentOut != null) {
			try {
				currentOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int write(byte[] buffer, int pos, int len) {
		record(buffer, pos, len);
		return len;
	}
	

	private synchronized void record(byte[] buffer, int pos, int len) {
		if (currentOut != null) {
			try {
				currentOut.write(buffer, pos, len);
				currentOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public AudioFormat getAudioFormat() {
		return format;
	}

	@Override
	public void close() {
		if (currentOut != null)
			stopRecording();
	}

	public boolean isRecording() {
		return recording;
	}
	
}
