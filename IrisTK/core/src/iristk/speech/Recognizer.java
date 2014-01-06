package iristk.speech;

import iristk.util.Language;

import java.io.File;
import java.net.URI;

public interface Recognizer {
	
	void loadGrammar(String name, URI uri) throws RecognizerException;
	
	void loadGrammar(String name, String grammarString) throws RecognizerException;
	
	void activateGrammar(String name, float weight) throws RecognizerException;
	
	void deactivateGrammar(String name) throws RecognizerException;

	/**
	 * Sets the maximum length of silence before the recognizer stops and gives a NOSPEECH result
	 * @param msec the number of milliseconds for the timeout
	 * @throws RecognizerException
	 */
	void setNoSpeechTimeout(int msec) throws RecognizerException;
	
	/**
	 * Sets the end silence threshold for detecting an end of utterance
	 * @param msec the number of milliseconds for the timeout
	 * @throws RecognizerException
	 */
	void setEndSilTimeout(int msec) throws RecognizerException;
	
	/**
	 * Sets the maximum length of an utterance before the recognizer stops and gives a MAXSPEECH result
	 * @param msec the number of milliseconds for the timeout
	 * @throws RecognizerException
	 */
	void setMaxSpeechTimeout(int msec) throws RecognizerException;
	
	/**
	 * Sets whether to generate partial results (default should be false)
	 */
	void setPartialResults(boolean cond);
	
	/**
	 * Tells the recognizer to start listen
	 * @throws RecognizerException
	 */
	void startListen() throws RecognizerException;
	
	/**
	 * Forces the recognizer to stop listening. It should be possible to call this function even if 
	 * the recognizer is not in the listening state. The method should block until the stopping is 
	 * complete and the recognizer is ready to start listening again. 
	 * @return true if the recognizer was listening, false otherwise. 
	 * @throws RecognizerException
	 */
	boolean stopListen() throws RecognizerException;
	
	/**
	 * Recognizes a wave-file
	 * @param file the file to recognize
	 * @return a recognition result
	 * @throws RecognizerException
	 */
	RecognitionResult recognizeFile(File file) throws RecognizerException;

	void setLanguage(Language lang);

	/**
	 * Adds a listener for recognizer events
	 * @param listener
	 */
	void addRecognizerListener(RecognizerListener listener);
	
}
