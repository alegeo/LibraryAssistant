/*
 * Copyright 2009-2010 Gabriel Skantze.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */
package iristk.app.chess;

import iristk.flow.Flow;
import iristk.flow.FlowModule;
import iristk.parser.cfg.Parser;
import iristk.parser.cfg.SrgsGrammar;
import iristk.speech.Console;
import iristk.speech.RecognizerModule;
import iristk.speech.SynthesizerModule;
import iristk.system.Event;
import iristk.system.IrisMonitorGUI;
import iristk.system.IrisSystem;
import iristk.system.EventLogger;
import iristk.system.IrisUtils;
import iristk.util.NameFilter;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Properties;

public class ChessSystem implements GameListener {

	private static final String CHESS_PACKAGE = "chess";
	private ChessGame chess;
	private FlowModule flowModule;

	private RecognizerModule asr;
	private SynthesizerModule tts;
	private Console console;

	@Override
	public void tentativeMove(Move move) {
	}

	@Override
	public void move(Move move) {
		// The system made a move, inform the flow
		flowModule.invokeEvent(new Event("chess.move.system"));
	}

	@Override
	public void gameRestart() {
		flowModule.invokeEvent(new Event("chess.restart"));
	}
	
	public ChessSystem() {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(new File(IrisUtils.getPackagePath(CHESS_PACKAGE), "chess.properties")));

			IrisSystem iris = new IrisSystem("chess");
			
			new IrisMonitorGUI(iris);
			// Print all events in the system
			iris.addMonitor(new EventLogger(System.out, NameFilter.ALL));

			chess = new ChessGame();
			// Listen for events in the chess game
			chess.chessWindow.board.gameListener = this;
			chess.chessWindow.gameListener = this;

			Flow flow = new ChessFlow();
			// Give the flow access to the chess game
			flow.setVariable("chess", chess);
			flowModule = new FlowModule(flow);
			iris.addModule("flow", flowModule);

			if (prop.getProperty("console").equalsIgnoreCase("true")) {
				console = new Console();
			}

			if (prop.getProperty("asr") != null) {
				asr = new RecognizerModule(prop.getProperty("asr"));
			}

			URI chessGrammar = getClass().getResource("ChessGrammar.xml").toURI();

			if (asr != null) {
				iris.addModule("asr", asr);
				if (console != null) 
					console.useRecognizer(true);
			} else if (console != null) {
				Parser parser = new Parser();
				parser.addGrammar(new SrgsGrammar(chessGrammar));
				console.setParser(parser);
			}

			if (prop.getProperty("tts") != null) {
				tts = new SynthesizerModule(prop.getProperty("tts"));
				iris.addModule("tts", tts);
				if (console != null)
					console.useSynthesizer(true);
			}

			if (console != null)
				iris.addModule("console", console);

			if (asr != null) {
				asr.loadGrammar("chess", chessGrammar);
				asr.setDefaultGrammars("chess");
			}
			
			chess.start();
			iris.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new ChessSystem();
	}

}
