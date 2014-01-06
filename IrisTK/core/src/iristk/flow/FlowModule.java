package iristk.flow;

import iristk.util.Record;
import iristk.system.Event;
import iristk.system.IrisModule;

public class FlowModule extends IrisModule implements FlowListener {

	private FlowRunner flowRunner;
	private Flow flow;

	public FlowModule(Flow flow) {
		flowRunner = new FlowRunner();
		this.flow = flow;
	}
	
	//public FlowModule(File flow) throws FlowCompilerException {
	//	this.flow = Flow.compile(flow);
	//}
	
	@Override
	public void init() {
		flowRunner.addFlowListener(this);
	}

	@Override
	public void run() {
		new Thread() {
			@Override
			public void run() {
				flowRunner.start(flow.getInitialState());
			}
		}.start();
		super.run();
	}

	@Override
	public void onEvent(Event event) {
		flowRunner.invokeEvent(event);
	}
	
	public void addFlowListener(FlowListener listener) {
		flowRunner.addFlowListener(listener);
	}
	
	@Override
	public void onFlowEvent(Event event) {
		monitorEvent(event);
		//System.out.println("event " + event);
	}

	@Override
	public void onSendEvent(Event event) {
		send(event);
	}

	@Override
	public void onGotoState(State state, Record parameters) {
		monitorState(state);
	}

	@Override
	public void onCallState(State state, Record parameters) {
		//System.out.println("call " + state + " " + parameters);
		monitorState(state);
	}
	
	@Override
	public void onReturnState(State state) {
		//System.out.println("return " + state);
		monitorState(state);
	}
	
	private void monitorState(State state) {
		int depth = 0;
		State s = state;
		while (s != null) {
			depth++;
			s = s.caller;
		}
		String[] states = new String[depth];
		s = state;
		int i = depth - 1;
		while (s != null) {
			states[i] = s.getClass().getSimpleName();
			i--;
			s = s.caller;
		}
		super.monitorState(states);
	}

}
