package iristk.speech;

import iristk.audio.SoundPlayer;
import iristk.system.InitializationException;
import iristk.system.IrisUtils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class TestSynth {

	JTextArea textField;
	SoundPlayer player;
	SynthesizerModule currentSynth;
	private HashMap<String,SynthesizerModule> synthmap = new HashMap<String,SynthesizerModule>();
	private JComboBox<String> synthList;
	private JComboBox<String> voiceList;
	private HashMap<String,Voice> voices = new HashMap<String,Voice>();
	private boolean updating = false;

	public TestSynth() throws FileNotFoundException {
		JFrame window = new JFrame("Test Synthesizer");

		ArrayList<String> synthesizers = new ArrayList<>();
		synthesizers.add("");
		for (iristk.xml._package.Package pack : IrisUtils.getPackages()) {
			if (pack.getProvide() != null) {
				for (iristk.xml._package.Package.Provide.Class clazz : pack.getProvide().getClazz()) {
					if (clazz.getType().equals("iristk.speech.Synthesizer")) {
						synthesizers.add(clazz.getName());
					}
				}
			}
		}

		synthList = new JComboBox<String>(synthesizers.toArray(new String[0]));
		voiceList = new JComboBox<String>();
		synthList.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!updating) setSynth();
			}
		});
		voiceList.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!updating) setVoice();
			}
		});

		textField = new JTextArea();
		textField.setWrapStyleWord(true);

		JButton playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = textField.getText().trim();
				sayAsync(text);
			}
		});

		JPanel top = new JPanel();
		top.add(synthList, BorderLayout.PAGE_START);
		top.add(voiceList, BorderLayout.PAGE_END);
		window.add(top, BorderLayout.PAGE_START);
		window.add(textField, BorderLayout.CENTER);
		window.add(playButton, BorderLayout.PAGE_END);

		window.setPreferredSize(new Dimension(800,600));
		window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	protected void setVoice() {
		try {
			currentSynth.getSynthesizer().setVoice(voices.get(voiceList.getSelectedItem()));
		} catch (InitializationException e) {
			e.printStackTrace();
		}
	}

	protected void setSynth() {
		updating  = true;
		try {
			String synth = (String) synthList.getSelectedItem();
			if (!synth.equals("")) {
				if (!synthmap.containsKey(synth)) {
					SynthesizerModule sm = new SynthesizerModule((Synthesizer)Class.forName(synth).newInstance());
					sm.init();
					synthmap.put(synth, sm);
					currentSynth = sm;
				} else {
					currentSynth = synthmap.get(synth);
				}
				voiceList.removeAllItems();
				voices.clear();
				for (Voice voice : currentSynth.getSynthesizer().getVoices()) {
					voiceList.addItem(voice.getName());
					voices.put(voice.getName(), voice);
				}
				voiceList.setSelectedIndex(0);
				setVoice();
			}
		} catch (Exception e) {
			System.out.println("Error initializing " + synthList.getSelectedItem() + ": " + e.getMessage());
			synthList.setSelectedItem("");
			voiceList.removeAllItems();
			currentSynth = null;
		}
		updating = false;
	}

	public void sayAsync(String text) {
		if (currentSynth != null) {
			currentSynth.stopSpeaking();
			currentSynth.startSpeaking(text);
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		new TestSynth();
	}

}
