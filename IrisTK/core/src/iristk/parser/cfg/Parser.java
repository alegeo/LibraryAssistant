package iristk.parser.cfg;

import iristk.speech.RecognitionResult;
import iristk.speech.RecResultProcessor;
import iristk.util.Record;
import iristk.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;

public class Parser implements RecResultProcessor {

	private Stack<Edge> agenda = new Stack<Edge>();
	private Chart activeChart = new Chart();
	private Chart passiveChart = new Chart();
	private List<Grammar> grammars = new ArrayList<Grammar>();

	private static HashMap<String,ABNFGrammar> cachedGrammars = new HashMap<>();

	public boolean onlyPublicRules = false;
	private List<Word> words;
	private State state;
	
	public Parser() {
	}
	
	private class State {
		private Stack<Edge> agenda;
		private Chart activeChart;
		private Chart passiveChart;
	}
		
	private void saveState() {
		state = new State();
		state.agenda = new Stack<Edge>();
		state.agenda.addAll(agenda);
		state.activeChart = new Chart(activeChart);
		state.passiveChart = new Chart(passiveChart);
	}
	
	private void restoreState() {
		agenda = new Stack<Edge>();
		agenda.addAll(state.agenda);
		activeChart = new Chart(state.activeChart);
		passiveChart = new Chart(state.passiveChart);
	}
	
	private void combine(Edge active, Edge passive) {
		List<Edge> newEdges = active.matches(passive);
		if (newEdges != null) {
			for (Edge newEdge : newEdges) {
				agenda.add(newEdge);
			}
		}
	}
	
	private void processAgenda() {
		while (!agenda.isEmpty()) {
			Edge edge = agenda.pop();
			if (edge.isPassive()) {
				ArrayList<Edge> activeEdges = activeChart.get(edge.getBegin());
				int i = 0;
				while (i < activeEdges.size()) {
					Edge active = activeEdges.get(i);
					combine(active, edge);
					i++;
				}
				//System.out.println(edge);
				passiveChart.put(edge.getBegin(), edge);
			} else {
				for (Edge passive : passiveChart.get(edge.getEnd())) {
					combine(edge, passive);
				}
				activeChart.put(edge.getEnd(), edge);
			}
		} 
	}
	
	private void initRules(Grammar grammar, boolean putOnAgenda) {
		for (Object rule : grammar.getRules()) {
			for (int i = 0; i < words.size(); i++) {
				RuleEdge edge = new RuleEdge(i, grammar, rule);
				activeChart.put(i, edge);
				if (putOnAgenda) {
					agenda.push(edge);
				}
			}
 		}
	}
	
	private void initWords(List<Word> words) {
		this.words = words;
		activeChart.clear();
		passiveChart.clear();
		agenda.clear();
		for (int i = 0; i < words.size(); i++) {
			agenda.push(new WordEdge(i, words.get(i)));
		}
	}

	private List<Edge> findBestEdges(Integer start, Integer end) {
		Graph<Integer,Edge> edgeGraph = new DirectedSparseMultigraph<Integer,Edge>();
		for (Integer v : passiveChart.getVertices()) {
			for (Edge e : passiveChart.get(v)) {
				if (!onlyPublicRules || e instanceof WordEdge || (e instanceof RuleEdge && ((RuleEdge)e).isPublic())) {
					edgeGraph.addEdge(e, e.getBegin(), e.getEnd());
				}
			}
		}
		DijkstraShortestPath<Integer,Edge> shortestPath = new DijkstraShortestPath<Integer,Edge>(edgeGraph, new EdgeWeightCalculator(), true);
		List<Edge> topEdges = shortestPath.getPath(start, end);
		return topEdges;
	}

	private ParseResult findBestResult(Integer start, Integer end) {
		List<Edge> topEdges = findBestEdges(start, end);
		ParseResult result = new ParseResult();
		ParseResult.Phrase wordPhrase = null;
		for (Edge e : topEdges) {
			if (e instanceof RuleEdge) {
				wordPhrase = null;
				result.add(new ParseResult.Phrase(((RuleEdge)e).getRuleId(), 
						((RuleEdge)e).getSem(), 
						e.getWords()));
			} else if (e instanceof WordEdge) {
				if (wordPhrase == null) {
					wordPhrase = new ParseResult.Phrase();
					result.add(wordPhrase);
				}
				wordPhrase.getWords().add(((WordEdge)e).getWord());
			}
		}
		return result;
	}
	
