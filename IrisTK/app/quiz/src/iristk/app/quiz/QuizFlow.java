package iristk.app.quiz;

import iristk.system.Event;
import iristk.flow.*;
import iristk.util.Record;
import static iristk.util.Converters.*;

public class QuizFlow extends iristk.flow.Flow {

	public iristk.dialog.MultiParty mp = new iristk.dialog.MultiParty();
	public iristk.dialog.UserModel users = mp.users;
	public Question question;
	public QuestionSet questions = new QuestionSet();
	public int guess = 0;

	public Object getVariable(String name) {
		if (name.equals("mp")) return mp;
		if (name.equals("users")) return users;
		if (name.equals("question")) return question;
		if (name.equals("questions")) return questions;
		if (name.equals("guess")) return guess;
		return null;
	}

	public void setVariable(String name, Object value) {
		if (name.equals("mp")) mp = (iristk.dialog.MultiParty) value;
		if (name.equals("users")) users = (iristk.dialog.UserModel) value;
		if (name.equals("question")) question = (Question) value;
		if (name.equals("questions")) questions = (QuestionSet) value;
		if (name.equals("guess")) guess = (int) value;
	}

	public QuizFlow() {
	}

	@Override
	protected State getInitialState() {return new iristk.app.quiz.QuizFlow.Idle();}


