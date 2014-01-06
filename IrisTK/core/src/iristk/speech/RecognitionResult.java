package iristk.speech;

import iristk.util.Record;

import java.util.List;

public class RecognitionResult extends Record {
	
	public enum ResultType {
		
		FINAL ("final"), 
		PARTIAL ("partial"), 
		NOSPEECH ("nospeech"), 
		MAXSPEECH ("maxspeech"), 
		CANCELED ("canceled"), 
		FAILED ("failed");
		
		public final String label;

		ResultType(String label) {
			this.label = label;
		}
		
	};
	
	private final ResultType type;
	public static final String NOMATCH = "<NOMATCH>";
	
	public RecognitionResult(RecognitionResult toClone) {
		super(toClone);
		this.type = toClone.type;
	}
	
	public RecognitionResult(ResultType type) {
		this.type = type;
	}
	
	public RecognitionResult(ResultType type, String text, float conf, float length, Record sem) {
		this.type = type;
		setText(text);
		setConf(conf);
		setLength(length);
		setSem(sem);
	}

	public RecognitionResult(ResultType type, String text) {
		this.type = type;
		setText(text);
	}

	public boolean isNomatch() {
		return getText().equals(NOMATCH);
	}
	
	public boolean isFinal() {
		return type == ResultType.FINAL;
	}
	
	public boolean isPartial() {
		return type == ResultType.PARTIAL;
	}
	
	public boolean isTimeout() {
		return type == ResultType.NOSPEECH;
	}
	
	public boolean isMaxSpeech() {
		return type == ResultType.MAXSPEECH;
	}
	
	public boolean isCanceled() {
		return type == ResultType.CANCELED;
	}
	
	public boolean isFailed() {
		return type == ResultType.FAILED;
	}

	public ResultType getType() {
		return type;
	}
	
	public Float getConf() {
		return getFloat("conf");
	}

	public void setConf(Float conf) {
		put("conf", conf);
	}

	public String getText() {
		return getString("text");
	}

	public void setText(String text) {
		put("text", text);
	}
	
	public Float getLength() {
		return getFloat("length");
	}

	public void setLength(Float length) {
		put("length", length);
	}

	public Record getSem() {
		return getRecord("sem");
	}

	public void setSem(Record sem) {
		put("sem", sem);
	}
	
	public void setNbest(List<NbestHyp> nbest) {
		put("nbest", nbest);
	}
	
	public List<NbestHyp> getNbest() {
		return getList("nbest");
	}
	
	public List<Word> getWords() {
		return getList("words");
	}
	
	public void setWords(List<Word> words) {
		put("words", words);
	}
	
	public static class NbestHyp extends Record {
		
		public NbestHyp(String text) {
			setText(text);
		}
		
		public String getText() {
			return getString("text");
		}

		public void setText(String text) {
			put("text", text);
		}
		
	}
	
	public static class Word extends Record {
		
		public Word(String text, Float conf, Float startTime, Float endTime) {
			setText(text);
			setConf(conf);
			setStartTime(startTime);
			setEndTime(endTime);
		}
		
		public Float getConf() {
			return getFloat("conf");
		}

		public void setConf(Float conf) {
			put("conf", conf);
		}

		public String getText() {
			return getString("text");
		}

		public void setText(String text) {
			put("text", text);
		}
		
		public Float getEndTime() {
			return getFloat("endTime");
		}

		public void setEndTime(Float time) {
			put("endTime", time);
		}
		
		public Float getStartTime() {
			return getFloat("startTime");
		}

		public void setStartTime(Float time) {
			put("startTime", time);
		}
		
	}
	
}
