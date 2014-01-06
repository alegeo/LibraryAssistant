package iristk.dialog;

import iristk.system.Event;
import iristk.flow.*;
import iristk.util.Record;
import static iristk.util.Converters.*;
import iristk.parser.cfg.Parser;

public class MultiParty extends iristk.flow.Flow {

	public UserModel users = new UserModel();
	public EventClock blinkClock;

	@Override
	public Object getVariable(String name) {
		if (name.equals("users")) return users;
		if (name.equals("blinkClock")) return blinkClock;
		return null;
	}

	@Override
	public void setVariable(String name, Object value) {
		if (name.equals("users")) users = (UserModel) value;
		if (name.equals("blinkClock")) blinkClock = (EventClock) value;
	}

	public MultiParty() {
	}

	@Override
	protected State getInitialState() {return new iristk.dialog.MultiParty.Init();}

	public ActionTemplate attend = new ActionTemplate() {

		@Override
		public boolean execute(FlowRunner flowRunner, Record parameters) {
			String mode = "headpose";
			String target = "nobody";
			if (parameters.has("mode")) {
				mode = makeString(parameters.get("mode"));
			}
			if (parameters.has("target")) {
				target = makeString(parameters.get("target"));
			}
			boolean result = false;
			EXECUTION: {
				users.setAttending(target);
				Event sendEvent0 = new Event("action.attend");
				sendEvent0.put("target", target);
				sendEvent0.put("mode", mode);
				flowRunner.sendEvent(sendEvent0);
				result = true;
			}
			return result;
		}
	};

	public ActionTemplate attendNobody = new ActionTemplate() {

		@Override
		public boolean execute(FlowRunner flowRunner, Record parameters) {
			boolean result = false;
			EXECUTION: {
				users.setAttending("nobody");
				Event sendEvent1 = new Event("action.attend");
				sendEvent1.put("target", "nobody");
				flowRunner.sendEvent(sendEvent1);
				result = true;
			}
			return result;
		}
	};

	public ActionTemplate attendRandom = new ActionTemplate() {

		@Override
		public boolean execute(FlowRunner flowRunner, Record parameters) {
			boolean result = false;
			EXECUTION: {
				String random = users.random().id();
				users.setAttending(random);
				Event sendEvent2 = new Event("action.attend");
				sendEvent2.put("target", random);
				flowRunner.sendEvent(sendEvent2);
				result = true;
			}
			return result;
		}
	};

	public ActionTemplate attendOther = new ActionTemplate() {

		@Override
		public boolean execute(FlowRunner flowRunner, Record parameters) {
			String mode = "headpose";
			if (parameters.has("mode")) {
				mode = makeString(parameters.get("mode"));
			}
			boolean result = false;
			EXECUTION: {
				String other = users.other().id();
				users.setAttending(other);
				Event sendEvent3 = new Event("action.attend");
				sendEvent3.put("target", other);
				sendEvent3.put("mode", mode);
				flowRunner.sendEvent(sendEvent3);
				result = true;
			}
			return result;
		}
	};

	public ActionTemplate attendAll = new ActionTemplate() {

		@Override
		public boolean execute(FlowRunner flowRunner, Record parameters) {
			boolean result = false;
			EXECUTION: {
				users.setAttendingAll();
				Event sendEvent4 = new Event("action.attend");
				sendEvent4.put("target", "all");
				flowRunner.sendEvent(sendEvent4);
				result = true;
			}
			return result;
		}
	};

	public ActionTemplate attendAsleep = new ActionTemplate() {

		@Override
		public boolean execute(FlowRunner flowRunner, Record parameters) {
			boolean result = false;
			EXECUTION: {
				users.setAttending("asleep");
				Event sendEvent5 = new Event("action.attend");
				sendEvent5.put("target", "asleep");
				flowRunner.sendEvent(sendEvent5);
				result = true;
			}
			return result;
		}
	};

	public ActionTemplate say = new ActionTemplate() {

		@Override
		public boolean execute(FlowRunner flowRunner, Record parameters) {
			String text = null;
			if (parameters.has("text")) {
				text = makeString(parameters.get("text"));
			}
			boolean result = false;
			EXECUTION: {
				Record callParams6 = new Record();
				callParams6.put("text", text);
				if (!flowRunner.callState(new Speaking(), callParams6)) break EXECUTION;
				result = true;
			}
			return result;
		}
	};

