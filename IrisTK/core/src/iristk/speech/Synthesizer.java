package iristk.speech;

import iristk.system.InitializationException;

import java.io.File;
import javax.sound.sampled.AudioFormat;

public interface Synthesizer {
	
	/**
	 * Initializes the synthesizer. This method will be called once after the synthesizer is constructed and specific parameters are set.
	 * @throws InitializationException
	 */
	void init() throws InitializationException;
	
	/**
	 * Synthesizes text and writes the audio to a wav-file
	 * @param text The text to synthesize
	 * @param file The wav-file to write
	 * @return The transcription of the synthesized text
	 */
	Transcription synthesize(String text, File file);
	
	/**
	 * Transcribes a text
	 * @param text The text to synthesize
	 * @return The transcription of the text if it would be synthesized
	 */
	Transcription transcribe(String text);

	/**
	 * @return the audio format of the wav-files that are produced by this synthesizer
	 */
	AudioFormat getAudioFormat();

	/**
	 * Sets the currently selected voice
	 * @throws InitializationException 
	 */
	void setVoice(Voice voice) throws InitializationException;
	
	/**
	 * @return the currently selected voice
	 */
	Voice getVoice();
	
	/**
	 * @return a list of voices supported by this synthesizer
	 */
	VoiceList getVoices();
	
	/**
	 * @return a convenient name for this synthesizer
	 */
	String getSynthesizerName();
	
}
