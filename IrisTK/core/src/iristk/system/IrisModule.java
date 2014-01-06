package iristk.system;

import iristk.util.NameFilter;

import java.util.concurrent.ArrayBlockingQueue;

public abstract class IrisModule implements Runnable, EventListener {

	private Thread thread;
	String moduleName;
	IrisSystem system;
	ArrayBlockingQueue<Event> eventQueue = new ArrayBlockingQueue<Event>(1000);
	NameFilter subscribes = NameFilter.ALL;
	private boolean running = false;
		
	public IrisModule() {
	}
	
	public void send(Event event) {
		system.send(event, moduleName);
	}
	
	public void monitorEvent(Event event) {
		system.monitorEvent(moduleName, event);
	}
	
	public void monitorState(String... states) {
		system.monitorState(moduleName, states);
	}
	
	public void subscribe(NameFilter filter) {
		subscribes = filter;
	}
	
	public void subscribe(String filter) {
		subscribes = NameFilter.compile(filter);
	}
	
	public void start() {
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public void stop() {
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return moduleName;
	}
	
	public void setName(String name) {
		this.moduleName = name;
	}
	
	public IrisSystem getSystem() {
		return system;
	}
	
	public void setSystem(IrisSystem system) {
		this.system = system;
	}
	
	@Override
	public void run() {
		while (running) {
			try {
				Event event = eventQueue.take();
				onEvent(event);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	/*
	 * Adds an event to the module's eventQueue so that it is processed in the module's own thread.
	 */
	public void invokeEvent(Event message) {
		eventQueue.add(message);
	}
	
	protected static boolean eq(Object s1, Object s2) {
		return (s1 == s2 || (s1 != null && s2 != null && s1.equals(s2)));
	}
	
	protected static boolean eqnn(Object s1, Object s2) {
		return (s1 != null && s2 != null && s1.equals(s2));
	}

	public abstract void init() throws InitializationException;
	
	//public Record getConfig() {
	//	return getSystem().getConfig(moduleName);
	//}
		
	@Override
	public String toString() {
		return moduleName;
	}
	
}
