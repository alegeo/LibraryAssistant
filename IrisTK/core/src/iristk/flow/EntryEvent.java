package iristk.flow;

import iristk.system.Event;

public class EntryEvent extends Event {

	public EntryEvent() {
		super("flow.entry");
	}

	/*
	private final Class<?> stateClass;

	public EntryEvent(Class<?> stateClass) {
		this.stateClass = stateClass;
	}

	public Class<?> getStateClass() {
		return stateClass;
	}
*/
}
