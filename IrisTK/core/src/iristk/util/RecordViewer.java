package iristk.util;

import javax.swing.JFrame;

public class RecordViewer extends JFrame {

	public RecordViewer(Record record) {
		add(new RecordTreeView(record));
		pack();
	}

}
