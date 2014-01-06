package iristk.parser.cfg;

import java.io.OutputStream;
import java.util.List;

public interface Grammar {

	public int INFINITY = Integer.MAX_VALUE;
	
	List<Object> getRules();
	
	String getRuleId(Object rule);
	
	boolean isPublic(Object rule);

	List<Object> getMatches(Object ruleOrMatcher);

	boolean isOneOf(Object matcher);

	boolean isRuleRef(Object matcher);
	
	boolean isGroup(Object matcher);

	int getMinRepeat(Object matcher);
	
	int getMaxRepeat(Object matcher);

	String getRuleRef(Object matcher);

	boolean isWord(Object matcher);
	
	String getWordString(Object word);

	boolean isTag(Object matcher);
	
	String getTagScript(Object matcher);
	
	String getLanguage();

	String getRoot();
	
	void marshal(OutputStream out);
	
}
