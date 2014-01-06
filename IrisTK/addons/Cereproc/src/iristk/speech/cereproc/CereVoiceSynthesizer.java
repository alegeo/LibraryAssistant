package iristk.speech.cereproc;

import iristk.speech.Synthesizer;
import iristk.speech.Transcription;
import iristk.speech.Voice;
import iristk.speech.VoiceList;
import iristk.system.InitializationException;
import iristk.system.IrisUtils;
import iristk.util.Language;
import iristk.util.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;

import com.sun.jna.Pointer;

public class CereVoiceSynthesizer implements Synthesizer {

	private Voice voice;
	private Pointer engine;
	private int channel;
	private String langCode;
	private File cereprocDir;
	private File prerecPath;
	private VoiceList voices = new VoiceList();
	private File voicesDir;
	
	private static final String PACKAGE = "Cereproc";
	
	public static HashMap<String,String> GESTURES = new HashMap<String,String>();
	static {
		GESTURES.put("GESTURE_TUT", "<spurt audio=\"g0001_001\">tut</spurt>");
		GESTURES.put("GESTURE_TUT_TUT", "<spurt audio=\"g0001_002\">tut tut</spurt>");
		GESTURES.put("GESTURE_COUGH_1", "<spurt audio=\"g0001_003\">COUGH</spurt>");
		GESTURES.put("GESTURE_COUGH_2", "<spurt audio=\"g0001_004\">COUGH</spurt>");
		GESTURES.put("GESTURE_COUGH_3", "<spurt audio=\"g0001_005\">COUGH</spurt>");
		GESTURES.put("GESTURE_CLEAR_THROAT", "<spurt audio=\"g0001_006\">CLEAR THROAT</spurt>");
		GESTURES.put("GESTURE_BREATH_IN", "<spurt audio=\"g0001_007\">BREATH</spurt>");
		GESTURES.put("GESTURE_SHARP_INTAKE_OF_BREATH", "<spurt audio=\"g0001_008\">BREATH</spurt>");
		GESTURES.put("GESTURE_BREATH_IN_THROUGH_TEETH", "<spurt audio=\"g0001_009\">BREATH</spurt>");
		GESTURES.put("GESTURE_SIGH_HAPPY", "<spurt audio=\"g0001_010\">SIGH</spurt>");
		GESTURES.put("GESTURE_SIGH_SAD", "<spurt audio=\"g0001_011\">SIGH</spurt>");
		GESTURES.put("GESTURE_HMM_QUESTION", "<spurt audio=\"g0001_012\">hm</spurt>");
		GESTURES.put("GESTURE_HMM_YES", "<spurt audio=\"g0001_013\">hm</spurt>");
		GESTURES.put("GESTURE_HMM_THINKING", "<spurt audio=\"g0001_014\">hm</spurt>");
		GESTURES.put("GESTURE_UMM_1", "<spurt audio=\"g0001_015\">ehm</spurt>");
		GESTURES.put("GESTURE_UMM_2", "<spurt audio=\"g0001_016\">ehm</spurt>");
		GESTURES.put("GESTURE_ERR_1", "<spurt audio=\"g0001_017\">ehm</spurt>");
		GESTURES.put("GESTURE_ERR_2", "<spurt audio=\"g0001_018\">eh</spurt>");
		GESTURES.put("GESTURE_GIGGLE_1", "<spurt audio=\"g0001_019\">GIGGLE</spurt>");
		GESTURES.put("GESTURE_GIGGLE_2", "<spurt audio=\"g0001_020\">GIGGLE</spurt>");
		GESTURES.put("GESTURE_LAUGH_1", "<spurt audio=\"g0001_021\">LAUGH</spurt>");
		GESTURES.put("GESTURE_LAUGH_2", "<spurt audio=\"g0001_022\">LAUGH</spurt>");
		GESTURES.put("GESTURE_LAUGH_3", "<spurt audio=\"g0001_023\">LAUGH</spurt>");
		GESTURES.put("GESTURE_LAUGH_4", "<spurt audio=\"g0001_024\">LAUGH</spurt>");
		GESTURES.put("GESTURE_AH_POSITIVE", "<spurt audio=\"g0001_025\">ah</spurt>");
		GESTURES.put("GESTURE_AH_NEGATIVE", "<spurt audio=\"g0001_026\">ah</spurt>");
		GESTURES.put("GESTURE_YEAH_QUESTION", "<spurt audio=\"g0001_027\">yeah</spurt>");
		GESTURES.put("GESTURE_YEAH_POSITIVE", "<spurt audio=\"g0001_028\">yeah</spurt>");
		GESTURES.put("GESTURE_YEAH_RESIGNED", "<spurt audio=\"g0001_029\">yeah</spurt>");
		GESTURES.put("GESTURE_SNIFF_1", "<spurt audio=\"g0001_030\">SNIFF</spurt>");
		GESTURES.put("GESTURE_SNIFF_2", "<spurt audio=\"g0001_031\">SNIFF</spurt>");
		GESTURES.put("GESTURE_ARGH_1", "<spurt audio=\"g0001_032\">ARGH</spurt>");
		GESTURES.put("GESTURE_ARGH_2", "<spurt audio=\"g0001_033\">ARGH</spurt>");
		GESTURES.put("GESTURE_UGH", "<spurt audio=\"g0001_034\">ugh</spurt>");
		GESTURES.put("GESTURE_OCHT", "<spurt audio=\"g0001_035\">ocht</spurt>");
		GESTURES.put("GESTURE_YAY", "<spurt audio=\"g0001_036\">yay</spurt>");
		GESTURES.put("GESTURE_OH_POSITIVE", "<spurt audio=\"g0001_037\">oh</spurt>");
		GESTURES.put("GESTURE_OH_NEGATIVE", "<spurt audio=\"g0001_038\">oh</spurt>");
		GESTURES.put("GESTURE_SARCASTIC_NOISE", "<spurt audio=\"g0001_039\">NOISE</spurt>");
		GESTURES.put("GESTURE_YAWN_1", "<spurt audio=\"g0001_040\">YAWN</spurt>");
		GESTURES.put("GESTURE_YAWN_2", "<spurt audio=\"g0001_041\">YAWN</spurt>");
		GESTURES.put("GESTURE_SNORE", "<spurt audio=\"g0001_042\">SNORE</spurt>");
		GESTURES.put("GESTURE_SNORE_PHEW", "<spurt audio=\"g0001_043\">SNORE</spurt>");
		GESTURES.put("GESTURE_ZZZ", "<spurt audio=\"g0001_044\">ZZZ</spurt>");
		GESTURES.put("GESTURE_RASPBERRY_1", "<spurt audio=\"g0001_045\">RASPBERRY</spurt>");
		GESTURES.put("GESTURE_RASPBERRY_2", "<spurt audio=\"g0001_046\">RASPBERRY</spurt>");
		GESTURES.put("GESTURE_BRRR_COLD", "<spurt audio=\"g0001_047\">BRR</spurt>");
		GESTURES.put("GESTURE_SNORT", "<spurt audio=\"g0001_048\">SNORT</spurt>");
		GESTURES.put("GESTURE_HA_HA_SARCASTIC", "<spurt audio=\"g0001_050\">ha ha</spurt>");
		GESTURES.put("GESTURE_DOH", "<spurt audio=\"g0001_051\">doh</spurt>");
		GESTURES.put("GESTURE_GASP", "<spurt audio=\"g0001_052\">GASP</spurt>");
	}
	
