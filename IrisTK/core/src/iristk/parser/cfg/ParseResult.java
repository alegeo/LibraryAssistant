package iristk.parser.cfg;

import iristk.util.Record;

import java.util.ArrayList;
import java.util.List;

public class ParseResult extends ArrayList<ParseResult.Phrase> {
	
	public static class Phrase {
		
		private Object sem;
		private List<Word> words;
		private String ruleId;
		
		public Phrase() {
			this.words = new ArrayList<Word>();
		}
		
		public Phrase(String ruleId, Object sem, List<Word> list) {
			this();
			this.words.addAll(list);
			this.sem = sem;
			this.ruleId = ruleId;
		}

		public List<Word> getWords() {
			return words;
		}

		public Object getSem() {
			return sem;
		}
		
		public String getRuleId() {
			return ruleId;
		}
		
		@Override
		public String toString() {
			if (sem == null)
				return null;
			else
				return sem.toString();
		}
		
	}
	
	
	public Record getSem() {
		Record sem = new Record();
		for (Phrase phrase : this) {
			if (phrase.getSem() instanceof Record) {
				sem.putAll((Record)phrase.getSem());
			}
		}
		return sem;
	}
	
	/*
	public static String objectToString(Object obj) {
		if (obj instanceof NativeObject) {
			String result = "{";
			NativeObject no = (NativeObject) obj;
			boolean first = true;
			for (Object pid : NativeObject.getPropertyIds(no)) {
				if (!first) result += ",";
				result += pid + ":" + objectToString(NativeObject.getProperty(no,pid.toString()));
				first = false;
			}
			result += "}";
			return result;
		} else {
			return obj.toString();
		}
	}
	*/
	
	/*
	public static String semToString(Object sem) {
		if (sem == null) {
			return "";
		} else if (sem.getSem().size() > 0) {
			String result = "{";
			boolean first = true;
			for (Sem c : sem.getSem()) {
				if (!first)
					result += ",";
				result += c.getField() + ":" + semToString(c);
				first = false;
			}
			result += "}";
			return result;
		} else {
			return sem.getValue();
		}
	}
	*/

	/*
	public List<Sem> getSemList() {
		List<Sem> result = new ArrayList<Sem>();
		for (ParseResult.Phrase phrase : this) {	
			if (phrase.getSem() != null) {
				try {
					result.add(phrase.getSem());
				} catch (IllegalArgumentException e) {
					System.err.println("Cannot translate " + phrase.getSem() + " to concept");
				}
			}
		}
		return result;
	}
	*/
	
	public float getSemCoverage() {
		float result = 0;
		float tot = 0;
		for (ParseResult.Phrase phrase : this) {
			if (phrase.getSem() != null) {
				result += phrase.getWords().size();
			}
			tot +=  phrase.getWords().size();
		}
		return result / tot;
	}
	
}
