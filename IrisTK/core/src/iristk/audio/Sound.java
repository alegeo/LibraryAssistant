/*
* Copyright 2009-2010 Gabriel Skantze.
* All Rights Reserved.  Use is subject to license terms.
*
* See the file "license.terms" for information on usage and
* redistribution of this file, and for a DISCLAIMER OF ALL
* WARRANTIES.
*
*/
package iristk.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;


public class Sound {

	private File file;
	private AudioFormat	audioFormat;
	private int soundLength;
	private byte[] soundData;

	public Sound() {
	}
	
	public Sound(File file) throws UnsupportedAudioFileException, IOException {
		this();
		load(file);
	}
	
	public Sound(byte[] soundData, AudioFormat audioFormat) {
		this();
		this.soundData = soundData;
		this.soundLength = soundData.length;
		this.audioFormat = audioFormat;
	}
	
	public Sound(AudioInputStream audio) throws IOException {
		load(audio);
	}
	
	public void load(AudioInputStream audio) throws IOException {
		this.audioFormat = audio.getFormat();
		ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
		int	nBytesRead = 0;
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		while ((nBytesRead = audio.read(buffer, 0, bufferSize)) != -1) {
			dataStream.write(buffer, 0, nBytesRead);
		}
		this.soundData = dataStream.toByteArray();
		this.soundLength = soundData.length;	
	}

	private void load(AudioInputStream audioInputStream, OutputStream outStream, int bufferSize) throws UnsupportedAudioFileException, IOException {
		this.audioFormat = audioInputStream.getFormat();
		
		ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
		
		int	nBytesRead = 0;
		int pos = 0;
		byte[] buffer = new byte[bufferSize];
		while (true) {
			nBytesRead = audioInputStream.read(buffer, 0, bufferSize);
			if (nBytesRead == -1) 
				break;
			dataStream.write(buffer, 0, nBytesRead);
			if (outStream != null) {
				outStream.write(buffer); 
			}
			pos += nBytesRead;
		}
		this.soundData = dataStream.toByteArray();
		this.soundLength = soundData.length;	
		audioInputStream.close();	
	}
	
	public short getSample(int pos, int channel) {
		int p = pos * getAudioFormat().getFrameSize() + channel * 2;
		return AudioUtil.bytesToShort(getAudioFormat(), soundData[p], soundData[p + 1]);
	}
	
	public float getSecondsLength() {
		return getSampleLength() / audioFormat.getSampleRate();
	}
	
	public int getSampleLength() {
		return soundLength / audioFormat.getFrameSize();
	}
	
	public void setSecondsLength(float length) {
		this.soundLength = (int) (length * audioFormat.getSampleRate() * audioFormat.getFrameSize());
	}
	
	public void setSampleLength(int length) {
		this.soundLength = length * audioFormat.getFrameSize();
	}
	
	/** 
	 * Simultaneously load the sound and write the data to an OutputStream
	 */
	public void load(File file, OutputStream outStream, int bufferSize) throws UnsupportedAudioFileException, IOException {
		this.file = file;
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
		load(audioInputStream, outStream, bufferSize);
	}
	
	/** 
	 * Simultaneously load the sound and write the data to an OutputStream
	 */
	public void load(URL url, OutputStream outStream, int bufferSize) throws UnsupportedAudioFileException, IOException {
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
		load(audioInputStream, outStream, bufferSize);	
	}
	
	public void load(File file) throws UnsupportedAudioFileException, IOException {
		load(file, null, 320);
	}
	
	public void load(URL url) throws UnsupportedAudioFileException, IOException {
		load(url, null, 320);
	}

	public byte[] getData() {
		return soundData;
	}
	
	public void setData(byte[] data) {
		this.soundData = data;
		this.soundLength = data.length;
	}
	
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}
	
	public void setAudioFormat(AudioFormat format) {
		this.audioFormat = format;
	}

	public void save(File file) {
		this.file = file;
		ByteArrayInputStream bis = new ByteArrayInputStream(soundData);		
		AudioInputStream ais = new AudioInputStream(bis, audioFormat, soundLength/audioFormat.getFrameSize());
		try {
			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	public void play() {
		JSoundPlayer player = new JSoundPlayer(audioFormat);
		player.open();
		player.play(this);
		player.close();
	}
	*/
}
