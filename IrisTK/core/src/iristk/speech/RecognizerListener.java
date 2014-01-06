package iristk.speech;

public interface RecognizerListener {

	/**
	 * A start of speech event
	 * @param timestamp the number of seconds since listening started
	 */
	void startOfSpeech(float timestamp);

	/**
	 * An end of speech event
	 * @param timestamp the number of seconds since listening started
	 */
	void endOfSpeech(float timestamp);

	void speechSamples(byte[] samples, int pos, int len);

	void recognitionResult(RecognitionResult result);

}
