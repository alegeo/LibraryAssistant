package iristk.speech;

import java.util.ArrayList;

public class RecognizerListeners extends ArrayList<RecognizerListener> implements RecognizerListener {

	@Override
	public void startOfSpeech(float timestamp) {
		for (RecognizerListener listener : this) {
			listener.startOfSpeech(timestamp);
		}
	}

	@Override
	public void endOfSpeech(float timestamp) {
		for (RecognizerListener listener : this) {
			listener.endOfSpeech(timestamp);
		}
	}

	@Override
	public void speechSamples(byte[] samples, int pos, int len) {
		for (RecognizerListener listener : this) {
			listener.speechSamples(samples, pos, len);
		}
	}

	@Override
	public void recognitionResult(RecognitionResult result) {
		for (RecognizerListener listener : this) {
			listener.recognitionResult(result);
		}
	}

	
}
