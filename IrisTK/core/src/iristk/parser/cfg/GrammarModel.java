package iristk.parser.cfg;

import java.util.ArrayList;
import java.util.List;

public abstract class GrammarModel implements Grammar {

	private String language;
	private String root;
	private List<Object> rules = new ArrayList<Object>();

	public GrammarModel() {
	}

	public GrammarModel(Grammar grammar) {
		this.language = grammar.getLanguage();
		this.root = grammar.getRoot();
		for (Object rule : grammar.getRules()) {
			rules.add(new Rule(grammar.getRuleId(rule), grammar.isPublic(rule), convert(grammar, grammar.getMatches(rule))));
		}
	}
	
	private List<Object> convert(Grammar grammar, List<Object> matches) {
		List<Object> result = new ArrayList<Object>();
		for (Object match : matches) {
			result.add(convert(grammar, match));
		}
		return result;
	}
	
	private Object convert(Grammar grammar, Object match) {
		if (grammar.isGroup(match)) {
			Group group = new Group(convert(grammar, grammar.getMatches(match)));
			group.setRepeat(grammar.getMinRepeat(match), grammar.getMaxRepeat(match));
			return group;
		} else if (grammar.isOneOf(match)) {
			OneOf oneof = new OneOf(convert(grammar, grammar.getMatches(match)));
			return oneof;
		} else if (grammar.isRuleRef(match)) {
			return new RuleRef(grammar.getRuleRef(match));
		} else if (grammar.isWord(match)) {
			return grammar.getWordString(match);
		} else if (grammar.isTag(match)) {
			return new Tag(grammar.getTagScript(match));
		} else {
			throw new RuntimeException("Cannot convert " + match);
		}
	}
	
	protected void addRule(String ruleId, boolean isPublic, List<Object> matches) {
		rules.add(new Rule(ruleId, isPublic, matches));
	}
	
	protected void setLanguage(String language) {
		this.language = language;
	}
	
	protected void setRoot(String root) {
		this.root = root;
	}
	
	@Override
	public List<Object> getRules() {
		return rules;
	}

	@Override
	public String getRuleId(Object rule) {
		return ((Rule)rule).getId();
	}

	@Override
	public boolean isPublic(Object rule) {
		return ((Rule)rule).isPublic();
	}

	@Override
	public List<Object> getMatches(Object ruleOrMatcher) {
		return new ArrayList<Object>((List)ruleOrMatcher);
	}

	@Override
	public boolean isOneOf(Object matcher) {
		return (matcher instanceof OneOf);
	}

	@Override
	public boolean isRuleRef(Object matcher) {
		return (matcher instanceof RuleRef);
	}

	@Override
	public boolean isGroup(Object matcher) {
		return (matcher instanceof Group);
	}

	@Override
	public int getMinRepeat(Object matcher) {
		return ((Group)matcher).getMinRepeat();
	}

	@Override
	public int getMaxRepeat(Object matcher) {
		return ((Group)matcher).getMaxRepeat();
	}

	@Override
	public String getRuleRef(Object matcher) {
		return ((RuleRef)matcher).getRef();
	}

	@Override
	public boolean isWord(Object matcher) {
		return matcher instanceof String;
	}

	@Override
	public String getWordString(Object word) {
		return word.toString();
	}
	
	@Override
	public boolean isTag(Object matcher) {
		return matcher instanceof Tag;
	}

	@Override
	public String getTagScript(Object matcher) {
		return ((Tag)matcher).getScript();
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public String getRoot() {
		return root;
	}
	
	public static class Tag {

		private String script;
		
		public Tag(String script) {
			this.script = script;
		}
		
		public String getScript() {
			return script;
		}
		
	}
	
	public static class RuleRef {

		private String ref;
		
		public RuleRef(String ref) {
			this.ref = ref;
		}
		
		public String getRef() {
			return ref;
		}
		
	}
	
	public static class Rule extends ArrayList<Object> {

		private boolean isPublic;
		private String ruleId;

		public Rule(String ruleId, boolean isPublic, List<Object> matches) {
			super(matches);
			this.ruleId = ruleId;
			this.isPublic = isPublic;
		}

		public boolean isPublic() {
			return isPublic;
		}

		public String getId() {
			return ruleId;
		}
		
	}
	
	public static class OneOf extends ArrayList<Object> {

		public OneOf(List<Object> matches) {
			super(matches);
		}
		
		public OneOf() {
		}
		
	}
	
	public static class Group extends ArrayList<Object> {
				
		private int minRepeat;
		private int maxRepeat;

		public Group(List<Object> matches) {
			super(matches);
			this.minRepeat = 1;
			this.maxRepeat = 1;
		}
		
		public void setRepeat(int min, int max) {
			this.minRepeat = min;
			this.maxRepeat = max;
		}
		
		public int getMaxRepeat() {
			return maxRepeat;
		}

		public int getMinRepeat() {
			return minRepeat;
		}
		
	}

}
