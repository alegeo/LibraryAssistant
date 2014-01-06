package iristk.parser.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Chart {

	private HashMap<Integer,ArrayList<Edge>> chart = new HashMap<Integer,ArrayList<Edge>>(); 
	
	public Chart(Chart copyOf) {
		for (Integer key : copyOf.chart.keySet()) {
			chart.put(key, new ArrayList<Edge>(copyOf.chart.get(key)));
		}
	}

	public Chart() {
	}

	public void clear(Integer vertex) {
		if (chart.containsKey(vertex)) 
			chart.get(vertex).clear();
	}	
	
	public void put(Integer vertex, Edge edge) {
		if (!chart.containsKey(vertex)) {
			chart.put(vertex, new ArrayList<Edge>());
		}
		chart.get(vertex).add(edge);
	}
	
	public ArrayList<Edge> get(Integer vertex) {
		if (!chart.containsKey(vertex)) {
			chart.put(vertex, new ArrayList<Edge>());
		}
		return chart.get(vertex);
	}

	public Collection<Integer> getVertices() {
		return chart.keySet();
	}

	public void clear() {
		chart.clear();
	}

	public RuleEdge findRule(String ruleId) {
		for (Integer v : getVertices()) {
			for (Edge e : get(v)) {
				if (e instanceof RuleEdge) {
					RuleEdge rule = (RuleEdge)e;
					if (((RuleEdge) e).getRuleId().equals(ruleId)) {
						return rule;
					}
				}
			}
		}
		return null;
	}
	
}
