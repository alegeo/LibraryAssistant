package iristk.parser.cfg;

import iristk.util.ParsedInputStream;
import iristk.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  
Extensions to ABNF:

*			0- repeat
+			1- repeat
_			Matches any word
red_		Matches all words that starts with "red"
 
 */

public class ABNFGrammar extends GrammarModel {
	
	public ABNFGrammar() {
	}
	
	public ABNFGrammar(InputStream is) throws IOException {
		ParsedInputStream reader = new ParsedInputStream(is);
		String line = "";
		while (reader.available() > 0) {
			line += reader.readTo(';');
			int sb = StringUtils.countMatches(line, '{');
			int eb = StringUtils.countMatches(line, '}');
			if (sb == eb) {
				line = line.trim();
				if (line.length() > 0) {
					parseLine(line);
				}
				line = "";
			} else {
				line += ";";
			}
		}
	}
	
	public ABNFGrammar(File file) throws IOException {
		this(new FileInputStream(file));
	}
	
	public ABNFGrammar(Grammar grammar) {
		super(grammar);
	}
		
	public void parseLine(String line) {
		if (line.startsWith("language")) {
			setLanguage(line.substring(line.lastIndexOf(" ")).trim());
		} else if (line.startsWith("root")) {
			setRoot(line.substring(line.lastIndexOf(" ")).trim().substring(1));
		} else if (line.startsWith("public $") || line.startsWith("$")) {
			Matcher m = Pattern.compile("(public +)?\\$([^ =]+) *= *(.*)").matcher(line);
			if (m.matches()) {
				boolean isPublic = (m.group(1) != null);
				String ruleId = m.group(2);
				String matchStr = m.group(3);
				addRule(ruleId, isPublic, parseMatches(matchStr));
			}
		}
	}
	
	private List<Object> parseMatches(String matches) {
		List<String> groups = split(matches, "|");
		if (groups.size() > 1) {
			OneOf oneof = new OneOf();
			for (String gr : groups) {
				List<Object> grl = parseMatches(gr);
				if (grl.size() == 1) {
					oneof.add(grl.get(0));
				} else {
					oneof.add(new Group(grl));
				}
			}
			List<Object> result = new ArrayList<Object>();
			result.add(oneof);
			return result;
		}
		List<Object> result = new ArrayList<Object>();
		groups = split(matches, " ");
		if (groups.size() > 1) {
			for (String gr : groups) {
				List<Object> grl = parseMatches(gr);
				if (grl.size() == 1) {
					result.add(grl.get(0));
				} else {
					result.add(new Group(grl));
				}
			}
		} else {
			String group = groups.get(0);
			if (group.startsWith("(")) {
				List<Object> list = parseMatches(group.substring(1, group.length()-1));
				if (list.size() == 1)
					result.add(list.get(0));
				else
					result.add(new Group(list));
			} else if (group.startsWith("$")) {
				result.add(new RuleRef(group.substring(1)));
			} else if (group.startsWith("{")) {
				result.add(new Tag(group.substring(1, group.length()-1)));
			} else if (group.startsWith("[")) {
				List<Object> list = parseMatches(group.substring(1, group.length()-1));
				Group optional = new Group(list);
				optional.setRepeat(0, 1);
				result.add(optional);
			} else {
				result.add(group);
			}
		}
		return result;
	}

	private List<String> split(String string, String split) {
		List<String> result = new ArrayList<String>();
		String group = "";
		int para = 0;
		int cpara = 0;
		int spara = 0;
		for (int i = 0; i < string.length(); i++) {
			String c = string.substring(i, i+1);
			if (c.equals(split) && para == 0 && cpara == 0 && spara == 0) {
				result.add(group.trim());
				group = "";
			} else {
				if (c.equals("(")) {
					para++;
				} else if (c.equals(")")) {
					para--;
				} else if (c.equals("{")) {
					cpara++;
				} else if (c.equals("}")) {
					cpara--;
				} else if (c.equals("[")) {
					spara++;
				} else if (c.equals("]")) {
					spara--;
				}
				group += c;
			}
		}
		result.add(group.trim());
		return result;
	}

	private String matchToString(Object match) {
		if (isGroup(match)) {
			String content = matchToString(getMatches(match));
			int min = getMinRepeat(match);
			int max = getMaxRepeat(match);
			if (min == 1 && max == 1) {
				return "(" + content + ")";
			} else if (min == 0 && max == 1) {
				return "[" + content + "]";
			} else {
				return "(" + content + ") " + "<" + min + "-" + (max == INFINITY ? "" : max) + ">";
			}
		} else if (isOneOf(match)) {
			String result = "";
			boolean first = true;
			for (Object part : getMatches(match)) {
				if (!first) result += " | ";
				result += matchToString(part);
				first = false;
			}
			return "(" + result.trim() + ")";
		} else if (isRuleRef(match)) {
			return "$" + getRuleRef(match);
		} else if (isWord(match)) {
			return getWordString(match);
		} else if (isTag(match)) {
			return "{" + getTagScript(match) + "}";
		} else if (match instanceof List) {
			String result = "";
			for (Object part : (List)match) {
				result += matchToString(part) + " ";
			}
			return result.trim();
		} else {
			throw new RuntimeException("Cannot convert " + match);
		}
	}

	@Override
	public void marshal(OutputStream out) {
		PrintStream ps = new PrintStream(out);
		if (getLanguage() != null)
			ps.println("language " + getLanguage() + ";");
		if (getRoot() != null)
			ps.println("root $" + getRoot() + ";");
		ps.println();
		for (Object rule : getRules()) {
			if (isPublic(rule)) 
				ps.print("public ");
			ps.println("$" + getRuleId(rule) + " = " + matchToString(getMatches(rule)) + ";");
			ps.println();
		}
	}

	public static void main(String[] args) throws IOException {
		//ABNFGrammar.write(new SrgsGrammar(new File("src/iristk/app/chess/ChessGrammar.xml")), System.out);
		//ContextFreeGrammar cfg = new ABNFGrammar(new File("app/chess/src/iristk/app/chess/ChessGrammar.abnf"));
		//Grammar cfg = new SrgsGrammar(new ABNFGrammar(new File("app/chess/src/iristk/app/chess/ChessGrammar.abnf")));
		Grammar cfg = new ABNFGrammar(new SrgsGrammar(new File("app/chess/src/iristk/app/chess/ChessGrammar.xml")));
		cfg.marshal(System.out);
	}
}