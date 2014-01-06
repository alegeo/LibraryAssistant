package iristk.speech.windows;

import iristk.parser.cfg.Grammar;
import iristk.parser.cfg.Parser;
import iristk.parser.cfg.SrgsGrammar;
import iristk.speech.windows.IResultListener;
import iristk.speech.windows.ManagedRecognizer;
import iristk.speech.windows.Result;
import iristk.speech.windows.Word;
import iristk.audio.AudioSource;
import iristk.audio.AudioSourceGate;
import iristk.audio.AudioSourceReader;
import iristk.audio.AudioTarget;
import iristk.audio.PortAudioSource;
import iristk.speech.RecognitionResult;
import iristk.speech.Recognizer;
import iristk.speech.RecognizerException;
import iristk.speech.RecognitionResult.ResultType;
import iristk.speech.RecognizerListener;
import iristk.speech.RecognizerListeners;
import iristk.util.Language;
import iristk.util.Record;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sound.sampled.AudioFormat;

public class WindowsRecognizer implements Recognizer, IResultListener {
	
	ManagedRecognizer recognizer;
	private int adaptation = 0;
	private long startListen;
	private Parser parser;
	private HashMap<String,Grammar> parserGrammars = new HashMap<String,Grammar>();
	private RecognizerListeners listeners = new RecognizerListeners();
	private boolean listening = false;
	private AudioSourceReader audioSourceReader;
	private SpeechAudioStream speechRecStream;
	private AudioSourceGate audioSource;
	private boolean inSpeech;
	private boolean partialResults = false;
	
	static {
		WindowsSpeech.init();
	}
	
	public WindowsRecognizer() {
		this(Language.ENGLISH_US, new PortAudioSource(16000, 1));
	}
	
	public WindowsRecognizer(Language lang, AudioSource audioSource) {
		recognizer = new ManagedRecognizer();
		recognizer.init(lang.code);
		recognizer.setRecognizerSetting("CFGConfidenceRejectionThreshold", 5);
		recognizer.setRecognizerSetting("AdaptationOn", adaptation);
		recognizer.setRecognizerSetting("PersistedBackgroundAdaptation", adaptation);
		recognizer.registerListener(this);
		parser = new Parser();
		recognizer.setInputToAudioStream((int)audioSource.getAudioFormat().getSampleRate(), 3200);
		speechRecStream = recognizer.getAudioStream();
		this.audioSource = new AudioSourceGate(audioSource);
		audioSourceReader = new AudioSourceReader(this.audioSource);
		audioSourceReader.addAudioTarget(new SpeechRecAudioWriter());
	}
	
	private class SpeechRecAudioWriter implements AudioTarget {
		
		@Override
		public int write(byte[] buffer, int pos, int len) {
			speechRecStream.Write(buffer, pos, len);
			return len;
		}
		
		@Override
		public void stop() {
		}
		
		@Override
		public void start() {
		}
		
		@Override
		public AudioFormat getAudioFormat() {
			return audioSource.getAudioFormat();
		}
		
		@Override
		public void flush() {				
		}
		
		@Override
		public void close() {
		}
	}

	@Override
	public void loadGrammar(String name, URI uri) throws RecognizerException {
		if (uri.toString().equals("builtin:dictation")) {
			recognizer.loadDictationGrammar(name);
		} else {
			recognizer.loadGrammarFromPath(name, new File(uri).getAbsolutePath());
			parserGrammars.put(name, new SrgsGrammar(uri));
		}
	}

	@Override
	public void loadGrammar(String name, String grammarString) throws RecognizerException {
		if (grammarString.equals("<DICTATION>")) {
			recognizer.loadDictationGrammar(name);
		} else {
			recognizer.loadGrammarFromString(name, grammarString);
			parserGrammars.put(name, new SrgsGrammar(grammarString));
		}
	}

	@Override
	public void activateGrammar(String name, float weight) throws RecognizerException {
		recognizer.activateGrammar(name);
		parser.addGrammar(parserGrammars.get(name));
	}

	@Override
	public void deactivateGrammar(String name) throws RecognizerException {
		recognizer.deactivateGrammar(name);
		parser.removeGrammar(parserGrammars.get(name));
	}

