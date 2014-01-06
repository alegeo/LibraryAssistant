package iristk.speech.nuance9;

import iristk.speech.RecognitionResult;
import iristk.speech.nuance9.xml.Interpretation;
import iristk.speech.nuance9.xml.Result;
import iristk.util.Record;
import iristk.util.XmlMarshaller;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.PointerByReference;

public class NuanceResult extends RecognitionResult {

	private Pointer handle;
	private String xmlResult;
	
	static XmlMarshaller<Result> marshaller;
	
	static {
		marshaller = new XmlMarshaller<Result>("iristk.speech.nuance9.xml");
	}
	
	NuanceResult(Pointer handle, boolean makeWords) {
		super(ResultType.FINAL);
		this.handle = handle;
		PointerByReference xr = new PointerByReference();
		try {
			BaseRecognizer.call("SWIrecGetXMLResult", SWIrec.INSTANCE.SWIrecGetXMLResult(handle, new WString("application/x-vnd.speechworks.recresult+xml"), xr));
		} catch (NuanceException e) {
			e.printStackTrace();
		}
		xmlResult = xr.getValue().getString(0, true);

		try {
			Result xResult = marshaller.unmarshal(xmlResult);
			if (xResult.getInterpretation().size() > 0) {
				Interpretation interp = xResult.getInterpretation().get(0);
				setConf(interp.getConf());
				setText(interp.getText().getContent());
				if (makeWords) {
					List<Word> words = new ArrayList<Word>();
					for (iristk.speech.nuance9.xml.Word word : interp.getInstance().getSWILiteralTimings().getAlignment().getWord()) {
						words.add(new Word(word.getContent(), word.getConfidence(), (float)word.getStart() / 1000f, (float)word.getEnd() / 1000f));
					}
					setWords(words);
				}
				if (interp.getInstance() != null && interp.getInstance().getSWIMeaning() != null) {
					String meaning = interp.getInstance().getSWIMeaning().trim();
					if (meaning.length() > 0) {
						put("sem", parseMeaning(meaning));
					}
				}
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		/*
		Matcher m = Pattern.compile("[0-9\\.]+").matcher(xmlResult);
		if (m.find()) {
			setConf(Float.parseFloat(m.group()));
		}
		m = Pattern.compile("<text mode=\"voice\">(.*?)<").matcher(xmlResult);
		if (m.find()) {
			setText(m.group(1));
		}
		*/
	}
	
	// {act_move:true movement:{steps:2} piece:{relPiecePos:{piece:{type:Queen} rel:FrontOf} type:Pawn}}
	private static Object parseMeaning(String meaning) {
		if (meaning.startsWith("{"))
			return parseRecord(meaning);
		else
			return meaning;
	}
	
	public static void main(String[] args) {
		System.out.println(parseMeaning("{act_move:true movement:{steps:2} piece:{relPiecePos:{piece:{type:Queen} rel:FrontOf} type:Pawn}}"));
	}
	
	private static Record parseRecord(String meaning) {
		Record result = new Record();
		meaning = meaning.substring(1, meaning.length() - 1);
		int col = meaning.indexOf(':');
		int pos = 0;
		while (col > -1) {
			String field = meaning.substring(pos, col);
			String value;
			if (meaning.charAt(col+1) == '{') {
				int para = 1;
				pos = col+1;
				while (para > 0) {
					pos++;
					char c = meaning.charAt(pos);
					if (c == '}')
						para--;
					else if (c == '{')
						para++;
				}
				value = meaning.substring(col+1, pos+1);
				pos += 2;
			} else {
				pos = meaning.indexOf(' ', col);
				if (pos == -1)
					pos = meaning.length();
				value = meaning.substring(col+1, pos);
				pos++;
			}
			result.put(field, parseMeaning(value));
			col = meaning.indexOf(':', pos);
		}
		return result;
	}
	
	NuanceResult(RecognitionResult.ResultType type) {
		super(type);
	}
	
	NuanceResult(RecognitionResult.ResultType type, String text) {
		super(type, text);
	}
	
	public String toXML() { 
		return xmlResult;
	}
	
	public String toLattice() {
		PointerByReference xr = new PointerByReference(); 
		try {
			BaseRecognizer.call("SWIrecGetXMLResult", SWIrec.INSTANCE.SWIrecGetXMLResult(handle, new WString("application/x-vnd.speechworks.wordlattice+xml"), xr));
		} catch (NuanceException e) {
			e.printStackTrace();
		}
		return xr.getValue().getString(0, true);
	}
	
	//@Override
	//public String toString() {
	//	return super.getText();
	//}
	
}
