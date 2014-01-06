package iristk.speech.cereproc;

import iristk.speech.SynthesizerModule;
import iristk.speech.Voice;
import iristk.system.InitializationException;

public class CereVoiceModule extends SynthesizerModule {
	
	public CereVoiceModule(Voice voice) throws InitializationException {
		super(new CereVoiceSynthesizer(voice));
	}
	
	public CereVoiceModule() throws InitializationException {
		super(new CereVoiceSynthesizer());
	}

	public CereVoiceModule(String voiceName) throws InitializationException {
		super(new CereVoiceSynthesizer(voiceName));
	}

}
