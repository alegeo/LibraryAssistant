package iristk.flow;

import iristk.system.Event;

import java.util.Random;

public class EventClock {

	Random rand = new Random();
	private int minInt;
	private int maxInt;
	private String eventName;
	private FlowRunner flowRunner;
	private boolean running;
	private ClockThread clockThread;
	
	public EventClock(FlowRunner flowRunner, int minInt, int maxInt, String event) {
		this.minInt = minInt;
		this.maxInt = maxInt;
		this.eventName = event;
		this.flowRunner = flowRunner;
		this.clockThread = new ClockThread();
		clockThread.start();
	}
		
	public EventClock(FlowRunner flowRunner, int interval, String event) {
		this(flowRunner, interval, interval, event);
	}
	
	private class ClockThread extends Thread {
		@Override
		public void run() {
			running = true;
			while (running) {
				int interval;
				if (minInt == maxInt)
					interval = minInt;
				else
					interval = rand.nextInt(1 + maxInt - minInt) + minInt;
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					break;
				}
				if (running && flowRunner.isRunning()) {
					Event event = new Event(eventName);
					flowRunner.raiseEvent(event);
				}
			}
		}
	}
	
	public void stop() {
		running = false;
	}
	
}
