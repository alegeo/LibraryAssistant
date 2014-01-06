package iristk.app.chess;

import iristk.system.Event;
import iristk.flow.*;
import iristk.util.Record;
import static iristk.util.Converters.*;

public class ChessFlow extends iristk.flow.Flow {

	public ChessGame chess = null;

	public Object getVariable(String name) {
		if (name.equals("chess")) return chess;
		return null;
	}

	public void setVariable(String name, Object value) {
		if (name.equals("chess")) chess = (ChessGame) value;
	}

	public ChessFlow() {
	}

	@Override
	protected State getInitialState() {return new iristk.app.chess.ChessFlow.Start();}


	private class Game extends State {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event.triggers("chess.restart")) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams0 = new Record();
					actionParams0.put("text", "Okay, let's restart");
					if (!iristk.dialog.SimpleDialog.say.execute(flowRunner, actionParams0)) break EXECUTION;
					Record gotoParams1 = new Record();
					flowRunner.gotoState(new AwaitUser(), gotoParams1, this);
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


	private class Start extends Game {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams2 = new Record();
					actionParams2.put("text", "Okay, let's start");
					if (!iristk.dialog.SimpleDialog.say.execute(flowRunner, actionParams2)) break EXECUTION;
					Record gotoParams3 = new Record();
					flowRunner.gotoState(new AwaitUser(), gotoParams3, this);
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