	public CereVoiceSynthesizer(Voice voice) throws InitializationException {
		this();
		setVoice(voice);
	}
	
	public CereVoiceSynthesizer(String voiceName) throws InitializationException {
		this();
		setVoice(getVoices().getByName(voiceName));
	}
	
	public CereVoiceSynthesizer() throws InitializationException {
		cereprocDir = IrisUtils.getPackagePath(PACKAGE);
		IrisUtils.loadPackageDlls(PACKAGE);
		/*
		IrisUtils.loadPackageLib64(PACKAGE, "x64/w64gcc_s_sjlj-1.dll");
		IrisUtils.loadPackageLib64(PACKAGE, "x64/libstdc++-6.dll");
		IrisUtils.loadPackageLib64(PACKAGE, "x64/libcerevoice_shared-3.dll");
		IrisUtils.loadPackageLib64(PACKAGE, "x64/libcerehts_shared-3.dll");
		IrisUtils.loadPackageLib64(PACKAGE, "x64/libcerevoice_pmod_shared-3.dll");
		IrisUtils.loadPackageLib64(PACKAGE, "x64/libcerevoice_eng_shared-3.dll");
		IrisUtils.loadPackageLib64(PACKAGE, "x64/libcerevoice_aud_shared-3.dll");
		IrisUtils.loadPackageLib32(PACKAGE, "libcerevoice_shared-3.dll");
		IrisUtils.loadPackageLib32(PACKAGE, "libcerehts_shared-3.dll");
		IrisUtils.loadPackageLib32(PACKAGE, "libcerevoice_pmod_shared-3.dll");
		IrisUtils.loadPackageLib32(PACKAGE, "libcerevoice_eng_shared-3.dll");
		IrisUtils.loadPackageLib32(PACKAGE, "libcerevoice_aud_shared-3.dll");
		*/
		
		voicesDir = new File(cereprocDir, "voices");
		if (voicesDir.exists()) {
			for (File file : voicesDir.listFiles()) {
				if (file.isDirectory()) {
					String voiceName = file.getName();
					Properties props = new Properties();
					try {
						File propFile = new File(new File(voicesDir, voiceName), voiceName + ".properties");
						if (propFile.exists()) {
							props.load(new FileReader(propFile));
							Voice.Gender gender = Voice.Gender.fromString(props.getProperty("gender"));
							Language lang = Language.fromCode(props.getProperty("language"));
							voices.add(new Voice(voiceName, gender, lang));
						}
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
					}
				}
			}
		} 
		if (voices.size() == 0)
			throw new InitializationException("No voices installed");
	}
	