	public ActionTemplate listen = new ActionTemplate() {

		@Override
		public boolean execute(FlowRunner flowRunner, Record parameters) {
			int timeout = 8000;
			Object grammar = null;
			if (parameters.has("timeout")) {
				timeout = (int) parameters.get("timeout");
			}
			if (parameters.has("grammar")) {
				grammar = parameters.get("grammar");
			}
			boolean result = false;
			EXECUTION: {
				Record callParams7 = new Record();
				callParams7.put("grammar", grammar);
				callParams7.put("timeout", timeout);
				if (!flowRunner.callState(new Listening(), callParams7)) break EXECUTION;
				result = true;
			}
			return result;
		}
	};

	public CatchTemplate onEnterAgent = new CatchTemplate() {

		@Override
		public boolean test(Event event, Record parameters) {
			if (event.triggers("sense.enter")) {
				return true;
			}
			else return false;
		}
	};

	public CatchTemplate onLeaveAttending = new CatchTemplate() {

		@Override
		public boolean test(Event event, Record parameters) {
			if (event.triggers("sense.leave.attending")) {
				return true;
			}
			else return false;
		}
	};

	public CatchTemplate onLeaveOther = new CatchTemplate() {

		@Override
		public boolean test(Event event, Record parameters) {
			if (event.triggers("sense.leave.other")) {
				return true;
			}
			else return false;
		}
	};

	public CatchTemplate onNoSpeech = new CatchTemplate() {

		@Override
		public boolean test(Event event, Record parameters) {
			if (event.triggers("sense.speech.multirec.nospeech")) {
				return true;
			}
			else return false;
		}
	};

	public CatchTemplate onSpeech = new CatchTemplate() {

		@Override
		public boolean test(Event event, Record parameters) {
			String agent = "attending";
			String text = null;
			String sem = null;
			if (parameters.has("agent")) {
				agent = makeString(parameters.get("agent"));
			}
			if (parameters.has("text")) {
				text = makeString(parameters.get("text"));
			}
			if (parameters.has("sem")) {
				sem = makeString(parameters.get("sem"));
			}
			if (event.triggers("sense.speech.multirec.final")) {
				return (makeBool((eq(agent,"any") || eq(agent,event.get("agent"))) && ((text == null && sem == null) || (text != null && makeBool(((Parser)event.get("parser")).find(text))) || (sem != null && event.has("sem:" + sem)))));
			}
			else return false;
		}
	};


	private class Init extends State {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					blinkClock = new EventClock(flowRunner, 1000, 5000, "blink");
					Record gotoParams8 = new Record();
					flowRunner.gotoState(new Idle(), gotoParams8, this);
					break EXECUTION;
				}
				if (!propagate) return true;
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


