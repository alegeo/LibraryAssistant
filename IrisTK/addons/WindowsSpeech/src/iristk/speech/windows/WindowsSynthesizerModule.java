package iristk.speech.windows;

import java.io.FileNotFoundException;

import iristk.speech.SynthesizerModule;

public class WindowsSynthesizerModule extends SynthesizerModule {
	
	public WindowsSynthesizerModule() throws FileNotFoundException {
		super(new WindowsSynthesizer());
	}
	
}
