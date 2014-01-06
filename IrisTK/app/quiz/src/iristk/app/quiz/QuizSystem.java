package iristk.app.quiz;

import iristk.agent.embr.EMBRModule;
import iristk.audio.AudioSourceReader;
import iristk.audio.PortAudioSource;
import iristk.dialog.SimulateEnter;
import iristk.flow.FlowModule;
import iristk.speech.RecognizerModule;
import iristk.speech.SynthesizerModule;
import iristk.speech.windows.WindowsRecognizer;
import iristk.speech.windows.WindowsSynthesizer;
import iristk.speech.windows.WindowsSynthesizerModule;
import iristk.system.IrisMonitorGUI;
import iristk.system.IrisSystem;
import iristk.util.Language;

public class QuizSystem {

	private QuizFlow quizFlow;
	
	public QuizSystem() {
		try {
			IrisSystem system = new IrisSystem("social");
			
			new IrisMonitorGUI(system);
			
			quizFlow = new QuizFlow();
			FlowModule flowModule = new FlowModule(quizFlow);
			system.addModule("quiz", flowModule);
			system.addModule("multiparty", new FlowModule(quizFlow.mp));
			
			new QuizWebServer(flowModule, quizFlow);
			
			system.addModule("embr", new EMBRModule());
			
			WindowsSynthesizer tts = new WindowsSynthesizer();
			system.addModule("tts", new SynthesizerModule(tts));
			tts.setVoice(tts.getVoices().getByName("Hazel"));
			
			AudioSourceReader microphones = new AudioSourceReader(new PortAudioSource(16000, 2));
			
			for (int i = 1; i <= 2; i++) {
				RecognizerModule rec = new RecognizerModule(new WindowsRecognizer(Language.ENGLISH_US, microphones.newAudioListener(i-1)));
				rec.setLocation("microphone-" + i);
				rec.loadGrammar("default", getClass().getResource("QuizGrammar.xml").toURI());
				rec.setDefaultGrammars("default"); 
				
				for (Question q : quizFlow.questions) {
					rec.loadGrammar(q.getId(), q.getGrammar());
				}
				
				system.addModule("asr-" + i, rec);
			}
		 	
		 	microphones.runAsync();
		 	
			system.addModule("sim", new SimulateEnter());
			
			system.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new QuizSystem();
	}
	
}