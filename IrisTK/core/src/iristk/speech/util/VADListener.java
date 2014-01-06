package iristk.speech.util;

public interface VADListener {
	
	public void vadEvent(long pos, boolean inSpeech, int energy);
	
}
