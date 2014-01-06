package iristk.speech;

import iristk.speech.Voice.Gender;
import iristk.util.Language;

import java.util.ArrayList;

public class VoiceList extends ArrayList<Voice> {

	//TODO: should be case insensitive
	public Voice getByName(String name) {
		for (Voice voice : this) {
			if (voice.getName().matches(".*" + name + ".*"))
				return voice;
		}
		return null;
	}
	
	public Voice getByLanguage(Language lang) {
		for (Voice voice : this) {
			if (voice.getLanguage() == lang)
				return voice;
		}
		return null;
	}
	
	public Voice getByLanguageAndGender(Language lang, Gender gender) {
		for (Voice voice : this) {
			if (voice.getLanguage() == lang && voice.getGender() == gender)
				return voice;
		}
		return null;
	}
	
}
