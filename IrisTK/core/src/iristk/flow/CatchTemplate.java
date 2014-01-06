package iristk.flow;

import iristk.system.Event;
import iristk.util.Record;

public abstract class CatchTemplate {

	public abstract boolean test(Event event, Record parameters);
	
}
