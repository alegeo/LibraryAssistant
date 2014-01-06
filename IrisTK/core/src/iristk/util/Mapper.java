package iristk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Mapper extends HashMap<String,String> {

	private String name;

	public Mapper(String name, InputStream inputStream) {
		this.name = name;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] cols = line.trim().split("\\s+");
				if (cols.length >= 2) {
					add(cols);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void add(String... items) {
		for (int i = 1; i < items.length; i++) {
			put(items[i], items[0]);
		}
	}

	public String map(String label) {
		if (containsKey(label)) {
			return get(label);
		} else {
			System.err.println("WARNING: " + name + " could not map '" + label + "'");
			return label;
		}
	}

}
