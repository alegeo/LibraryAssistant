package iristk.speech;

public interface RecResultProcessor {

	RecognitionResult processResult(RecognitionResult result);

	void processSpeech(byte[] samples, int pos, int len);

	void initRecognition();

	boolean willTakeTime(RecognitionResult result);

}
