package iristk.flow;

import iristk.system.Event;
import iristk.util.Record;

public interface FlowListener {

	void onFlowEvent(Event event);

	void onSendEvent(Event event);
	
	void onGotoState(State state, Record parameters);
	
	void onCallState(State state, Record parameters);
	
	void onReturnState(State state);
	
}
