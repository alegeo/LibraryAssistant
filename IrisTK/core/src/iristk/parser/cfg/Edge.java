package iristk.parser.cfg;

import java.util.List;

public abstract class Edge {

	protected Integer begin;
	protected Integer end;
	
	public abstract List<Edge> matches(Edge passive);

	public abstract boolean isPassive();

	public Integer getBegin() {
		return begin;
	}

	public Integer getEnd() {
		return end;
	}

	public abstract List<Word> getWords();
	
	public abstract int getLevel();
	
}

