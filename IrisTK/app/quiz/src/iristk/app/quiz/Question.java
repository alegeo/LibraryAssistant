package iristk.app.quiz;

import iristk.util.Record;

public class Question extends Record {

	private static int idCounter = 0;
	private String id;
	
	public Question() {
		this.id = "q" + idCounter++;
	}
	
	public String getFullQuestion() {
		return get("question") + " " + get("answer1") + ", " + get("answer2") + ", " + get("answer3") + ", or " + get("answer4");
	}
	
	public String getGrammar() {
		StringBuilder grammar = new StringBuilder();
		//"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
		grammar.append("<grammar xml:lang=\"en-US\" version=\"1.0\" root=\"root\" xmlns=\"http://www.w3.org/2001/06/grammar\" tag-format=\"semantics/1.0\">");
		grammar.append("<rule id=\"root\" scope=\"public\"><one-of>");
		for (int i = 1; i <= 4; i++) {
			grammar.append("<item>" + get("answer" + i) + "<tag>out.answer=\"answer" + i + "\"</tag></item>");
		}
		grammar.append("</one-of></rule></grammar>");	
		return grammar.toString();
	}
	
	public String getId() {
		return id;
	}
	
	
}
