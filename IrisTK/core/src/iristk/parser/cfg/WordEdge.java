package iristk.parser.cfg;

import java.util.ArrayList;
import java.util.List;

public class WordEdge extends Edge {

	private Word word;
	
	public WordEdge(int i, Word word) {
		this.word = word;
		this.begin = i;
		this.end = i + 1;
	}
	
	@Override
	public boolean isPassive() {
		return true;
	}

	@Override
	public List<Edge> matches(Edge passive) {
		return null;
	}

	public Word getWord() {
		return word;
	}
	
	@Override
	public String toString() {
		return "WordEdge{" + word.toString() + ":" + begin + ":" + end + "}";
	}

	@Override
	public List<Word> getWords() {
		ArrayList<Word> words = new ArrayList<Word>();
		words.add(word);
		return words;
	}

	@Override
	public int getLevel() {
		return 0;
	}
}