	@Override
	public void setNoSpeechTimeout(int msec) throws RecognizerException {
		recognizer.setNoSpeechTimeout(msec);
	}

	@Override
	public void setEndSilTimeout(int msec) throws RecognizerException {
		recognizer.setEndSilTimeout(msec);
	}

	@Override
	public void setMaxSpeechTimeout(int msec) throws RecognizerException {
		recognizer.setMaxSpeechTimeout(msec);
	}

	@Override
	public void startListen() throws RecognizerException {
		speechRecStream.Clear();
		audioSource.releaseGate();
		audioSourceReader.runAsync();
		startListen = System.currentTimeMillis();
		listening = true;
		inSpeech = false;
		recognizer.recognize();
	}

	@Override
	public synchronized boolean stopListen() throws RecognizerException {
		if (listening) {
			recognizer.recognizeCancel();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized void recognizeCompleted(Result result) {
		if (listening) {
			listening  = false;
			audioSourceReader.stop();
			if (inSpeech) {
				listeners.endOfSpeech((System.currentTimeMillis() - startListen) / 1000f);
			}
			if (result.isCancelled()) {
				listeners.recognitionResult(new RecognitionResult(ResultType.CANCELED));
			} else if (result.isTimeout()) {
				listeners.recognitionResult(new RecognitionResult(ResultType.NOSPEECH));	
			} else {
				if (result.size() > 0) {
					//System.out.println(result.getGrammar());
					String text = "";//result.getHypothesis(0).getText();
					List<iristk.parser.cfg.Word> words = new ArrayList<iristk.parser.cfg.Word>();
					for (int i = 0; i < result.getHypothesis(0).size(); i++) {
						Word w = result.getHypothesis(0).getWord(i);
						words.add(new iristk.parser.cfg.Word(w.getText(), w.getConfidence()));
						text += " " + w.getText();
					}
					text = text.trim();
					RecognitionResult recresult = new RecognitionResult(ResultType.FINAL, text);
					recresult.setConf(result.getHypothesis(0).getConfidence());
					recresult.setLength((float) (result.getLength()/1000));
					//recresult.setSem(parseSemantics(result.getHypothesis(0).getSemantics()));
					parser.processResult(recresult);
					listeners.recognitionResult(recresult);
				} else {
					RecognitionResult recresult = new RecognitionResult(ResultType.FINAL, RecognitionResult.NOMATCH);
					recresult.setLength(3.0f);
					listeners.recognitionResult(recresult);
				}
			}
		}
	}
	
	private Record parseSemantics(SemanticStruct struct) {
		if (struct == null)
			return null;
		Record result = new Record();
		for (int i = 0; i < struct.getKeysCount(); i++) {
			SemanticStruct child = struct.getValue(i);
			if (child.getValue() != null) {
				result.put(struct.getKey(i), child.getValue().toString());
			} else {
				result.put(struct.getKey(i), parseSemantics(child));
			}
		}
		return result;
	}

	@Override
	public void recognizeHypothesis(Result result) {
		if (partialResults && result.size() > 0) {
			String text = result.getHypothesis(0).getText();
			RecognitionResult recresult = new RecognitionResult(ResultType.PARTIAL, text);
			recresult.setConf(result.getHypothesis(0).getConfidence());
			recresult.setLength((float) (result.getLength()/1000));
			parser.processResult(recresult);
			listeners.recognitionResult(recresult);
		}
	}

	@Override
	public void speechDetected(int audioLevel) {
		audioSource.lockGate(true);
		inSpeech = true;
		listeners.startOfSpeech((System.currentTimeMillis() - startListen) / 1000f);
	}

	@Override
	public RecognitionResult recognizeFile(File file) throws RecognizerException {
		//TODO
		return null;
	}

	@Override
	public void setLanguage(Language lang) {
		//TODO
	}

	@Override
	public void addRecognizerListener(RecognizerListener listener) {
		listeners.add(listener);
	}

	@Override
	public void setPartialResults(boolean cond) {
		partialResults = cond;
	}
	
}