	private class Attention extends State {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event.triggers("action.attend")) {
				if (makeBool(eq(event.get("target"),"nobody"))) {
					boolean propagate = false;
					EXECUTION: {
						Record gotoParams9 = new Record();
						flowRunner.gotoState(new Idle(), gotoParams9, this);
						break EXECUTION;
					}
					if (!propagate) return true;
				}
			}
			if (event.triggers("action.attend")) {
				if (makeBool(eq(event.get("target"),"asleep"))) {
					boolean propagate = false;
					EXECUTION: {
						Record gotoParams10 = new Record();
						flowRunner.gotoState(new Asleep(), gotoParams10, this);
						break EXECUTION;
					}
					if (!propagate) return true;
				}
			}
			if (event.triggers("action.attend")) {
				if (makeBool(eq(event.get("target"),"all"))) {
					boolean propagate = false;
					EXECUTION: {
						Record gotoParams11 = new Record();
						flowRunner.gotoState(new AttendingAll(), gotoParams11, this);
						break EXECUTION;
					}
					if (!propagate) return true;
				}
			}
			if (event.triggers("action.attend")) {
				if (makeBool(eq(event.get("target"),"spot"))) {
					boolean propagate = false;
					EXECUTION: {
						Record gotoParams12 = new Record();
						gotoParams12.putAll(event);
						flowRunner.gotoState(new AttendingSpot(), gotoParams12, this);
						break EXECUTION;
					}
					if (!propagate) return true;
				}
			}
			if (event.triggers("action.attend")) {
				if (makeBool(users.get("" + event.get("target")))) {
					boolean propagate = false;
					EXECUTION: {
						Record gotoParams13 = new Record();
						gotoParams13.putAll(event);
						flowRunner.gotoState(new AttendingAgent(), gotoParams13, this);
						break EXECUTION;
					}
					if (!propagate) return true;
				}
			}
			if (event.triggers("sense.body")) {
				boolean propagate = false;
				EXECUTION: {
					if (makeBool(event.get("location"))) {
						if (makeBool(event.get("proximate"))) {
							if (makeBool(!makeBool(users.get("" + event.get("agent"))))) {
								users.put("" + event.get("agent"), new User(event.get("agent"), event.get("location")));
								Event sendEvent14 = new Event("sense.enter");
								sendEvent14.put("location", event.get("location"));
								sendEvent14.put("agent", event.get("agent"));
								flowRunner.sendEvent(sendEvent14);
							}
						} else {
							if (makeBool(users.get("" + event.get("agent")))) {
								users.remove(event.get("agent"));
								if (makeBool(users.isAttending(event.get("agent")) || users.isAttendingAll())) {
									Event sendEvent15 = new Event("sense.leave.attending");
									sendEvent15.put("location", event.get("location"));
									sendEvent15.put("agent", event.get("agent"));
									flowRunner.sendEvent(sendEvent15);
								} else {
									Event sendEvent16 = new Event("sense.leave.other");
									sendEvent16.put("location", event.get("location"));
									sendEvent16.put("agent", event.get("agent"));
									flowRunner.sendEvent(sendEvent16);
								}
							}
						}
					}
					if (makeBool(users.get("" + event.get("agent")))) {
						users.put("" + event.get("agent") + ":x", makeFloat(event.get("x")));
						users.put("" + event.get("agent") + ":y", makeFloat(event.get("y")));
						if (makeBool(event.get("age"))) {
							users.put("" + event.get("agent") + ":age", event.get("age"));
						};
						Event raiseEvent17 = new Event("changePosition");
						flowRunner.raiseEvent(raiseEvent17);
					}
				}
				if (!propagate) return true;
			}
			if (event.triggers("blink")) {
				boolean propagate = false;
				EXECUTION: {
					Event sendEvent18 = new Event("action.gesture");
					sendEvent18.put("name", "blink");
					flowRunner.sendEvent(sendEvent18);
				}
				if (!propagate) return true;
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


	private class Idle extends Attention {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Event sendEvent19 = new Event("action.gaze");
					sendEvent19.put("mode", "headpose");
					sendEvent19.put("x", 0);
					sendEvent19.put("y", -8);
					flowRunner.sendEvent(sendEvent19);
					Event sendEvent20 = new Event("monitor.attend");
					sendEvent20.put("target", "nobody");
					flowRunner.sendEvent(sendEvent20);
				}
				if (!propagate) return true;
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


	private class AttendingAll extends Attention {

		public User gazeTarget = users.random();


		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					User user1 = users.random();
					User user2 = users.other(user1);
					Event sendEvent21 = new Event("action.gaze");
					sendEvent21.put("mode", "headpose");
					sendEvent21.put("x", makeFloat(user1.get("x")) + ((makeFloat(user2.get("x")) - makeFloat(user1.get("x"))) / 2));
					sendEvent21.put("y", makeFloat(user1.get("y")) + ((makeFloat(user2.get("y")) - makeFloat(user1.get("y"))) / 2));
					flowRunner.sendEvent(sendEvent21);
					Event sendEvent22 = new Event("monitor.attend");
					sendEvent22.put("target", "all");
					flowRunner.sendEvent(sendEvent22);
					Event raiseEvent23 = new Event("gazeShift");
					flowRunner.raiseEvent(raiseEvent23, 1000);
				}
				if (!propagate) return true;
			}
			if (event.triggers("gazeShift")) {
				if (makeBool(users.hasMultiUsers())) {
					boolean propagate = false;
					EXECUTION: {
						gazeTarget = users.other(gazeTarget);
						Event sendEvent24 = new Event("action.gaze");
						sendEvent24.put("mode", "eyes");
						sendEvent24.put("x", users.get("" + gazeTarget.get("id") + ":x"));
						sendEvent24.put("y", users.get("" + gazeTarget.get("id") + ":y"));
						flowRunner.sendEvent(sendEvent24);
						Event raiseEvent25 = new Event("gazeShift");
						flowRunner.raiseEvent(raiseEvent25, 1000);
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


	private class AttendingAgent extends Attention {


		public String target = null;
		public String mode = "headpose";

		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
			if (parameters.has("target")) {
				target = makeString(parameters.get("target"));
			}
			if (parameters.has("mode")) {
				mode = makeString(parameters.get("mode"));
			}
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Event sendEvent26 = new Event("action.gaze");
					sendEvent26.put("mode", mode);
					sendEvent26.put("x", users.get("" + target + ":x"));
					sendEvent26.put("y", users.get("" + target + ":y"));
					flowRunner.sendEvent(sendEvent26);
					Event sendEvent27 = new Event("monitor.attend");
					sendEvent27.put("target", target);
					flowRunner.sendEvent(sendEvent27);
				}
				if (!propagate) return true;
			}
			if (event.triggers("changePosition")) {
				boolean propagate = false;
				EXECUTION: {
					if (makeBool(users.has(target) && eq(mode,"headpose"))) {
						Event sendEvent28 = new Event("action.gaze");
						sendEvent28.put("x", users.get("" + target + ":x"));
						sendEvent28.put("y", users.get("" + target + ":y"));
						flowRunner.sendEvent(sendEvent28);
					}
				}
				if (!propagate) return true;
			}
			if (event.triggers("sense.speech.start")) {
				if (makeBool(users.isAttendingLocation(event.get("location")))) {
					boolean propagate = false;
					EXECUTION: {
						Event sendEvent29 = new Event("action.gesture");
						sendEvent29.put("name", "smile");
						flowRunner.sendEvent(sendEvent29);
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


	private class AttendingSpot extends Attention {


		public float x = 0;
		public float y = 0;
		public String mode = "default";

		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
			if (parameters.has("x")) {
				x = (float) parameters.get("x");
			}
			if (parameters.has("y")) {
				y = (float) parameters.get("y");
			}
			if (parameters.has("mode")) {
				mode = makeString(parameters.get("mode"));
			}
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Event sendEvent30 = new Event("action.gaze");
					sendEvent30.put("mode", mode);
					sendEvent30.put("x", x);
					sendEvent30.put("y", y);
					flowRunner.sendEvent(sendEvent30);
				}
				if (!propagate) return true;
			}
			if (event.triggers("action.attend")) {
				if (makeBool(eq(event.get("target"),"spot"))) {
					boolean propagate = false;
					EXECUTION: {
						Event sendEvent31 = new Event("action.gaze");
						sendEvent31.put("mode", makeString(event.get("mode"), "default"));
						sendEvent31.put("x", event.get("x"));
						sendEvent31.put("y", event.get("y"));
						flowRunner.sendEvent(sendEvent31);
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


	private class Asleep extends Attention {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Event sendEvent32 = new Event("action.gaze");
					sendEvent32.put("mode", "headpose");
					sendEvent32.put("x", 0);
					sendEvent32.put("y", -15);
					flowRunner.sendEvent(sendEvent32);
					Event sendEvent33 = new Event("action.gesture");
					sendEvent33.put("name", "sleep");
					flowRunner.sendEvent(sendEvent33);
				}
				if (!propagate) return true;
			}
			if (event.triggers("blink")) {
				boolean propagate = false;
				EXECUTION: {
				}
				if (!propagate) return true;
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
				Event sendEvent34 = new Event("action.gesture");
				sendEvent34.put("name", "blink");
				flowRunner.sendEvent(sendEvent34);
			}
			if (!propagate) return true;
			return super.onExit();
		}

	}


	private class Speaking extends State {

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
					Event sendEvent35 = new Event("action.speech");
					sendEvent35.put("text", text);
					flowRunner.sendEvent(sendEvent35);
					actionId = sendEvent35.getId();
				}
				if (!propagate) return true;
			}
			if (event.triggers("monitor.speech.start")) {
				boolean propagate = false;
				EXECUTION: {
					Event sendEvent36 = new Event("action.gesture");
					sendEvent36.put("name", "brow_raise");
					flowRunner.sendEvent(sendEvent36);
				}
				if (!propagate) return true;
			}
			if (event.triggers("monitor.speech.end")) {
				if (makeBool(eq(event.get("action"),actionId))) {
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
			int rand;
			boolean chosen;
			boolean matching;
			Event sendEvent;
			boolean propagate = false;
			EXECUTION: {
				Event sendEvent37 = new Event("action.speech.stop");
				flowRunner.sendEvent(sendEvent37);
			}
			if (!propagate) return true;
			return super.onExit();
		}

	}


	private class Listening extends State {

		public String actionId;
		public int speakers = 0;
		public Record agent1 = null;

		public int timeout = 8000;
		public Object grammar = null;

		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
			if (parameters.has("timeout")) {
				timeout = (int) parameters.get("timeout");
			}
			if (parameters.has("grammar")) {
				grammar = parameters.get("grammar");
			}
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					if (makeBool(grammar != null)) {
						Event sendEvent38 = new Event("action.listen");
						sendEvent38.put("grammar", grammar);
						sendEvent38.put("timeout", timeout);
						flowRunner.sendEvent(sendEvent38);
					} else {
						Event sendEvent39 = new Event("action.listen");
						sendEvent39.put("timeout", timeout);
						flowRunner.sendEvent(sendEvent39);
					}
				}
				if (!propagate) return true;
			}
			if (event.triggers("sense.speech.start")) {
				boolean propagate = false;
				EXECUTION: {
					speakers++;
				}
				if (!propagate) return true;
			}
			if (event.triggers("sense.speech.rec.nospeech")) {
				if (makeBool(speakers == 0)) {
					boolean propagate = false;
					EXECUTION: {
						Event returnEvent40 = new Event("sense.speech.multirec.nospeech");
						flowRunner.raiseEvent(returnEvent40);
						returnFromState(this);
						break EXECUTION;
					}
					if (!propagate) return true;
				}
			}
			if (event.triggers("sense.speech.rec.final")) {
				boolean propagate = false;
				EXECUTION: {
					if (makeBool(users.hasMultiUsers() && speakers == 2)) {
						if (makeBool(users.isAttendingAll())) {
							if (makeBool(agent1 == null)) {
								agent1 = event; agent1.put("agent", users.userAt(event.get("location")).id());
							} else {
								Record agent2 = event;
								agent2.put("agent", users.userAt(event.get("location")).id());
								Event returnEvent41 = new Event("sense.speech.multirec.final");
								returnEvent41.put("agent", "both");
								returnEvent41.put("agent1", agent1);
								returnEvent41.put("agent2", agent2);
								flowRunner.raiseEvent(returnEvent41);
								returnFromState(this);
								break EXECUTION;
							}
						} else if (makeBool(users.isAttendingLocation(event.get("location")))) {
							Event returnEvent42 = new Event("sense.speech.multirec.final");
							returnEvent42.putAll(event);
							returnEvent42.put("agent", "attending");
							flowRunner.raiseEvent(returnEvent42);
							returnFromState(this);
							break EXECUTION;
						} else {
							speakers--;
						}
					} else {
						if (makeBool(users.isAttendingAll())) {
							users.setAttending(users.userAt(event.get("location")).id());
							Event sendEvent43 = new Event("action.attend");
							sendEvent43.put("target", users.userAt(event.get("location")).id());
							sendEvent43.put("mode", "headpose");
							flowRunner.sendEvent(sendEvent43);
							Event returnEvent44 = new Event("sense.speech.multirec.final");
							returnEvent44.putAll(event);
							returnEvent44.put("agent", "attending");
							flowRunner.raiseEvent(returnEvent44);
							returnFromState(this);
							break EXECUTION;
						} else if (makeBool(!users.hasMultiUsers() || users.isAttendingLocation(event.get("location")))) {
							Event returnEvent45 = new Event("sense.speech.multirec.final");
							returnEvent45.putAll(event);
							returnEvent45.put("agent", "attending");
							flowRunner.raiseEvent(returnEvent45);
							returnFromState(this);
							break EXECUTION;
						} else if (makeBool(users.hasUserAt(event.get("location")))) {
							Event returnEvent46 = new Event("sense.speech.multirec.final");
							returnEvent46.putAll(event);
							returnEvent46.put("agent", "other");
							flowRunner.raiseEvent(returnEvent46);
							returnFromState(this);
							break EXECUTION;
						} else {
							Event returnEvent47 = new Event("sense.speech.multirec.final");
							returnEvent47.putAll(event);
							returnEvent47.put("agent", "nobody");
							flowRunner.raiseEvent(returnEvent47);
							returnFromState(this);
							break EXECUTION;
						}
					}
				}
				if (!propagate) return true;
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
				Event sendEvent48 = new Event("action.listen.stop");
				flowRunner.sendEvent(sendEvent48);
			}
			if (!propagate) return true;
			return super.onExit();
		}

	}


}
