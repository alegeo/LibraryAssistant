package iristk.speech;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import iristk.parser.cfg.Grammar;
import iristk.parser.cfg.SrgsGrammar;
import iristk.system.Event;
import iristk.system.InitializationException;
import iristk.system.IrisModule;

public class RecognizerModule extends IrisModule implements RecognizerListener {

	private String location = "default";
	private String listenActionId = null;
	private boolean inSpeech = false;
	
	private List<RecResultProcessor> resultProcessors = new ArrayList<RecResultProcessor>();
	private Float segmentLength;
	private Float startTime;
	private String[] defaultGrammars = new String[0];
	private Recognizer recognizer;
	private HashSet<String> loadedGrammars = new HashSet<String>();
	private HashSet<String> activatedGrammars = new HashSet<String>();
		
	public RecognizerModule(Recognizer recognizer) {
		this.recognizer = recognizer;
		this.recognizer.addRecognizerListener(this);
	}

	public RecognizerModule(String recognizerClassName) throws InitializationException {
		try {
			this.recognizer = (Recognizer) Class.forName(recognizerClassName).newInstance();
			this.recognizer.addRecognizerListener(this);
		} catch (InstantiationException e) {
			throw new InitializationException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new InitializationException(e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new InitializationException(e.getMessage());
		}
	}

	@Override
	public void init() throws InitializationException {
		try {
			recognizer.setEndSilTimeout(700);
		} catch (RecognizerException e) {
			throw new InitializationException(e.getMessage());
		}
	}
	
	public void setDefaultGrammars(String... grammars) {
		defaultGrammars = grammars;
	}
	
	public void loadGrammar(String name, Grammar grammar) throws RecognizerException {
		loadedGrammars.add(name);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new SrgsGrammar(grammar).marshal(out);
		recognizer.loadGrammar(name, out.toString());
	}
	
	public void loadGrammar(String name, URI uri) throws RecognizerException {
		loadedGrammars.add(name);
		recognizer.loadGrammar(name, uri);
	}
	
	public void loadGrammar(String name, File file) throws RecognizerException {
		loadGrammar(name, file.toURI());
	}
	
	public void loadGrammar(String name, String grammarString) throws RecognizerException {
		loadedGrammars.add(name);
		recognizer.loadGrammar(name, grammarString);
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public void addResultProcessor(RecResultProcessor processor) {
		resultProcessors.add(processor);
	}

	@Override
	public void startOfSpeech(float timestamp) {
		inSpeech = true;
		startTime = timestamp;
		Event event = new Event("sense.speech.start");
		event.put("location", location);
		if (listenActionId != null)
			event.put("action", listenActionId);
		monitorState("Speech");
		send(event);
	}
	
	@Override
	public void endOfSpeech(float timestamp) {
		inSpeech = false;
		segmentLength = timestamp - startTime;
		Event event = new Event("sense.speech.end");
		event.put("location", location);
		event.put("length", segmentLength);
		if (listenActionId != null)
			event.put("action", listenActionId);
		monitorState();
		send(event);
	}
	
	@Override
	public void speechSamples(byte[] samples, int pos, int len) {
		for (RecResultProcessor processor : resultProcessors) {
			processor.processSpeech(samples, pos, len);
		}
	}
	
	@Override
	public void recognitionResult(RecognitionResult result) {
		if (!result.isPartial()) {
			//listening = false;
			monitorState();
		}
		for (RecResultProcessor processor : resultProcessors) {
			if (result != null && processor.willTakeTime(result)) {
				sendResult(result);
			}
			result = processor.processResult(result);
		}
		if (result != null) {
			if (segmentLength != null) {
				result.put("length", segmentLength);
			}
			sendResult(result);
		}
	}
	
	private void sendResult(RecognitionResult result) {
		Event event = new Event("sense.speech.rec." + result.getType().label);
		event.putAll(result);
		event.put("location", location);
		if (listenActionId != null)
			event.put("action", listenActionId);
		send(event);
	}
	
	private void activateGrammars(Object grammarParam) throws RecognizerException {
		List<String> grammarList = new ArrayList<String>();
		if (grammarParam != null) {
			if (grammarParam instanceof String) {
				grammarList.add((String) grammarParam);
			} else if (grammarParam instanceof List) {
				grammarList.addAll((List)grammarParam); 
			} else {
				throw new RecognizerException("Not a valid grammar parameter, must be String or List: " + grammarParam.toString());
			}
		} else {
			for (String grammar : defaultGrammars) {
				grammarList.add(grammar);
			}
		}
		HashSet<String> toActivate = new HashSet<String>();
		for (String grammar : grammarList) {
			if (grammar.startsWith("<define")) {
				throw new RecognizerException("grammar <define> not implemented");
				//TODO
			} else if (grammar.startsWith("<url")) {
				//TODO
				throw new RecognizerException("grammar <url> not implemented");
			} else if (loadedGrammars.contains(grammar)) {
				toActivate.add(grammar);
			} else {
				throw new RecognizerException("Not a valid grammar: " + grammar.toString());
			}
		}
		for (String grammar : new ArrayList<String>(activatedGrammars)) {
			if (!toActivate.contains(grammar)) {
				recognizer.deactivateGrammar(grammar);
				activatedGrammars.remove(grammar);
			}
		}
		for (String grammar : toActivate) {
			if (!activatedGrammars.contains(grammar)) {
				recognizer.activateGrammar(grammar, 1);
				activatedGrammars.add(grammar);
			}
		}
	}
	
	@Override
	public void onEvent(Event event) {
		try {
			if (event.getName().equals("action.listen")) {
				if ((event.get("location") == null) || eq(event.get("location"), this.location)) {
					//if (listening) {
						recognizer.stopListen();
					//}
					activateGrammars(event.get("grammar"));
					Event listen = new Event("monitor.listen.start");
					if (location != null)
						listen.put("location", location);
					listenActionId  = event.getId();
					listen.put("action", listenActionId);
					send(listen);	
					//listening = true;
					inSpeech = false;
					segmentLength = null;
					startTime = null;
					for (RecResultProcessor processor : resultProcessors) {
						processor.initRecognition();
					}
					monitorState("Listen");
					recognizer.setEndSilTimeout(event.getInt("endSil", 500));
					recognizer.setNoSpeechTimeout(event.getInt("timeout", 5000));
					recognizer.startListen();
				}
			} else if (event.getName().equals("action.listen.stop")) {
				//if (listening) {
					if (recognizer.stopListen())
						monitorState();
				//}
			}
		} catch (RecognizerException e) {
			e.printStackTrace();
		}
	}

	//public boolean isListening() {
	//	return listening;
	//}

	public boolean isInSpeech() {
		return inSpeech;
	}
	
	public void setPartialResults(boolean cond) {
		recognizer.setPartialResults(cond);
	}

}
