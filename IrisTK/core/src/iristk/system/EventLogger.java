package iristk.system;

import iristk.util.NameFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EventLogger implements IrisMonitor {

	private OutputStream out;
	private NameFilter filter;

	public EventLogger(OutputStream out, NameFilter filter) {
		this.out = out;
		this.filter = filter;
	}
		
	public EventLogger(File file, NameFilter filter) throws FileNotFoundException {
		this.out = new FileOutputStream(file);
		this.filter = filter;
	}
	
	public void close() {
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void monitorEvent(String sender, Event event) {
		if (filter.accepts(event.getName())) {
			try {
				out.write(new String(event + "\n").getBytes());
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}

	@Override
	public void monitorState(String sender, String[] states) {
		// TODO Auto-generated method stub
	}
	
}
