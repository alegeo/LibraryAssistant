package iristk.speech;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.bind.JAXBException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import iristk.system.InitializationException;

import iristk.xml.phones.Phones;
import iristk.audio.Sound;
import iristk.audio.SoundPlayer;
import iristk.audio.SoundPlayer.CallbackDelegate;
import iristk.system.Event;
import iristk.system.IrisModule;
import iristk.system.IrisUtils;
import iristk.util.XmlMarshaller;
import iristk.util.XmlUtils;

public class SynthesizerModule extends IrisModule {

	private Synthesizer synthesizer;
	protected XmlMarshaller<Phones> phonesMarshaller = new XmlMarshaller<Phones>("iristk.xml.phones");
	private XmlMarshaller<iristk.xml.event.Event> eventMarshaller = new XmlMarshaller<iristk.xml.event.Event>("iristk.xml.event");
	private SoundPlayer player;
	private SpeechThread speechThread = null;
	ArrayList<String> monitorStates = new ArrayList<String>();
	
	public SynthesizerModule() {
	}
	
	public SynthesizerModule(Synthesizer synth) {
		setSynthesizer(synth);
	}
	
	public SynthesizerModule(String synthesizerClassName) throws InitializationException {
		try {
			setSynthesizer((Synthesizer) Class.forName(synthesizerClassName).newInstance());
		} catch (InstantiationException e) {
			throw new InitializationException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new InitializationException(e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new InitializationException(e.getMessage());
		}
	}

	public void setSynthesizer(Synthesizer synth) {
		this.synthesizer = synth;
	}

	@Override
	public void init() throws InitializationException {
		subscribe("action.speech**");
		synthesizer.init();
		if (player == null)
			player = new SoundPlayer(synthesizer.getAudioFormat());
	}

	public AudioFormat getAudioFormat() {
		return getSynthesizer().getAudioFormat();
	}

	public void setSoundPlayer(SoundPlayer player) {
		this.player = player;
	}

	public SoundPlayer getSoundPlayer() {
		return player;
	}

	public Synthesizer getSynthesizer() {
		return synthesizer;
	}

	@Override
	public void onEvent(Event event) {
		if (event.getName().equals("action.speech")) {
			processSpeechAction(event);
		} else if (event.getName().equals("action.speech.stop")) {
			stopSpeaking();
		}
	}

	public void stopSpeaking() {
		player.stop();
		if (speechThread != null && speechThread.isRunning()) {
			speechThread.stopRunning();
		}
	}
	
	public void processSpeechAction(Event speechEvent) {
		stopSpeaking();
		speechThread = new SpeechThread(speechEvent);
	}
	
	protected File getCachePath() {
		File cachePath = IrisUtils.getTempDir(synthesizer.getSynthesizerName());
		if (!cachePath.exists()) {
			cachePath.mkdirs();
		}
		return cachePath;
	}
	
	protected Phones startSpeaking(String synthText) {

		File wavFile = null;
		File phoFile = null;
		Phones phones = null;

		// Not canned: synthesize or load from cache
		String cacheId = cacheId(synthText);
		File cachePath = getCachePath();
		phoFile = new File(cachePath, cacheId + ".pho");
		wavFile = new File(cachePath, cacheId + ".wav");
		if (!wavFile.exists() || !phoFile.exists()) {
			// Synthesize
			phones = synthesizer.synthesize(synthText, wavFile);
			try {
				phonesMarshaller.marshal(phones, phoFile);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		} else {
			// Load from cache
			try {
				phones = phonesMarshaller.unmarshal(phoFile);
			} catch (JAXBException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		try {
			Sound s = new Sound(wavFile);
			player.playAsync(s, new CallbackDelegate() {
				@Override
				public void callback(int pos) {
					if (speechThread != null) {
						synchronized (speechThread) {
							speechThread.notify();
						}
					}
				}
			});
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return phones;
	}
	
	protected void addMonitorState(String state) {
		monitorStates.add(state);
		monitorState(monitorStates.toArray(new String[0]));
	}
	
	protected void removeMonitorState(String state) {
		monitorStates.remove(state);
		monitorState(monitorStates.toArray(new String[0]));
	}
	
	private class SpeechThread extends Thread {

		public Event speechEvent;
		private boolean running;

		public SpeechThread(Event speechEvent) {
			this.speechEvent = speechEvent;
			running = true;
			start();
		}

		@Override
		public void run() {
			try {
				String text = speechEvent.getString("text").trim();
				text = text.replaceAll("&(?![a-z]+;)", "&amp;");
				List<Object> actions = new ArrayList<Object>();
				Document doc = eventMarshaller.stringToDocument("<reply>" + text + "</reply>");
				NodeList children = doc.getDocumentElement().getChildNodes();
				for (int c = 0; c < children.getLength(); c++) {
					if (children.item(c) instanceof Element) {
						try {
							iristk.xml.event.Event child = eventMarshaller.unmarshal(children.item(c));
							actions.add(new Event(child));
							continue;
						} catch (JAXBException e) {
							//e.printStackTrace();
						} 
					}
					String t = XmlUtils.nodeToString(children.item(c));
					if (actions.size() > 0 && actions.get(actions.size()-1) instanceof String) {
						String last = (String)actions.remove(actions.size()-1);
						actions.add(last + " " + t);
					} else {
						actions.add(t);
					}
				}

				for (Object action : actions) {

					if (!running) break;

					if (action instanceof Event) {
						send((Event)action);
					} else if (action instanceof String) {
						String synthText = (String)action;
						Phones phones = startSpeaking(synthText);
						
						final String stateLabel = "\"" + synthText.replaceAll("<.*?>", "") + "\"";
						addMonitorState(stateLabel);
						
						Event onset = new Event("monitor.speech.start");
						onset.put("action", speechThread.speechEvent.getId());
						onset.put("text", synthText);
						if (phones != null) {	
							try {
								onset.put("phones", phonesMarshaller.marshalToDOM(phones));
								onset.put("length", new Transcription(phones).length());
							} catch (JAXBException e) {
								e.printStackTrace();
							}
						}
						send(onset);
						
						synchronized (SpeechThread.this) {
							try {
								SpeechThread.this.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						
						removeMonitorState(stateLabel);
						Event offset = new Event("monitor.speech.end");
						offset.put("action", speechThread.speechEvent.getId());
						send(offset);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e1) {
				e1.printStackTrace();
			} 
			running = false;
		}

		public void stopRunning() {
			running = false;
			synchronized (SpeechThread.this) {
				SpeechThread.this.notify();
			}
			try {
				join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public boolean isRunning() {
			return running;
		}

	}

	private String cacheId(String text) {
		String id;
		try {
			id = URLEncoder.encode(text, "UTF-8");
			if (id.length() > 100) {
				id = id.substring(0, 100) + text.hashCode();
			}
		} catch (UnsupportedEncodingException e) {
			id = "" + text.hashCode();
		}
		return id + "." + synthesizer.getVoice().getName();
	}

}