	private class Idle extends State {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams0 = new Record();
					if (!mp.attendNobody.execute(flowRunner, actionParams0)) break EXECUTION;
				}
				if (!propagate) return true;
			}
			Record catchParams1 = new Record();
			if (mp.onEnterAgent.test(event, catchParams1)) {
				boolean propagate = false;
				EXECUTION: {
					users.put("" + event.get("agent") + ":score", 0);
					Record actionParams2 = new Record();
					actionParams2.put("target", event.get("agent"));
					if (!mp.attend.execute(flowRunner, actionParams2)) break EXECUTION;
					Record gotoParams3 = new Record();
					flowRunner.gotoState(new Greeting(), gotoParams3, this);
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


	private class Dialog extends State {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			Record catchParams4 = new Record();
			if (mp.onLeaveAttending.test(event, catchParams4)) {
				boolean propagate = false;
				EXECUTION: {
					if (makeBool(users.hasUser())) {
						Record actionParams5 = new Record();
						if (!mp.attendRandom.execute(flowRunner, actionParams5)) break EXECUTION;
						Record gotoParams6 = new Record();
						flowRunner.gotoState(new NextQuestion(), gotoParams6, this);
						break EXECUTION;
					} else {
						Record gotoParams7 = new Record();
						flowRunner.gotoState(new Goodbye(), gotoParams7, this);
						break EXECUTION;
					}
				}
				if (!propagate) return true;
			}
			Record catchParams8 = new Record();
			if (mp.onEnterAgent.test(event, catchParams8)) {
				boolean propagate = false;
				EXECUTION: {
					users.put("" + event.get("agent") + ":score", 0);
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


	private class Greeting extends Dialog {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams9 = new Record();
					actionParams9.put("text", "Would you like to play a game with me?");
					if (!mp.say.execute(flowRunner, actionParams9)) break EXECUTION;
					Record actionParams10 = new Record();
					if (!mp.listen.execute(flowRunner, actionParams10)) break EXECUTION;
				}
				if (!propagate) return true;
			}
			Record catchParams11 = new Record();
			catchParams11.put("sem", "yes");
			if (mp.onSpeech.test(event, catchParams11)) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams12 = new Record();
					actionParams12.put("text", "Great, let's start");
					if (!mp.say.execute(flowRunner, actionParams12)) break EXECUTION;
					question = questions.next();
					Record gotoParams13 = new Record();
					flowRunner.gotoState(new ReadQuestion(), gotoParams13, this);
					break EXECUTION;
				}
				if (!propagate) return true;
			}
			Record catchParams14 = new Record();
			if (mp.onSpeech.test(event, catchParams14)) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams15 = new Record();
					actionParams15.put("text", "Come on,");
					if (!mp.say.execute(flowRunner, actionParams15)) break EXECUTION;
					flowRunner.raiseEvent(new EntryEvent());
					returnToState(this);
				}
				if (!propagate) return true;
			}
			Record catchParams16 = new Record();
			if (mp.onNoSpeech.test(event, catchParams16)) {
				boolean propagate = false;
				EXECUTION: {
					flowRunner.raiseEvent(new EntryEvent());
					returnToState(this);
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


	private class NextQuestion extends Dialog {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					question = questions.next();guess=0;
					if (makeBool(users.hasMultiUsers())) {
						boolean chosen17 = false;
						boolean matching18 = true;
						while (!chosen17 && matching18) {
							int rand19 = random(1449293024, 2, iristk.util.RandomList.RandomModel.DECK_RESHUFFLE_NOREPEAT);
							matching18 = false;
							if (true) {
								matching18 = true;
								if (rand19 >= 0 && rand19 < 1) {
									chosen17 = true;
									Record actionParams20 = new Record();
									if (!mp.attendAll.execute(flowRunner, actionParams20)) break EXECUTION;
								}
							}
							if (true) {
								matching18 = true;
								if (rand19 >= 1 && rand19 < 2) {
									chosen17 = true;
									Record actionParams21 = new Record();
									if (!mp.attendOther.execute(flowRunner, actionParams21)) break EXECUTION;
								}
							}
						}
					}
					Record gotoParams22 = new Record();
					flowRunner.gotoState(new ReadQuestion(), gotoParams22, this);
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


	private class ReadQuestion extends Dialog {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams23 = new Record();
					actionParams23.put("text", question.getFullQuestion());
					if (!mp.say.execute(flowRunner, actionParams23)) break EXECUTION;
					Record gotoParams24 = new Record();
					flowRunner.gotoState(new AwaitAnswer(), gotoParams24, this);
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


	private class AwaitAnswer extends Dialog {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams25 = new Record();
					actionParams25.put("grammar", java.util.Arrays.asList("default", question.getId()));
					if (!mp.listen.execute(flowRunner, actionParams25)) break EXECUTION;
				}
				if (!propagate) return true;
			}
			Record catchParams26 = new Record();
			if (mp.onNoSpeech.test(event, catchParams26)) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams27 = new Record();
					actionParams27.put("text", "Why don't you make a guess");
					if (!mp.say.execute(flowRunner, actionParams27)) break EXECUTION;
					Record gotoParams28 = new Record();
					flowRunner.gotoState(new AwaitAnswer(), gotoParams28, this);
					break EXECUTION;
				}
				if (!propagate) return true;
			}
			Record catchParams29 = new Record();
			catchParams29.put("agent", "both");
			if (mp.onSpeech.test(event, catchParams29)) {
				boolean propagate = false;
				EXECUTION: {
					if (makeBool(eq(event.get("agent1:sem:answer"),question.get("correct")))) {
						Record actionParams30 = new Record();
						actionParams30.put("target", event.get("agent1:agent"));
						if (!mp.attend.execute(flowRunner, actionParams30)) break EXECUTION;
						Record gotoParams31 = new Record();
						flowRunner.gotoState(new CorrectAnswer(), gotoParams31, this);
						break EXECUTION;
					} else if (makeBool(eq(event.get("agent2:sem:answer"),question.get("correct")))) {
						Record actionParams32 = new Record();
						actionParams32.put("target", event.get("agent2:agent"));
						if (!mp.attend.execute(flowRunner, actionParams32)) break EXECUTION;
						Record gotoParams33 = new Record();
						flowRunner.gotoState(new CorrectAnswer(), gotoParams33, this);
						break EXECUTION;
					} else {
						Record actionParams34 = new Record();
						actionParams34.put("text", "None of you were correct, let's try another question.");
						if (!mp.say.execute(flowRunner, actionParams34)) break EXECUTION;
						Record gotoParams35 = new Record();
						flowRunner.gotoState(new NextQuestion(), gotoParams35, this);
						break EXECUTION;
					}
				}
				if (!propagate) return true;
			}
			Record catchParams36 = new Record();
			catchParams36.put("agent", "other");
			if (mp.onSpeech.test(event, catchParams36)) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams37 = new Record();
					actionParams37.put("mode", "eyes");
					if (!mp.attendOther.execute(flowRunner, actionParams37)) break EXECUTION;
					Record actionParams38 = new Record();
					actionParams38.put("text", "You were not supposed to answer that");
					if (!mp.say.execute(flowRunner, actionParams38)) break EXECUTION;
					Record actionParams39 = new Record();
					actionParams39.put("mode", "eyes");
					if (!mp.attendOther.execute(flowRunner, actionParams39)) break EXECUTION;
					Record actionParams40 = new Record();
					actionParams40.put("text", "So, what is your answer?");
					if (!mp.say.execute(flowRunner, actionParams40)) break EXECUTION;
					Record gotoParams41 = new Record();
					gotoParams41.put("read", false);
					flowRunner.gotoState(new AwaitAnswer(), gotoParams41, this);
					break EXECUTION;
				}
				if (!propagate) return true;
			}
			Record catchParams42 = new Record();
			catchParams42.put("sem", "dontknow");
			if (mp.onSpeech.test(event, catchParams42)) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams43 = new Record();
					actionParams43.put("text", "Give me your best guess");
					if (!mp.say.execute(flowRunner, actionParams43)) break EXECUTION;
					Record gotoParams44 = new Record();
					flowRunner.gotoState(new AwaitAnswer(), gotoParams44, this);
					break EXECUTION;
				}
				if (!propagate) return true;
			}
			Record catchParams45 = new Record();
			if (mp.onSpeech.test(event, catchParams45)) {
				boolean propagate = false;
				EXECUTION: {
					if (makeBool(eq(event.get("sem:answer"),question.get("correct")))) {
						Record gotoParams46 = new Record();
						flowRunner.gotoState(new CorrectAnswer(), gotoParams46, this);
						break EXECUTION;
					} else {
						Record gotoParams47 = new Record();
						flowRunner.gotoState(new IncorrectAnswer(), gotoParams47, this);
						break EXECUTION;
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
			return super.onExit();
		}

	}


	private class CorrectAnswer extends Dialog {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					users.put("current:score", makeInt(users.get("current:score")) + 1);
					Record actionParams48 = new Record();
					actionParams48.put("text", "That is correct");
					if (!mp.say.execute(flowRunner, actionParams48)) break EXECUTION;
					if (makeBool(makeInt(users.get("current:score")) == 3)) {
						Record gotoParams49 = new Record();
						flowRunner.gotoState(new Winner(), gotoParams49, this);
						break EXECUTION;
					}
					Record gotoParams50 = new Record();
					flowRunner.gotoState(new NextQuestion(), gotoParams50, this);
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


	private class IncorrectAnswer extends Dialog {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams51 = new Record();
					actionParams51.put("text", "That was wrong");
					if (!mp.say.execute(flowRunner, actionParams51)) break EXECUTION;
					if (makeBool(users.hasMultiUsers() && guess == 0)) {
						guess++;
						Record actionParams52 = new Record();
						if (!mp.attendOther.execute(flowRunner, actionParams52)) break EXECUTION;
						Record actionParams53 = new Record();
						actionParams53.put("text", "Maybe you know the answer?");
						if (!mp.say.execute(flowRunner, actionParams53)) break EXECUTION;
						Record gotoParams54 = new Record();
						flowRunner.gotoState(new AwaitAnswer(), gotoParams54, this);
						break EXECUTION;
					}
					Record actionParams55 = new Record();
					actionParams55.put("text", concat("The correct answer was ", question.get("" + question.get("correct"))));
					if (!mp.say.execute(flowRunner, actionParams55)) break EXECUTION;
					Record gotoParams56 = new Record();
					flowRunner.gotoState(new NextQuestion(), gotoParams56, this);
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


	private class Winner extends Dialog {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams57 = new Record();
					actionParams57.put("text", "Congratulations, you are the winner");
					if (!mp.say.execute(flowRunner, actionParams57)) break EXECUTION;
					Record actionParams58 = new Record();
					if (!mp.attendOther.execute(flowRunner, actionParams58)) break EXECUTION;
					Record actionParams59 = new Record();
					actionParams59.put("text", "I am sorry, but you are a looser.");
					if (!mp.say.execute(flowRunner, actionParams59)) break EXECUTION;
					Record gotoParams60 = new Record();
					flowRunner.gotoState(new Goodbye(), gotoParams60, this);
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


	private class Goodbye extends Idle {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams61 = new Record();
					actionParams61.put("text", "Have a nice day.");
					if (!mp.say.execute(flowRunner, actionParams61)) break EXECUTION;
					Record gotoParams62 = new Record();
					flowRunner.gotoState(new Idle(), gotoParams62, this);
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


}
