package iristk.speech;

import iristk.xml.phones.Phones;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

public class Transcription extends Phones {

	public static HashSet<String> UPS = new HashSet<String>();
	static {
		UPS.addAll(Arrays.asList(new String[] {"P", "B", "M", "BB", "PH",
				"BH", "MF", "F", "V", "VA", "TH", "DH", "T", "D", "N", "RR",
				"DX", "S", "Z", "LSH", "LH", "RA", "L", "Lvel", "SH", "SHpal",
				"ZH", "ZHpal", "TR", "DR", "NR", "DXR", "SR", "ZR", "R", "LR",
				"RRrho", "CT", "JD", "NJ", "C", "CJ", "J", "LJ", "W", "K", "G",
				"NG", "X", "GH", "GA", "GL", "QT", "QD", "QN", "QQ", "QH",
				"RH", "HH", "HG", "GT", "H", "WJ", "PF", "TS", "CH", "JH",
				"JJ", "DZ", "CC", "TSR", "JC", "I", "Y", "IX", "YX", "UU", "U",
				"IH", "YH", "UH", "E", "EU", "EX", "OX", "OU", "O", "AX",
				"AXrho", "EH", "OE", "ER", "ERrho", "UR", "AH", "AO", "AE",
				"AEX", "A", "AOE", "AA", "Q", "EI", "AU", "OI", "AI", "IYX",
				"UYX", "EHX", "UWX", "OWX", "AOX", "_s"}));
	}

	public Transcription() {
	}

	public Transcription(Phones phones) {
		getPhone().addAll(phones.getPhone());
	}

	// String should be line separated phone labels
	Transcription(String labelString) {
		String[] lines = labelString.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String[] label = lines[i].split(" ");
			if (label.length != 3) {
				System.out
						.println("ERROR: Label String was not compatible, did not find 3 values at line"
								+ i);
				i = lines.length;
			}
			add(label[0], Float.valueOf(label[1]), Float.valueOf(label[2]));
		}
	}

	// String should be line separated phone labels
	Transcription(File labelFile) throws IOException {
		String labelString = "";
		BufferedReader in = new BufferedReader(new FileReader(labelFile));

		String line = in.readLine();
		while (line != null) {
			labelString += line + "\n";
			line = in.readLine();
		}
		in.close();

		String[] lines = labelString.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String[] label = lines[i].split(" ");
			if (label.length != 3) {
				System.out
						.println("ERROR: Label String was not compatible, did not find 3 values at line"
								+ i);
				i = lines.length;
			}
			add(label[0], Float.valueOf(label[1]), Float.valueOf(label[2]));
		}
	}

	public int length() {
		float length = 0.0f;
		for (Phone p : getPhone()) {
			if (!p.getName().equals("sil")) {
				length = p.getEnd();
			}
		}
		return (int) (length * 1000f);
	}

	@Override
	public String toString() {
		String output = "{";
		for (Phone p : getPhone()) {
			output += " {" + String.format(Locale.US, "%.3f", p.getStart())
					+ " " + String.format(Locale.US, "%.3f", p.getEnd()) + " "
					+ p.getName() + "}";
		}
		return output + " }";
	}

	public void add(String name, float start, float end) {
		Phone p = new Phone();
		if (!UPS.contains(name)) {
			System.err.println("WARNING: '" + name + "' is not a UPS phoneme");
		}
		p.setName(name);
		p.setStart(start);
		p.setEnd(end);
		getPhone().add(p);
	}

}
