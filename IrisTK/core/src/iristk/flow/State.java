package iristk.flow;

import iristk.system.Event;
import iristk.util.Record;

public abstract class State  {
	
	public State caller = null;
	//public EventHandler extraCallerHandler = null;
	public FlowRunner flowRunner;
	public State returnTo = null;
		
	// Returns true if event is consumed
	public boolean onFlowEvent(Event event) {
		if (event instanceof EntryEvent) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return getClass().getName();
	}

	protected void returnFromState(State state) {
		flowRunner.returnFromState(state);
	}
	
	protected void returnToState(State state) {
		flowRunner.returnToState(state);
	}
	
	protected boolean callerHandlers(Event event) {
		if (caller != null) {
			if (caller.onFlowEvent(event)) return true;
		}
		return false;
	}
	
	public void setParameters(Record param) {
	}
	
	public boolean onExit() {
		return false;
	}
	
}
