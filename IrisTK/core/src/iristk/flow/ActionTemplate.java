package iristk.flow;

import iristk.util.Record;

public abstract class ActionTemplate {

	public abstract boolean execute(FlowRunner flowRunner, Record parameters);
	
}
