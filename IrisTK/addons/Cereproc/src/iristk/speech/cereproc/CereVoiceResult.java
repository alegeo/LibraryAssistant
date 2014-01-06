package iristk.speech.cereproc;

import iristk.speech.Transcription;
import iristk.util.Mapper;
import iristk.xml.phones.Phones.Phone;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.sun.jna.Pointer;

public class CereVoiceResult {

	private static final int TRANS_PHONE = Cerevoice_engLibrary.CPRC_ABUF_TRANS.CPRC_ABUF_TRANS_PHONE;
	private static final int TRANS_WORD = Cerevoice_engLibrary.CPRC_ABUF_TRANS.CPRC_ABUF_TRANS_WORD;
	private static final int TRANS_MARK = Cerevoice_engLibrary.CPRC_ABUF_TRANS.CPRC_ABUF_TRANS_MARK;
	private static final int TRANS_ERROR = Cerevoice_engLibrary.CPRC_ABUF_TRANS.CPRC_ABUF_TRANS_ERROR;
	
	private static Mapper cereproc2ups = new Mapper("cereproc2ups", CereVoiceResult.class.getResourceAsStream("cereproc2ups.map"));
	
	private Pointer pointer;
	private int length;
	private Transcription transcription;
	
	public CereVoiceResult(Pointer pointer) {
		this.pointer = pointer;
		this.length = Cerevoice_Library.INSTANCE.CPRC_abuf_wav_sz(pointer);	
		this.transcription = new Transcription();
		for (int i = 0; i < Cerevoice_Library.INSTANCE.CPRC_abuf_trans_sz(pointer); i++) {
			CPRC_abuf_trans trans = Cerevoice_Library.INSTANCE.CPRC_abuf_get_trans(pointer, i);
			float start = Cerevoice_Library.INSTANCE.CPRC_abuf_trans_start(trans); 
			float end = Cerevoice_Library.INSTANCE.CPRC_abuf_trans_end(trans); 
			String name = Cerevoice_Library.INSTANCE.CPRC_abuf_trans_name(trans);
			int type = Cerevoice_Library.INSTANCE.CPRC_abuf_trans_type(trans);
			if (type == TRANS_PHONE) { 
				name = name.replaceAll("[0-9]", "");
				name = cereproc2ups.map(name);
				transcription.add(name, start, end);
			}
		}
		Phone lastPhone = transcription.getPhone().get(transcription.getPhone().size()-1);
		// Trim silence at the end
		if (lastPhone.getName().equals("sil") && (lastPhone.getEnd() - lastPhone.getStart()) > 0.1f) {
			//System.out.println(this.length);
			this.length -= (int) (((lastPhone.getEnd() - lastPhone.getStart()) - 0.1f) * getSampleRate());
			//System.out.println(this.length);
			lastPhone.setEnd(lastPhone.getStart() + 0.1f);
		}
	}
	
	public Pointer getPointer() {
		return pointer;
	}
	
	public int getSampleRate() {
		return Cerevoice_Library.INSTANCE.CPRC_abuf_wav_srate(pointer);	
	}
	
	public int getLength() {
		return length;
	}
	
	public Transcription getTranscription() {
		return transcription;
	}
	
	public AudioFormat getAudioFormatAsBigEndian() {
		return new AudioFormat(getSampleRate(), 16, 1, true, true);
	}
	
	public AudioFormat getAudioFormatAsLittleEndian() {
		return new AudioFormat(getSampleRate(), 16, 1, true, false);
	}
	
	public short[] getDataAsShort() {
		Pointer bp = Cerevoice_Library.INSTANCE.CPRC_abuf_wav_data(pointer);
		return bp.getShortArray(0, getLength());
	}

	public byte[] getDataAsLittleEndian() {
		ByteBuffer byteBuf = ByteBuffer.allocate(getLength() * 2);
		byteBuf.order(ByteOrder.LITTLE_ENDIAN);
		for (short s : getDataAsShort()) {
			byteBuf.putShort(s);
		}
		return byteBuf.array();
	}
	
	public void writeWav(File wavFile) {
		AudioFormat format = getAudioFormatAsLittleEndian();
		byte[] data = getDataAsLittleEndian();
		ByteArrayInputStream bis = new ByteArrayInputStream(data);		
		AudioInputStream ais = new AudioInputStream(bis, format, data.length/format.getFrameSize());
		try {
			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
