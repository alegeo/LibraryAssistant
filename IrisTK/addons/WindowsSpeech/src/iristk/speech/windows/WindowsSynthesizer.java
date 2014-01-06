package iristk.speech.windows;

import iristk.speech.windows.ManagedSynthesizer;
import iristk.speech.windows.Phoneme;
import iristk.speech.windows.Phonemes;
import iristk.speech.windows.Voices;
import iristk.audio.Sound;
import iristk.speech.Synthesizer;
import iristk.speech.Transcription;
import iristk.speech.Voice;
import iristk.speech.VoiceList;
import iristk.system.InitializationException;
import iristk.util.Language;
import iristk.util.Mapper;
import iristk.xml.phones.Phones.Phone;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

// TODO: should encode phones to IPA, now SAPI ID is used
// http://msdn.microsoft.com/en-us/library/hh361632(v=office.14).aspx

public class WindowsSynthesizer implements Synthesizer {
	
	static {
		WindowsSpeech.init();
	}

	private final ManagedSynthesizer synth;
	private Voice voice;
	private AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
	private Mapper ipa2ups = new Mapper("ipa2ups", this.getClass().getResourceAsStream("ipa2ups.map"));
	private Language language;

	//public static final Voice ANNA = new Voice("Microsoft Anna", Voice.Gender.FEMALE, Language.ENGLISH_US);
	
	public WindowsSynthesizer() {
		this(Language.ENGLISH_US);
	}

	public WindowsSynthesizer(Language lang) {
		synth = new ManagedSynthesizer();
		setLanguage(lang);
	}
	
	public void setLanguage(Language lang) {
		this.language = lang;
		setVoice(getVoices().getByLanguage(lang));
	}

	@Override
	public void setVoice(Voice voice) {
		this.voice = voice;
		this.language = voice.getLanguage();
		synth.setVoice(voice.getName());
	}

	@Override
	public VoiceList getVoices() {
		VoiceList voices = new VoiceList();
		Voices voiceL = synth.getVoices();
		for (int i = 0; i < voiceL.getLength(); i++) {
			Voice.Gender gender = voiceL.getVoice(i).getGender().equals("Female") ? Voice.Gender.FEMALE : Voice.Gender.MALE;
			voices.add(new Voice(voiceL.getVoice(i).getName(), gender, Language.fromCode(voiceL.getVoice(i).getLang())));
		}
		return voices;
	}
	
	@Override
	public void init() throws InitializationException {
	}

	private Transcription makeTrans(Phonemes phonemes) {
		Transcription trans = new Transcription();
		float pos = 0.0f;
		for (int i = 0; i < phonemes.getLength(); i++) {
			Phoneme phon = phonemes.getPhoneme(i);
			try {
				byte[] b = phon.getLabel().getBytes("UTF-16");
				String unicode;
				if (phon.getLabel().length() == 3) {
					unicode = String.format("%02X%02X+%02X%02X", b[2], b[3], b[6], b[7]);
				} else {
					unicode = String.format("%02X%02X", b[2], b[3]);
				}
				trans.add(ipa2ups.map(unicode), pos, pos + phon.getDuration());
				pos += phon.getDuration();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}	
		}
		return trans;
	}
	
	private String makeSSML(String text) {
		text = text.replaceAll("<spurt.*?>", "");
		text = text.replaceAll("&(?![a-z]+;)", "&amp;");
		String ssml = "<speak version=\"1.0\"";
		ssml += " xmlns=\"http://www.w3.org/2001/10/synthesis\"";
		ssml += " xml:lang=\"" + voice.getLanguage().code + "\">";
		ssml += text;
		ssml += "</speak>";
		return ssml;
	}

	@Override
	public Transcription synthesize(String text, File audioFile) {
		String ssml = makeSSML(text);
		Phonemes phonemes = synth.synthesize(ssml, audioFile.getAbsolutePath());
		Transcription trans = makeTrans(phonemes);
		Phone last = trans.getPhone().get(trans.getPhone().size() - 1);
		if (last.getName().equals("0004") && last.getEnd() - last.getStart() > 0.1) {
			try {
				Sound sound = new Sound(audioFile);
				sound.setSecondsLength(last.getStart() + 0.1f);
				sound.save(audioFile);
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			last.setEnd(last.getStart() + 0.1f);
		}
		return trans;
	}

	@Override
	public Transcription transcribe(String text) {
		String ssml = makeSSML(text);
		Phonemes phonemes = synth.transcribe(ssml);
		Transcription trans = makeTrans(phonemes);
		return trans;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	@Override
	public Voice getVoice() {
		return voice;
	}

	@Override
	public String getSynthesizerName() {
		return "Windows";
	}
	
	public void printVoices() {
		synth.printVoices();
	}

	public void setVoice(String name) {
		synth.setVoice(name);
	}
	
	public static void main(String[] args) {
		WindowsSynthesizer tts = new WindowsSynthesizer();
		tts.printVoices();
		tts.setVoice("Vocalizer Expressive Tom Premium 22kHz");
		System.out.println(tts.synthesize("The language-independent run-time component for the engine, which implements the Vocalizer Expressive API", new File("test.wav")).toString());
	}

}
