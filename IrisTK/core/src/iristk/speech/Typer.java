package iristk.speech;

import iristk.parser.cfg.Parser;
import iristk.parser.cfg.SrgsGrammar;
import iristk.speech.RecognitionResult.ResultType;
import iristk.util.Language;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class Typer implements Recognizer {
	
	JTextField textField;
	private Parser parser = new Parser();
	private boolean inSpeech = false;
	private boolean listening = false;
	private RecognizerListeners listeners = new RecognizerListeners();
	
	public void Typer() {
		JFrame window = new JFrame();
		textField = new JTextField();
		textField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent arg0) {	
			}
			@Override
			public void keyReleased(KeyEvent key) {
				if (key.getKeyCode() == 10) {
					typingDone();
				} else {
					if (inSpeech) {
						listeners.startOfSpeech(System.currentTimeMillis() / 1000f);
						inSpeech = true;
					}
				}
			}
			@Override
			public void keyPressed(KeyEvent arg0) {
			}
		});
		window.add(textField);
		textField.setPreferredSize(new Dimension(300,30));
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}

	private void typingDone() {
		if (listening) {
			String text = textField.getText().trim();
			listeners.endOfSpeech(System.currentTimeMillis() / 1000f);
			listeners.recognitionResult(new RecognitionResult(ResultType.FINAL, text, 1.0f, 3.0f, parser.parse(text).getSem()));
			textField.setText("");
			textField.setBackground(Color.white);
		}
		inSpeech = false;
		listening = false;
	}

	@Override
	public void loadGrammar(String name, URI uri) throws RecognizerException {
		parser.addGrammar(new SrgsGrammar(uri));
	}

	@Override
	public void loadGrammar(String name, String grammarString) throws RecognizerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void activateGrammar(String name, float weight)
			throws RecognizerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deactivateGrammar(String name) throws RecognizerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNoSpeechTimeout(int msec) throws RecognizerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEndSilTimeout(int msec) throws RecognizerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMaxSpeechTimeout(int msec) throws RecognizerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startListen() throws RecognizerException {
		listening = true;
	}

	@Override
	public boolean stopListen() throws RecognizerException {
		listening = false;
		return true;
	}

	@Override
	public RecognitionResult recognizeFile(File file)
			throws RecognizerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLanguage(Language lang) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addRecognizerListener(RecognizerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void setPartialResults(boolean cond) {
		// TODO Auto-generated method stub
		
	}

	
}
