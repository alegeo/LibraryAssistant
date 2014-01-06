package iristk.flow;

import iristk.system.Event;
import iristk.util.Record;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class FlowRunner {

	ArrayBlockingQueue<Event> eventQueue = new ArrayBlockingQueue<Event>(1000);
	private State currentState;
	private State gotoState;
	private ArrayList<FlowListener> listeners = new ArrayList<FlowListener>();
	private boolean running = false;

	public void invokeEvent(Event event) {
		eventQueue.add(event);
	}
	
	public void raiseEvent(Event event) {
		for (FlowListener listener : listeners) {
			listener.onFlowEvent(event);
		}
		invokeEvent(event);
	}
	
	public void raiseEvent(final Event message, final int delay) {
		if (delay <= 0) {
			raiseEvent(message);
		} else {
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					raiseEvent(message);
				};
			
			}.start();
		}
	}
	
	public void sendEvent(Event message) {
		for (FlowListener listener : listeners) {
			listener.onSendEvent(message);
		}
	}
	
	public void sendEvent(final Event message, final int delay) {
		if (delay <= 0) {
			sendEvent(message);
		} else {
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					sendEvent(message);
				};
			
			}.start();
		}
	}
	
	protected void actionWait(int msec) {
		callState(new Waiting(), new Record("time", msec));
	}

	public void addFlowListener(FlowListener listener) {
		listeners.add(listener);
	}
	
	public void gotoState(State state, Record parameters, State fromState) {
		//debug("Going to state " + state + " from " + currentState);
		gotoState = state;
		state.flowRunner = this;
		if (fromState != null) {
			state.caller = fromState.caller;
		}
		state.setParameters(parameters);
		for (FlowListener listener : listeners) {
			listener.onGotoState(state, parameters);
		}
	}
	
	// Returns true if the state has run to completion
	public boolean callState(State subState, Record parameters) { 
		//debug("Calling state " + subState + " from " + currentState);
		State callingState = currentState;
		currentState = subState;
		currentState.caller = callingState;
		currentState.flowRunner = this;
		currentState.setParameters(parameters);
		for (FlowListener listener : listeners) {
			listener.onCallState(subState, parameters);
		}
		//depth--;
		//System.out.println("Returning at depth " + depth + " " + currentState);
		return processState();
	}
	
	public void returnFromState(State fromState) {
		if (fromState.caller != null) {
			State state = currentState;
			while (state.caller != null) {
				if (state.caller == fromState.caller) {
					currentState.returnTo = fromState.caller;
					return;
				}
				state = state.caller;
			}
		}
	}
	
	public void returnToState(State toState) {
		State state = currentState;
		while (state.caller != null) {
			if (state.caller == toState) {
				currentState.returnTo = toState;
				return;
			}
			state = state.caller;
		}
	}
	
	public boolean abortExecution() {
		return (gotoState != null || (currentState != null && currentState.returnTo != null));
	}
	
	//int depth = 0;

	// Returns false if execution should be aborted
	private boolean processState() {
		//depth++;
		//System.out.println("Processing " + currentState);
		Event event = new EntryEvent();
		try {
			while (true) {
				if (gotoState != null) {
					//System.out.println("Going to " + gotoState + " called from " + gotoState.caller);
					if (currentState != null)
						currentState.onExit();
					if (currentState != null && gotoState.caller != currentState.caller) {
						currentState = currentState.caller;
						return false;
					} else {
						currentState = gotoState;
						gotoState = null;
						event = new EntryEvent();
					}
				} else if (currentState != null && currentState.returnTo != null) {
					State fromState = currentState;
					//System.out.println("Returning to " + fromState.returnTo);
					for (FlowListener listener : listeners) {
						listener.onReturnState(fromState.caller);
					}
					fromState.onExit();
					currentState = fromState.caller;
					if (currentState == fromState.returnTo) {
						return true;
					} else {
						currentState.returnTo = fromState.returnTo;
						return false;
					}
				}
				if (event == null)
					event = eventQueue.take();
				currentState.onFlowEvent(event);
				event = null;
			}
		} catch (InterruptedException e1) {
		}
		return false;
	}
	
	public void start(State state) {
		gotoState(state, new Record(), null);
		running  = true;
		processState();
	}
	
	public boolean isRunning() {
		return running;
	}
	
	
}