	@Override
	public void setVoice(Voice voice) throws InitializationException {
		this.voice = voice;
		File voiceDir = new File(voicesDir, voice.getName());
		File voiceFile = new File(voiceDir, voice.getName() + ".voice");
		File licfile = new File(voiceDir, voice.getName() + ".lic");
		if (!licfile.exists()) {
			throw new InitializationException("Cannot find " + licfile.getAbsolutePath());
		}
		if (!voiceFile.exists()) {
			throw new InitializationException("Cannot find " + voiceFile.getAbsolutePath());
		}	
		engine = Cerevoice_engLibrary.INSTANCE.CPRCEN_engine_load(licfile.getAbsolutePath(), voiceFile.getAbsolutePath());
		channel = Cerevoice_engLibrary.INSTANCE.CPRCEN_engine_open_default_channel(engine);
		prerecPath = new File(voiceDir, "prerec");
	}

	@Override
	public void init() throws InitializationException {
		if (voice == null) {
			Voice def = getVoices().getByName("william");
			if (def == null)
				def = getVoices().get(0);
			setVoice(def);
		} else {
			setVoice(voice);
		}
	}
	
	@Override
	public Transcription synthesize(String text, File outputWaveFilename) {
		CereVoiceResult ab = synthesize(text);
		// Check to see if this audio is canned
		Matcher matcher = Pattern.compile("<spurt audio=\"(.*?)\"").matcher(text);
		if (matcher.find()) {
			File prerec = new File(getPrerecPath(), matcher.group(1) + ".wav");
			if (prerec.exists()) {
				try {
					Utils.copyFile(prerec, outputWaveFilename);
					return ab.getTranscription();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		ab.writeWav(outputWaveFilename);
		return ab.getTranscription();
	}
	
	@Override
	public Transcription transcribe(String text) {
		CereVoiceResult ab = synthesize(text);
		return ab.getTranscription();
	}

	@Override
	public AudioFormat getAudioFormat() {
		return new AudioFormat(22050, 16, 1, true, false);
	}

	@Override
	public Voice getVoice() {
		return voice;
	}
	
	@Override
	public String getSynthesizerName() {
		return "CereVoice";
	}

	@Override
	public VoiceList getVoices() {
		return voices;
	}
	
	public void writeToFile(String filename) {
		try {
			ByteBuffer outfile = ByteBuffer.wrap(filename.getBytes("utf-8"));
			Cerevoice_engLibrary.INSTANCE.CPRCEN_engine_channel_to_file(engine, channel, outfile, Cerevoice_engLibrary.CPRCEN_AUDIO_FORMAT.CPRCEN_RIFF );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public CereVoiceResult synthesize(String msg) {
		if (GESTURES.containsKey(msg))
			msg = GESTURES.get(msg);
		Cerevoice_engLibrary.INSTANCE.CPRCEN_engine_clear_callback(engine, channel);
		try {
			msg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xml:lang=\"" + langCode + "\">" + msg + "</speak>";
			
			ByteBuffer bytes = ByteBuffer.wrap(msg.getBytes("utf-8"));
				
			return new CereVoiceResult(Cerevoice_engLibrary.INSTANCE.CPRCEN_engine_channel_speak(engine, channel, bytes, bytes.capacity(), 1));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public File synthToCache(String text, File cachePath) {
		String cacheId;
		File wavFile = null;
		try {
			cacheId = URLEncoder.encode(text, "UTF-8");
			wavFile = new File(cachePath, voice + "_" + cacheId + ".wav");
			if (!wavFile.exists()) {
				String synthText = text;
				if (GESTURES.containsKey(text))
					synthText = GESTURES.get(text);
				CereVoiceResult ab = synthesize(synthText);
				ab.writeWav(wavFile);	
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return wavFile;
	}

	public File getPrerecPath() {
		return prerecPath;
	}
	
}
