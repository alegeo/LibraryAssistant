package iristk.speech;

import iristk.audio.Sound;
import iristk.audio.SoundPlayer;
import iristk.speech.util.NISTAlign;
import iristk.util.ArgParser;
import iristk.util.Language;

import java.io.*;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;

public abstract class BatchRec {

	private boolean playback = false;
	private SoundPlayer soundPlayer = null;

	protected abstract Recognizer getRecognizer();

	public void run(File batchset, File audioPath, OutputStream... outs) throws IOException {
		ArrayList<PrintStream> printStreams = new ArrayList<PrintStream>();
		for (OutputStream out : outs) {
			printStreams.add(new PrintStream(out));
		}
		BufferedReader br = new BufferedReader(new FileReader(batchset));
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim().replaceAll("  +", " ");
			if (line.length() > 0) {
				String filename, ref = null;
				String cols[] = line.split("\t");
				if (cols.length == 2) {
					filename = cols[0];
					ref = cols[1];
				} else {
					filename = line;
				}
				File audioFile = new File(audioPath, filename);
				if (playback && audioFile.exists()) {
					Sound sound;
					try {
						sound = new Sound(audioFile);
						if (soundPlayer == null) {
							soundPlayer = new SoundPlayer(sound.getAudioFormat());
						}
						soundPlayer.playAsync(sound);
					} catch (UnsupportedAudioFileException e1) {
						e1.printStackTrace();
					}
				}
				for (PrintStream ps : printStreams) {
					if (ref == null) {
						ps.print(filename + "\t");
					} else {
						ps.print(filename + "\t" + ref + "\t");
					}
				}
				if (!audioFile.exists()) {
					for (PrintStream ps : printStreams) {
						ps.println("ERROR: could not find file");
					}
				} else {
					String rstring = "";
					try {
						RecognitionResult result = getRecognizer().recognizeFile(audioFile);
						rstring = result.getText();
					} catch (RecognizerException e) {
						rstring = "ERROR: " + e.getMessage(); 
					} 
					for (PrintStream ps : printStreams) {
						ps.println(rstring);
					}
				}
				if (playback && audioFile.exists()) {
					soundPlayer.waitForPlayingDone();
				}
			}
		}
	}


	public void run(String[] args) throws IOException {
		ArgParser argParser = new ArgParser();
		argParser.addArg("b", "Batch file", "filename", false);
		argParser.addArg("g", "Grammar file", "filename", false);
		argParser.addArg("a", "Audio file path", "path", false);
		argParser.addArg("r", "Result file", "filename", false);
		argParser.addArg("l", "Language", "lang", false);
		argParser.addArg("p", "Playback", false);
		argParser.addArg("e", "Evaluate", false);
		if (!argParser.parse(args)) {
			System.out.println(argParser.help());
			System.exit(0);
		}
		if (argParser.has("l")) {
			getRecognizer().setLanguage(Language.fromCode(argParser.get("l")));
		}
		if (argParser.has("g")) {
			try {
				getRecognizer().loadGrammar("default", argParser.get("g"));
				getRecognizer().activateGrammar("default", 1);
			} catch (RecognizerException e) {
				e.printStackTrace();
			}
		}
		playback = argParser.has("p");
		if (argParser.has("b")) {
			File batchSet = new File(argParser.get("b"));
			File audioPath = new File(".");
			if (argParser.has("a")) {
				audioPath = new File(argParser.get("a"));
			}
			if (argParser.has("r")) {
				FileOutputStream out = new FileOutputStream(new File(argParser.get("r")));
				run(batchSet, audioPath, System.out, out);
				out.close();
			} else {
				run(batchSet, audioPath, System.out);
			}		
		}
		if (argParser.has("e") && argParser.has("r")) {
			evaluate(new File(argParser.get("r")));
		}
	}

	public static void evaluate(File resultSet) {
		NISTAlign align = new NISTAlign(true, true);

		try {
			BufferedReader batchFile = new BufferedReader(new FileReader(resultSet));
			String line;
			try {
				while ((line = batchFile.readLine()) != null) {
					String[] cols = line.trim().split("\t");
					if (cols.length == 3) {
						if (!cols[2].startsWith("ERROR")) { 
							align.align(cols[1], cols[2]);
							align.printNISTSentenceSummary();
						}
					}
				}
			} catch (java.io.IOException e) {
			}
			align.printNISTTotalSummary();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
