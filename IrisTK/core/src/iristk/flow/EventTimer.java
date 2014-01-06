package iristk.flow;

import iristk.system.Event;

import java.util.Random;

public class EventTimer {

	Random rand = new Random();
	private int timeout;
	private String eventName;
	private FlowRunner flowRunner;
	private boolean running;
	private TimerThread timerThread;
	
	public EventTimer(FlowRunner flowRunner, int timeout, String event) {
		this.timeout = timeout;
		this.eventName = event;
		this.flowRunner = flowRunner;
		this.timerThread = new TimerThread();
		timerThread.start();
	}
		
	private class TimerThread extends Thread {
		@Override
		public void run() {
			running = true;
			try {
				Thread.sleep(timeout);
				if (running && flowRunner.isRunning()) {
					Event event = new Event(eventName);
					flowRunner.raiseEvent(event);
				}
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void stop() {
		running = false;
	}
	
}
