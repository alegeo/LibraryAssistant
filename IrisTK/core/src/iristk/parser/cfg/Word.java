package iristk.parser.cfg;

public class Word {

	private String word;
	private Double conf;
	
	public Word(String word) {
		this.word = word;
		setConf(1.0);
	}
	
	public Word(String word, double conf) {
		this.word = word;
		setConf(conf);
	}

	@Override
	public String toString() {
		return word;
	}

	public Double getConf() {
		return conf;
	}

	public void setConf(Double conf) {
		this.conf = conf;
	}

	public String getWordString() {
		return word;
	}

}
