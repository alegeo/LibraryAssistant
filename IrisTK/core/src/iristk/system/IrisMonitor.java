package iristk.system;

public interface IrisMonitor {

	void monitorEvent(String sender, Event event);
	
	void monitorState(String sender, String[] states);

}
