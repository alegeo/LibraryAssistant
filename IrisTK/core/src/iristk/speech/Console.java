package iristk.speech;

import iristk.parser.cfg.ParseResult;
import iristk.parser.cfg.Parser;
import iristk.system.Event;
import iristk.system.InitializationException;
import iristk.system.IrisModule;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Console extends IrisModule {

	JTextPane textPane;
	private StyledDocument doc;
	private SimpleAttributeSet userStyle;
	private SimpleAttributeSet systemStyle;
	private String actionId;
	private Integer timeout;
	private String lastAction = "";
	private int lastPos = 0;
	private boolean startOfSpeech;
	private String location;
	private JTextField textInput;
	private Parser parser;
	private boolean synthesizer = false;
	private boolean recognizer = false;
	
	public Console() {
	}
	
	public void useSynthesizer(boolean cond) {
		this.synthesizer = cond;
	}

	public void useRecognizer(boolean cond) {
		this.recognizer = cond;
	}
	
	public void setLocation(String location) {
		if (location == null)
			this.location = "default";
		else
			this.location = location;
	}
	
	@Override
	public void init() throws InitializationException {
		JFrame window = new JFrame();
		textPane = new JTextPane();
		textPane.setEditable(false);
		doc = textPane.getStyledDocument();
		userStyle = new SimpleAttributeSet();
		systemStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(userStyle, Color.BLUE);
		StyleConstants.setForeground(systemStyle, Color.RED);

		window.add(new JScrollPane(textPane));
		
		textInput = new JTextField();
		textInput.setEditable(false);
		textInput.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent key) {	
			}
			@Override
			public void keyReleased(KeyEvent key) {
			}
			@Override
			public void keyPressed(KeyEvent key) {
				if (key.getKeyCode() == 10) {
					sendSpeech(textInput.getText());
					textInput.setText("");
					textInput.setEditable(false);
				} else if (!startOfSpeech) {
					startOfSpeech();
				}
			}
		});
		
		window.add(textInput, BorderLayout.PAGE_END);
		
		textPane.setPreferredSize(new Dimension(500,500));
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}

	private void startOfSpeech() {
		startOfSpeech = true;
		Event speech = new Event("sense.speech.start");
		speech.put("location", location);
		speech.put("action", actionId);
		send(speech);
	}

	private void sendSpeech(String text) {
		if (text.length() > 0) {
			Event end = new Event("sense.speech.end");
			end.put("location", location);
			end.put("action", actionId);
			send(end);
			Event rec = new Event("sense.speech.rec.final");
			rec.put("text", text);
			rec.put("location", location);
			if (parser != null) {
				ParseResult presult = parser.parse(text);
				rec.put("sem", presult.getSem());
			}
			rec.put("action", actionId);
			send(rec);
		} else {
			Event speech = new Event("sense.speech.rec.nospeech");
			speech.put("location", location);
			speech.put("action", actionId);
			send(speech);
		}
	}

	@Override
	public void onEvent(final Event event) {
		if (event.getName().equals("action.speech")) {
			
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						String text = event.getString("text");
						text = text.replaceAll("<.*?>", "");
						textPane.setParagraphAttributes(systemStyle, true);
						doc.insertString(doc.getLength(), text + "\n", null);
						textPane.setCaretPosition(doc.getLength());
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			});

			if (!synthesizer) {
				Event speech = new Event("monitor.speech.end");
				speech.put("action", event.getId());
				send(speech);
			}
			
		} else if (event.getName().equals("sense.speech.rec.final")) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						if (event.get("action").equals(lastAction)) 
							doc.remove(lastPos, doc.getLength() - lastPos);
						lastPos = doc.getLength();
 						lastAction = event.getString("action");
						String text = event.getString("text");
						text = text.replaceAll("<.*?>", "");
						textPane.setParagraphAttributes(userStyle, true);
						doc.insertString(doc.getLength(), text + "\n", null);
						textPane.setCaretPosition(doc.getLength());
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			});
		} else if (event.getName().equals("sense.speech.rec.partial")) {
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {
						if (event.get("action").equals(lastAction)) 
							doc.remove(lastPos, doc.getLength() - lastPos);
						lastPos = doc.getLength();
 						lastAction = event.getString("action");
						String text = event.getString("text");
						text = text.replaceAll("<.*?>", "");
						textPane.setParagraphAttributes(userStyle, true);
						doc.insertString(doc.getLength(), text + "\n", null);
						textPane.setCaretPosition(doc.getLength());
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			});
		} else if (event.getName().equals("action.listen")) {
			if (!recognizer) {
				textInput.setEditable(true);
				actionId = event.getId();
				timeout = event.getInt("timeout");
				startOfSpeech = false;
			}
		}
	}

	public void setParser(Parser parser) {
		this.parser = parser;
	}

}
