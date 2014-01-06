package iristk.dialog;

import iristk.system.Event;
import iristk.flow.*;
import iristk.util.Record;
import static iristk.util.Converters.*;
import iristk.parser.cfg.Parser;

public class SimpleDialog extends iristk.flow.Flow {


	@Override
	public Object getVariable(String name) {
		return null;
	}

	@Override
	public void setVariable(String name, Object value) {
	}

	public SimpleDialog() {
	}

	public static ActionTemplate say = new ActionTemplate() {

		@Override
		public boolean execute(FlowRunner flowRunner, Record parameters) {
			String text = null;
			if (parameters.has("text")) {
				text = makeString(parameters.get("text"));
			}
			boolean result = false;
			EXECUTION: {
				Record callParams0 = new Record();
				callParams0.put("text", text);
				if (!flowRunner.callState(new Speaking(), callParams0)) break EXECUTION;
				result = true;
			}
			return result;
		}
	};

	public static ActionTemplate listen = new ActionTemplate() {

		@Override
		public boolean execute(FlowRunner flowRunner, Record parameters) {
			int timeout = 8000;
			if (parameters.has("timeout")) {
				timeout = (int) parameters.get("timeout");
			}
			boolean result = false;
			EXECUTION: {
				Record callParams1 = new Record();
				callParams1.put("timeout", timeout);
				if (!flowRunner.callState(new Listening(), callParams1)) break EXECUTION;
				result = true;
			}
			return result;
		}
	};

	public static CatchTemplate onSpeech = new CatchTemplate() {

		@Override
		public boolean test(Event event, Record parameters) {
			String sem = null;
			String text = null;
			if (parameters.has("sem")) {
				sem = makeString(parameters.get("sem"));
			}
			if (parameters.has("text")) {
				text = makeString(parameters.get("text"));
			}
			if (event.triggers("sense.speech.rec.final")) {
				return (makeBool((text == null && sem == null) || (text != null && makeBool(((Parser)event.get("parser")).find(text))) || (sem != null && event.has("sem:" + sem))));
			}
			else return false;
		}
	};

	public static CatchTemplate onNoSpeech = new CatchTemplate() {

		@Override
		public boolean test(Event event, Record parameters) {
			if (event.triggers("sense.speech.rec.nospeech")) {
				return true;
			}
			else return false;
		}
	};


	private static class Speaking extends State {

		public String actionId;

		public String text = null;

		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
			if (parameters.has("text")) {
				text = makeString(parameters.get("text"));
			}
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Event sendEvent2 = new Event("action.speech");
					sendEvent2.put("text", text);
					flowRunner.sendEvent(sendEvent2);
					actionId = sendEvent2.getId();
				}
				if (!propagate) return true;
			}
			if (event.triggers("monitor.speech.end")) {
				if (makeBool(eq(actionId,event.get("action")))) {
					boolean propagate = false;
					EXECUTION: {
						returnFromState(this);
						break EXECUTION;
					}
					if (!propagate) return true;
				}
			}
			if (super.onFlowEvent(event)) return true;
			if (callerHandlers(event)) return true;
			return false;
		}
		@Override
		public boolean onExit() {
			return super.onExit();
		}

	}


	private static class Listening extends State {

		public String actionId;

		public int timeout = 8000;

		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
			if (parameters.has("timeout")) {
				timeout = (int) parameters.get("timeout");
			}
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Event sendEvent3 = new Event("action.listen");
					sendEvent3.put("timeout", timeout);
					flowRunner.sendEvent(sendEvent3);
					actionId = sendEvent3.getId();
				}
				if (!propagate) return true;
			}
			if (event.triggers("sense.speech.rec.**")) {
				if (makeBool(eq(event.get("action"),actionId))) {
					boolean propagate = false;
					EXECUTION: {
						Event returnEvent4 = new Event(event.getName());
						returnEvent4.putAll(event);
						flowRunner.raiseEvent(returnEvent4);
						returnFromState(this);
						break EXECUTION;
					}
					if (!propagate) return true;
				}
			}
			if (super.onFlowEvent(event)) return true;
			if (callerHandlers(event)) return true;
			return false;
		}
		@Override
		public boolean onExit() {
			int rand;
			boolean chosen;
			boolean matching;
			Event sendEvent;
			boolean propagate = false;
			EXECUTION: {
				Event sendEvent5 = new Event("action.listen.stop");
				flowRunner.sendEvent(sendEvent5);
			}
			if (!propagate) return true;
			return super.onExit();
		}

	}


}
