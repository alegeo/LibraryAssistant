package iristk.speech.cereproc;
import com.ochafik.lang.jnaerator.runtime.Structure;
import com.sun.jna.Pointer;
/**
 * Structure to hold transcription data for the audio buffer<br>
 * The transcription holds useful non-speech information about the audio output.<br>
 * It can be used for a variety of purposes, such as lip syncing for animated <br>
 * characters (using the phoneme transcriptions), or word highlighting in an <br>
 * application (using the 'cptk' markers).  User-specified input markers are also<br>
 * stored in the transcription structure.<br>
 * All data is also accessible via helper functions.<br>
 * <i>native declaration : line 128</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class CPRC_abuf_trans extends Structure<CPRC_abuf_trans, CPRC_abuf_trans.ByValue, CPRC_abuf_trans.ByReference > {
	/**
	 * Text content of the transcription. <br>
	 * - Phone transcription - text contains the CereProc phoneme string. <br>
	 * - Word transcription - contains the text of the word being spoken.<br>
	 * - Marker transcription <br>
	 * - contains the text of the marker name, as supplied by the user via <br>
	 * markup such as SSML (e.g. 'marker_test' for <br>
	 * '<mark name="marker_test"\/>'<br>
	 * - can also contain a CereProc tokenisation marker. CereProc token markers <br>
	 * have the form 'cptk_n1_n2_n3_n4', where:<br>
	 * - n1 - integer character offset of the token<br>
	 * - n2 - integer byte offset of the token<br>
	 * - n3 - integer characters length of the token<br>
	 * - n4 - integer byte length of the token.<br>
	 * C type : const char*
	 */
	public Pointer name;
	/**
	 * Type of transcription<br>
	 * @see CPRC_ABUF_TRANS<br>
	 * C type : CPRC_ABUF_TRANS
	 */
	public int type;
	/// Start time, in seconds, of the transcription event
	public float start;
	/// End time, in seconds, of the transcription event
	public float end;
	/**
	 * Not used<br>
	 * C type : CPRC_abuf_dsp*
	 */
	public Pointer dsp;
	public CPRC_abuf_trans() {
		super();
		initFieldOrder();
	}
	protected void initFieldOrder() {
		setFieldOrder(new java.lang.String[]{"name", "type", "start", "end", "dsp"});
	}
	/**
	 * @param name Text content of the transcription. <br>
	 * - Phone transcription - text contains the CereProc phoneme string. <br>
	 * - Word transcription - contains the text of the word being spoken.<br>
	 * - Marker transcription <br>
	 * - contains the text of the marker name, as supplied by the user via <br>
	 * markup such as SSML (e.g. 'marker_test' for <br>
	 * '<mark name="marker_test"\/>'<br>
	 * - can also contain a CereProc tokenisation marker. CereProc token markers <br>
	 * have the form 'cptk_n1_n2_n3_n4', where:<br>
	 * - n1 - integer character offset of the token<br>
	 * - n2 - integer byte offset of the token<br>
	 * - n3 - integer characters length of the token<br>
	 * - n4 - integer byte length of the token.<br>
	 * C type : const char*<br>
	 * @param type Type of transcription<br>
	 * @see CPRC_ABUF_TRANS<br>
	 * C type : CPRC_ABUF_TRANS<br>
	 * @param start Start time, in seconds, of the transcription event<br>
	 * @param end End time, in seconds, of the transcription event<br>
	 * @param dsp Not used<br>
	 * C type : CPRC_abuf_dsp*
	 */
	public CPRC_abuf_trans(Pointer name, int type, float start, float end, Pointer dsp) {
		super();
		this.name = name;
		this.type = type;
		this.start = start;
		this.end = end;
		this.dsp = dsp;
		initFieldOrder();
	}
	@Override
	protected ByReference newByReference() { return new ByReference(); }
	@Override
	protected ByValue newByValue() { return new ByValue(); }
	@Override
	protected CPRC_abuf_trans newInstance() { return new CPRC_abuf_trans(); }
	public static CPRC_abuf_trans[] newArray(int arrayLength) {
		return Structure.newArray(CPRC_abuf_trans.class, arrayLength);
	}
	public static class ByReference extends CPRC_abuf_trans implements Structure.ByReference {
		
	};
	public static class ByValue extends CPRC_abuf_trans implements Structure.ByValue {
		
	};
}