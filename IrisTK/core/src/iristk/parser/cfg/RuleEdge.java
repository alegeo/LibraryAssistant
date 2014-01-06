package iristk.parser.cfg;

import iristk.util.Record;

import java.util.ArrayList;
import java.util.List;

public class RuleEdge extends Edge {

	private Object rule;
	private List<Object> matches;
	private List<RuleEdge> subRules = new ArrayList<RuleEdge>();
	private List<Word> words = new ArrayList<Word>();
	private int level;
	private Grammar grammar;
	private Object nativeSem = null;
	
	public RuleEdge(int i, Grammar grammar, Object rule) {
		this.grammar = grammar;
		this.rule = rule;
		this.begin = i;
		this.end = i;
		this.level = 0;
		matches = grammar.getMatches(rule);
		evalTags();
	}
	
	public RuleEdge(RuleEdge active, Edge passive, List<Object> matches) {
		this.rule = active.rule;
		this.grammar = active.grammar;
		this.begin = active.begin;
		this.end = passive.end;
		this.subRules.addAll(active.subRules);
		if (passive instanceof RuleEdge)
			this.subRules.add((RuleEdge)passive);
		this.words.addAll(active.getWords());
		this.words.addAll(passive.getWords());
		this.level = Math.max(active.getLevel(), passive.getLevel() + 1);
		this.matches = matches;
		evalTags();
	}
	
	private void evalTags() {
		for (int i = 0; i < matches.size();) {
			if (grammar.isTag(matches.get(i))) {
				String tagScript = grammar.getTagScript(matches.get(i));
				if (tagScript.length() > 0) {
					nativeSem = TagScript.eval(tagScript, this, nativeSem);
				} 
				matches.remove(i);
			} else {
				break;
			}
		}
		if (isPassive() && nativeSem == null) {
			String result = "";
			for (Word w : words) {
				result += w.toString() + " ";
			}
			nativeSem = result.trim();
		}
	}
	
	public boolean isPublic() {
		return grammar.isPublic(rule);
	}

	private void matches(List<Object> matches, Edge passive, List<Edge> result) {
		Object token = matches.get(0);
		List<Object> rest = matches.subList(1, matches.size());
		if (grammar.isOneOf(token)) {
			for (Object item : grammar.getMatches(token)) {
				List<Object> newMatches = new ArrayList<Object>(rest);
				newMatches.add(0, item);
				matches(newMatches, passive, result);
			}
		} else if (grammar.isGroup(token)) {
			if (grammar.getMinRepeat(token) == 0) {
				matches(new ArrayList<Object>(rest), passive, result);
			}
			List<Object> newMatches = new ArrayList<Object>(rest);
			newMatches.addAll(0, grammar.getMatches(token));
			matches(newMatches, passive, result);
		} else if (grammar.isRuleRef(token)) {
			if (passive instanceof RuleEdge) {
				String refid = grammar.getRuleRef(token);
				if (((RuleEdge)passive).getRuleId().equals(refid)) {
					RuleEdge resEdge = new RuleEdge(this, passive, new ArrayList<Object>(rest));
					result.add(resEdge);
				}
			}
			// else if (ruleref.getSpecial() != null) {
			//	matches(new ArrayList<Object>(rest), passive, result);
			//}
		} else if (grammar.isWord(token)) {
			if (passive instanceof WordEdge) {
				WordEdge we = (WordEdge) passive;
				if (wordMatches(grammar.getWordString(token), we.getWord())) {
					RuleEdge resEdge = new RuleEdge(this, passive, new ArrayList<Object>(rest));
					result.add(resEdge);
				}
			}
		} else {
			System.err.println("Doesn't recognize " + token.getClass());
		}
	}

	private boolean wordMatches(String token, Word word) {
		String pattern = token.replaceAll("_", ".*");
		return word.getWordString().matches("(?i:" + pattern + ")");
	}

	@Override
	public List<Edge> matches(Edge passive) {
		if (matches.size() > 0) {
			List<Edge> result = new ArrayList<Edge>();
			matches(matches, passive, result);
			return result;
		} else {
			return null;
		}
	}
	
	@Override
	public boolean isPassive() {
		return matches.size() == 0;
	}
	
	@Override
	public String toString() {
		return "RuleEdge{" + getRuleId() + ":" + begin + ":" + end + ":" + matches + "}";
	}

	@Override
	public List<Word> getWords() {
		return words;
	}

	public String getRuleId() {
		return grammar.getRuleId(rule);
	}
	
	@Override
	public int getLevel() {
		return level;
	}

	public Object getNativeSem() {
		return nativeSem;
	}
	
	public Record getSem() {
		return TagScript.nativeSemToRecord(nativeSem);
	}

	public List<RuleEdge> getSubRules() {
		return subRules;
	}
	
}
