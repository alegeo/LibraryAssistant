package iristk.app.quiz;

import iristk.util.RandomList;

import java.io.*;
import java.util.*;

public class QuestionSet extends ArrayList<Question> {

	private int n = 0;
	
	public QuestionSet() {
		this(QuestionSet.class.getResourceAsStream("questions.txt"));
	}
	
	public QuestionSet(InputStream questionFile) {
		try {
			List<Integer> order = new ArrayList<>();
			order.add(1);
			order.add(2);
			order.add(3);
			order.add(4);
			BufferedReader br = new BufferedReader(new InputStreamReader(questionFile));
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] cols = line.split(";");
				if (cols.length != 6) {
					System.err.println("Cannot parse: " + line);
				} else {
					RandomList.shuffle(order);
					Question q = new Question();
					q.put("question", cols[0].trim());
					q.put("answer" + order.get(0), cols[1].trim());
					q.put("answer" + order.get(1), cols[2].trim());
					q.put("answer" + order.get(2), cols[3].trim());
					q.put("answer" + order.get(3), cols[4].trim());
					q.put("correct", "answer" + order.get(0));
					q.put("category", cols[5].trim());
					add(q);
				}
			}
			randimize();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void randimize() {
		RandomList.shuffle(this);
	}
	
	public Question next() {
		Question q = get(n);
		n++;
		if (n >= size()) {
			n = 0;
		}
		return q;
	}
	
}
