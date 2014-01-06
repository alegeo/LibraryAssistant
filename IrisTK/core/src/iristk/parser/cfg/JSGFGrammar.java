package iristk.parser.cfg;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

public class JSGFGrammar {
	
	public static int INFINITY = Integer.MAX_VALUE;

	public static void write(Grammar cfg, OutputStream out) {
		PrintStream ps = new PrintStream(out);
		ps.println("#JSGF V1.0;");
		//if (cfg.getLanguage() != null)
		//	ps.println("language " + cfg.getLanguage() + ";");
		//if (cfg.getRoot() != null)
		//	ps.println("root $" + cfg.getRoot() + ";");
		ps.println();
		ps.println("grammar name;");
		ps.println();
		for (Object rule : cfg.getRules()) {
			if (cfg.isPublic(rule)) 
				ps.print("public ");
			ps.println("<" + cfg.getRuleId(rule) + "> = " + matchToString(cfg, cfg.getMatches(rule)) + ";");
			ps.println();
		}
	}

	private static String matchToString(Grammar cfg, Object match) {
		if (cfg.isGroup(match)) {
			String content = matchToString(cfg, cfg.getMatches(match));
			int min = cfg.getMinRepeat(match);
			int max = cfg.getMaxRepeat(match);
			if (min == 1 && max == 1) {
				return "(" + content + ")";
			} else if (min == 0 && max == 1) {
				return "[" + content + "]";
			} else {
				return "(" + content + ") " + "<" + min + "-" + (max == INFINITY ? "" : max) + ">";
			}
		} else if (cfg.isOneOf(match)) {
			String result = "";
			boolean first = true;
			for (Object part : cfg.getMatches(match)) {
				if (!first) result += " | ";
				result += matchToString(cfg, part);
				first = false;
			}
			return "(" + result.trim() + ")";
		} else if (cfg.isRuleRef(match)) {
			return "<" + cfg.getRuleRef(match) + ">";
		} else if (cfg.isWord(match)) {
			return cfg.getWordString(match);
		} else if (cfg.isTag(match)) {
			//return "{" + cfg.getTagScript(match) + "}";
			return "";
		} else if (match instanceof List) {
			String result = "";
			for (Object part : (List)match) {
				result += matchToString(cfg, part) + " ";
			}
			return result.trim();
		} else {
			throw new RuntimeException("Cannot convert " + match);
		}
	}
	
	public static void main(String[] args) throws Exception {
		ABNFGrammar abnf = new ABNFGrammar(new File("app/chess/ChessGrammar.abnf"));
		JSGFGrammar.write(abnf, System.out);
	}
	
}