	private class EdgeWeightCalculator implements Transformer<Edge, Number> {

		@Override
		public Number transform(Edge e) {
			return (1 + (e.getEnd() - e.getBegin())) - ((e.getLevel()) / 100.0f);
		}

	}
	
	private static List<Word> tokenize(String text) {
		ArrayList<Word> words = new ArrayList<Word>();
		for (String w : text.split(" ")) {
			words.add(new Word(w));
		}
		return words;
	}
	
	public ParseResult parse(List<Word> words) {
		initWords(words);
		for (Grammar grammar : grammars) {
			initRules(grammar, false);
		}
		processAgenda();
		saveState();
		return findBestResult(0, words.size());
	}

	public ParseResult parse(String text) {
		return parse(tokenize(text));
	}
	
	public ParseResult parse(Word... words) {
		ArrayList<Word> wl = new ArrayList<Word>();
		for (Word w : words) {
			wl.add(w);
		}
		return parse(wl);
	}

	public static ABNFGrammar parseGrammarString(String text) throws IOException {
		if (cachedGrammars.containsKey(text)) {
			return cachedGrammars.get(text);
		} else {
			ABNFGrammar grammar = new ABNFGrammar(new ByteArrayInputStream(text.getBytes()));
			cachedGrammars.put(text, grammar);
			return grammar;
		}
	}
	
	public String find(String pattern, String text) {
		initWords(tokenize(text));
		for (Grammar grammar : grammars) {
			initRules(grammar, false);
		}
		try {
			ABNFGrammar grammar = parseGrammarString("$findRuleX = " + pattern);
			initRules(grammar, false);
			saveState();
			processAgenda();
			RuleEdge rule = passiveChart.findRule("findRuleX");
			if (rule != null) {
				return Utils.listToString(rule.getWords());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String find(String pattern) {
		restoreState();
		try {
			ABNFGrammar grammar = parseGrammarString("$findRuleX = " + pattern);
			initRules(grammar, true);
			processAgenda();
			RuleEdge rule = passiveChart.findRule("findRuleX");
			if (rule != null) {
				return Utils.listToString(rule.getWords());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void clearGrammars() {
		grammars.clear();
	}
	
	public void addGrammar(Grammar grammar) {
		grammars.add(grammar);
	}
	
	public void removeGrammar(Grammar grammar) {
		grammars.remove(grammar);
	}
	
	@Override
	public RecognitionResult processResult(RecognitionResult result) {
		if (result != null) {
			if ((result.isFinal() || result.isPartial())) {
				//TODO: parse the individual words and assign confidence to concepts
				if (result.getText() != null) {
					Record sem = parse(result.getText()).getSem();
					if (result.getSem() == null || result.getSem().size() == 0) {
						result.setSem(sem);
					}
					result.put("parser", this);
				}
			}
		}
		return result;
	}

	@Override
	public void processSpeech(byte[] samples, int pos, int len) {
	}

	@Override
	public void initRecognition() {
	}
	
	@Override
	public String toString() {
		return "Parser";
	}
	
	public static void main(String args[]) throws IOException {
		Parser parser = new Parser();
		//parser.addGrammar(new SrgsGrammar(new File("src/iristk/app/chess/ChessGrammar.xml")));
		parser.addGrammar(new ABNFGrammar(new File("app/chess/src/iristk/app/chess/ChessGrammar.abnf")));
		//ParseResult result = parser.parse("the left knight");
		//System.out.println(result.getSem());
		System.out.println(parser.parse("move the knight two steps"));
		System.out.println(parser.find("move the ($piece)"));
		//System.out.println(parser.group(0));
	}

	@Override
	public boolean willTakeTime(RecognitionResult result) {
		return false;
	}
	
	private static class NomatchParser extends Parser {
		
		@Override
		public String find(String pattern) {
			return null;
		}
		
	}

}