	private class AwaitUser extends Game {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams4 = new Record();
					actionParams4.put("text", "What is your move?");
					if (!iristk.dialog.SimpleDialog.say.execute(flowRunner, actionParams4)) break EXECUTION;
					Record actionParams5 = new Record();
					if (!iristk.dialog.SimpleDialog.listen.execute(flowRunner, actionParams5)) break EXECUTION;
				}
				if (!propagate) return true;
			}
			if (event.triggers("chess.move.user")) {
				boolean propagate = false;
				EXECUTION: {
					chess.identifyMoves(makeRecord(event.get("move")));
					if (makeBool(chess.availableMoves() == 1)) {
						Record gotoParams6 = new Record();
						flowRunner.gotoState(new PerformMove(), gotoParams6, this);
						break EXECUTION;
					} else if (makeBool(chess.availableMoves() == 0)) {
						Record actionParams7 = new Record();
						actionParams7.put("text", "Sorry, I can't do that");
						if (!iristk.dialog.SimpleDialog.say.execute(flowRunner, actionParams7)) break EXECUTION;
						Record gotoParams8 = new Record();
						flowRunner.gotoState(new AwaitUser(), gotoParams8, this);
						break EXECUTION;
					} else {
						chess.displayAvailableMoves();
						Record clarify = chess.chooseClarification();
						if (makeBool(clarify.get("steps"))) {
							Record gotoParams9 = new Record();
							gotoParams9.put("lastMove", event.get("move"));
							flowRunner.gotoState(new ClarifySteps(), gotoParams9, this);
							break EXECUTION;
						} else if (makeBool(clarify.get("direction"))) {
							Record gotoParams10 = new Record();
							gotoParams10.put("lastMove", event.get("move"));
							flowRunner.gotoState(new ClarifyDirection(), gotoParams10, this);
							break EXECUTION;
						} else if (makeBool(clarify.get("piece"))) {
							Record gotoParams11 = new Record();
							gotoParams11.put("lastMove", event.get("move"));
							gotoParams11.put("piece", clarify.get("piece"));
							flowRunner.gotoState(new ClarifyPiece(), gotoParams11, this);
							break EXECUTION;
						} else if (makeBool(clarify.get("square"))) {
							Record gotoParams12 = new Record();
							gotoParams12.put("lastMove", event.get("move"));
							flowRunner.gotoState(new ClarifySquare(), gotoParams12, this);
							break EXECUTION;
						}
					}
				}
				if (!propagate) return true;
			}
			Record catchParams13 = new Record();
			catchParams13.put("sem", "act_move");
			if (iristk.dialog.SimpleDialog.onSpeech.test(event, catchParams13)) {
				boolean propagate = false;
				EXECUTION: {
					chess.newMove();
					Event raiseEvent14 = new Event("chess.move.user");
					raiseEvent14.put("move", event.get("sem"));
					flowRunner.raiseEvent(raiseEvent14);
				}
				if (!propagate) return true;
			}
			Record catchParams15 = new Record();
			if (iristk.dialog.SimpleDialog.onSpeech.test(event, catchParams15)) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams16 = new Record();
					actionParams16.put("text", "Sorry, I didn't get that");
					if (!iristk.dialog.SimpleDialog.say.execute(flowRunner, actionParams16)) break EXECUTION;
					flowRunner.raiseEvent(new EntryEvent());
					returnToState(this);
				}
				if (!propagate) return true;
			}
			Record catchParams17 = new Record();
			if (iristk.dialog.SimpleDialog.onNoSpeech.test(event, catchParams17)) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams18 = new Record();
					actionParams18.put("text", "Sorry");
					if (!iristk.dialog.SimpleDialog.say.execute(flowRunner, actionParams18)) break EXECUTION;
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


	private class PerformMove extends Game {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams19 = new Record();
					actionParams19.put("text", "Okay");
					if (!iristk.dialog.SimpleDialog.say.execute(flowRunner, actionParams19)) break EXECUTION;
					chess.performMove();
					Record gotoParams20 = new Record();
					flowRunner.gotoState(new AwaitSystem(), gotoParams20, this);
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


	private class ClarifySteps extends AwaitUser {


		public Record lastMove = null;

		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
			if (parameters.has("lastMove")) {
				lastMove = makeRecord(parameters.get("lastMove"));
			}
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams21 = new Record();
					actionParams21.put("text", "How many steps?");
					if (!iristk.dialog.SimpleDialog.say.execute(flowRunner, actionParams21)) break EXECUTION;
					Record actionParams22 = new Record();
					if (!iristk.dialog.SimpleDialog.listen.execute(flowRunner, actionParams22)) break EXECUTION;
				}
				if (!propagate) return true;
			}
			Record catchParams23 = new Record();
			catchParams23.put("sem", "number");
			if (iristk.dialog.SimpleDialog.onSpeech.test(event, catchParams23)) {
				boolean propagate = false;
				EXECUTION: {
					Record newMove = new Record(lastMove);
					newMove.put("movement:steps", event.get("sem:number"));
					Event raiseEvent24 = new Event("chess.move.user");
					raiseEvent24.put("move", newMove);
					flowRunner.raiseEvent(raiseEvent24);
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


	private class ClarifyDirection extends AwaitUser {


		public Record lastMove = null;

		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
			if (parameters.has("lastMove")) {
				lastMove = makeRecord(parameters.get("lastMove"));
			}
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams25 = new Record();
					actionParams25.put("text", "In which direction?");
					if (!iristk.dialog.SimpleDialog.say.execute(flowRunner, actionParams25)) break EXECUTION;
					Record actionParams26 = new Record();
					if (!iristk.dialog.SimpleDialog.listen.execute(flowRunner, actionParams26)) break EXECUTION;
				}
				if (!propagate) return true;
			}
			Record catchParams27 = new Record();
			catchParams27.put("sem", "direction");
			if (iristk.dialog.SimpleDialog.onSpeech.test(event, catchParams27)) {
				boolean propagate = false;
				EXECUTION: {
					Record newMove = new Record(lastMove);
					newMove.put("movement:direction", event.get("sem:direction"));
					Event raiseEvent28 = new Event("chess.move.user");
					raiseEvent28.put("move", newMove);
					flowRunner.raiseEvent(raiseEvent28);
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


	private class ClarifyPiece extends AwaitUser {


		public Record lastMove = null;
		public String piece = null;

		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
			if (parameters.has("lastMove")) {
				lastMove = makeRecord(parameters.get("lastMove"));
			}
			if (parameters.has("piece")) {
				piece = makeString(parameters.get("piece"));
			}
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams29 = new Record();
					actionParams29.put("text", concat("Which ", piece, "?"));
					if (!iristk.dialog.SimpleDialog.say.execute(flowRunner, actionParams29)) break EXECUTION;
					Record actionParams30 = new Record();
					if (!iristk.dialog.SimpleDialog.listen.execute(flowRunner, actionParams30)) break EXECUTION;
				}
				if (!propagate) return true;
			}
			Record catchParams31 = new Record();
			catchParams31.put("sem", "piece");
			if (iristk.dialog.SimpleDialog.onSpeech.test(event, catchParams31)) {
				boolean propagate = false;
				EXECUTION: {
					Record newMove = new Record(lastMove);
					newMove.put("piece", event.get("sem:piece"));
					Event raiseEvent32 = new Event("chess.move.user");
					raiseEvent32.put("move", newMove);
					flowRunner.raiseEvent(raiseEvent32);
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


	private class ClarifySquare extends AwaitUser {


		public Record lastMove = null;

		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
			if (parameters.has("lastMove")) {
				lastMove = makeRecord(parameters.get("lastMove"));
			}
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event instanceof EntryEvent) {
				boolean propagate = false;
				EXECUTION: {
					Record actionParams33 = new Record();
					actionParams33.put("text", "To which square?");
					if (!iristk.dialog.SimpleDialog.say.execute(flowRunner, actionParams33)) break EXECUTION;
					Record actionParams34 = new Record();
					if (!iristk.dialog.SimpleDialog.listen.execute(flowRunner, actionParams34)) break EXECUTION;
				}
				if (!propagate) return true;
			}
			Record catchParams35 = new Record();
			catchParams35.put("sem", "piece:square");
			if (iristk.dialog.SimpleDialog.onSpeech.test(event, catchParams35)) {
				boolean propagate = false;
				EXECUTION: {
					Record newMove = new Record(lastMove);
					newMove.put("movement:square", event.get("sem:piece:square"));
					Event raiseEvent36 = new Event("chess.move.user");
					raiseEvent36.put("move", newMove);
					flowRunner.raiseEvent(raiseEvent36);
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


	private class AwaitSystem extends Game {



		@Override
		public void setParameters(Record parameters) {
			super.setParameters(parameters);
		}

		@Override
		public boolean onFlowEvent(Event event) {
			if (event.triggers("chess.move.system")) {
				boolean propagate = false;
				EXECUTION: {
					Record gotoParams37 = new Record();
					flowRunner.gotoState(new AwaitUser(), gotoParams37, this);
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
