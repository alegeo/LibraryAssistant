package iristk.speech;

import iristk.system.IrisUtils;
import iristk.util.Language;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class TestRecognizer implements RecognizerListener {

	JTextArea grammarField;
	Recognizer currentRec;
	private HashMap<String,Recognizer> synthmap = new HashMap<>();
	private JComboBox<String> recList;
	private JComboBox<String> langList;
	private HashMap<String,Language> languages = new HashMap<>();
	private boolean updating = false;
	private HashMap<Recognizer,HashSet<String>> loadedGrammars = new HashMap<>();
	private JTextArea resultField;
	private String grammarCode;
	private JButton listenButton;

	public TestRecognizer() throws FileNotFoundException {
		JFrame window = new JFrame("Test Recognizer");

		ArrayList<String> recognizers = new ArrayList<>();
		recognizers.add("");
		for (iristk.xml._package.Package pack : IrisUtils.getPackages()) {
			if (pack.getProvide() != null) {
				for (iristk.xml._package.Package.Provide.Class clazz : pack.getProvide().getClazz()) {
					if (clazz.getType().equals("iristk.speech.Recognizer")) {
						recognizers.add(clazz.getName());
					}
				}
			}
		}

		recList = new JComboBox<String>(recognizers.toArray(new String[0]));
		langList = new JComboBox<String>();
		recList.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!updating) setRec();
			}
		});
		langList.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!updating) setLang();
			}
		});
		
		grammarField = new JTextArea();
		grammarField.setFont(new Font("Courier", Font.PLAIN, 12));
		grammarField.setTabSize(3);
		grammarField.setWrapStyleWord(true);
		grammarField.setText("<grammar xml:lang=\"en-US\" version=\"1.0\" root=\"srgs_root\"\n" + 
"	xmlns=\"http://www.w3.org/2001/06/grammar\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
"	xsi:schemaLocation=\"http://www.w3.org/2001/06/grammar http://www.w3.org/TR/speech-grammar/grammar.xsd\" tag-format=\"semantics/1.0\">\n" + 
"\n" + 	
"	<rule id=\"srgs_root\" scope=\"public\">\n" + 
"		<ruleref uri=\"#fruit\"/>\n" + 
"		<tag>out.fruit=rules.fruit</tag>\n" + 
"	</rule>\n" + 
"	\n" + 
"	<rule id=\"fruit\">\n" + 
"		<one-of>\n" + 
"			<item>banana</item>\n" + 
"			<item>orange</item>\n" + 
"			<item>apple</item>\n" + 
"		</one-of>\n" + 
"	</rule>\n" + 
"	\n" + 
"</grammar>");
		
		resultField = new JTextArea();
		resultField.setFont(new Font("Courier", Font.PLAIN, 12));
		resultField.setTabSize(3);
		resultField.setWrapStyleWord(true);

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, grammarField, resultField);
		
		listenButton = new JButton("Listen");
		listenButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String grammar = grammarField.getText().trim();
				listen(grammar);
			}
		});

		JPanel top = new JPanel();
		top.add(recList, BorderLayout.PAGE_START);
		top.add(langList, BorderLayout.PAGE_END);
		window.add(top, BorderLayout.PAGE_START);
		window.add(split, BorderLayout.CENTER);
		window.add(listenButton, BorderLayout.PAGE_END);

		window.setPreferredSize(new Dimension(800,600));
		window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	protected void setLang() {
		
	}

	protected void setRec() {
		updating  = true;
		try {
			String synth = (String) recList.getSelectedItem();
			if (!synth.equals("")) {
				if (!synthmap.containsKey(synth)) {
					Recognizer rec = (Recognizer)Class.forName(synth).newInstance();
					synthmap.put(synth, rec);
					loadedGrammars.put(rec, new HashSet<String>());
					rec.addRecognizerListener(this);
					currentRec = rec;
				} else {
					currentRec = synthmap.get(synth);
				}
				langList.removeAllItems();
				languages.clear();
				/*
				for (Voice voice : currentRec.getRecognizer().getLanguages()) {
					langList.addItem(voice.getName());
					languages.put(voice.getName(), voice);
				}
				langList.setSelectedIndex(0);
				setLang();
				*/
			}
		} catch (Exception e) {
			System.out.println("Error initializing " + recList.getSelectedItem() + ": " + e.getMessage());
			recList.setSelectedItem("");
			langList.removeAllItems();
			currentRec = null;
		}
		updating = false;
	}

	public void listen(String grammar) {
		try {
			listenButton.setEnabled(false);
			if (currentRec != null) {
				grammarCode = "g" + grammar.hashCode();
				if (!loadedGrammars.get(currentRec).contains(grammar)) {
					currentRec.loadGrammar(grammarCode, grammar);
					loadedGrammars.get(currentRec).add(grammar);
				}
				currentRec.activateGrammar(grammarCode, 1.0f);
				currentRec.startListen();
			}
		} catch (RecognizerException e) {
			e.printStackTrace();
			listenButton.setEnabled(true);
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		new TestRecognizer();
	}

	@Override
	public void startOfSpeech(float timestamp) {
		System.out.println("Start of speech");
	}

	@Override
	public void endOfSpeech(float timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void speechSamples(byte[] samples, int pos, int len) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recognitionResult(RecognitionResult result) {
		resultField.setText(result.toStringIndent());
		try {
			currentRec.deactivateGrammar(grammarCode);
		} catch (RecognizerException e) {
			e.printStackTrace();
		}
		listenButton.setEnabled(true);
	}

}
