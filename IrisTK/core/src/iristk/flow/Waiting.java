package iristk.flow;

import iristk.system.Event;
import iristk.util.Record;

public class Waiting extends State {

	public EventTimer timer;
	public Integer time;
	
	@Override
	public void setParameters(Record parameters) {
		super.setParameters(parameters);
		if (parameters.has("time")) {
			time = (Integer) parameters.get("time");
		}
	}
	
	@Override
	public boolean onFlowEvent(Event event) {
		if (event instanceof EntryEvent) {
			timer = new EventTimer(flowRunner, time, "waiting.done");
		} else if (event.triggers("waiting.done")) {
			returnFromState(this);
		}
		if (super.onFlowEvent(event)) return true;
		if (callerHandlers(event)) return true;
		return false;
	}

	@Override
	public boolean onExit() {
		if (timer != null)
			timer.stop();
		return super.onExit();
	}
	
}
