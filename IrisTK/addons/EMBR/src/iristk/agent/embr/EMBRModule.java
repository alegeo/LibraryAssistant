package iristk.agent.embr;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.bind.JAXBException;

import org.w3c.dom.Node;

import iristk.system.Event;
import iristk.system.InitializationException;
import iristk.system.IrisModule;
import iristk.util.Mapper;
import iristk.util.XmlMarshaller;
import iristk.xml.phones.Phones;
import iristk.xml.phones.Phones.Phone;

public class EMBRModule extends IrisModule {

	private Socket socket;
	private DataOutputStream outToServer;
	private XmlMarshaller<Phones> phonesMarshaller = new XmlMarshaller<Phones>("iristk.xml.phones");

	private Mapper ups2embr = new Mapper("ups2embr", this.getClass().getResourceAsStream("ups2embr.map"));
	private float headX = 0;
	private float headY = 0;

	private static class Mappings extends ArrayList<Mapping> {
		public String map(String label) {
			for (Mapping mapping : this) {
				if (mapping.matches(label)) {
					return mapping.get(0);
				}
			}
			return label;
		}
	}

	private static class Mapping extends ArrayList<String> {
		public Mapping(String... items) {
			for (String item : items)  {
				add(item);
			}
		}

		public boolean matches(String label) {
			for (int i = 1; i < this.size(); i++) {
				if (get(i).equals(label)) 
					return true;
			}
			return false;
		}
	}
	
	@Override
	public void onEvent(Event event) {
		try {
			if (event.getName().equals("monitor.speech.start")) {
				if (event.has("phones")) {
					lipSync(event);
				}
			} else if (event.getName().equals("action.gaze")) {
				gaze(event);
			} else if (event.getName().equals("action.gesture")) {
				String name = event.getString("name");
				if (name.equals("brow_raise")) {
					morphTarget(event.getInt("length", 0), "ModBrowUpRight", "ModBrowUpLeft");
				} else if (name.equals("smile")) {
					morphTarget(event.getInt("length", 0), "ExpSmileClosed");
				} else if (name.equals("smile_open")) {
					morphTarget(event.getInt("length", 1000), "ExpSmileOpen");
				} else if (name.equals("anger")) {
					morphTarget(event.getInt("length", 1000), "ExpAnger");
				} else if (name.equals("disgust")) {
					morphTarget(event.getInt("length", 1000), "ExpDisgust");
				} else if (name.equals("fear")) {
					morphTarget(event.getInt("length", 1000), "ExpFear");
				} else if (name.equals("sad")) {
					morphTarget(event.getInt("length", 1000), "ExpSad");
				} else if (name.equals("surprise")) {
					morphTarget(event.getInt("length", 1000), "ExpSurprise");
				} else if (name.equals("nod_head")) {
					nodHead();
				} else if (name.equals("shake_head")) {
					shakeHead();
				}
			}
		} catch (Exception e) {
			System.err.println("Cannot connect to EMBR: " + e.getMessage());
		} finally {
			closeConnection();
		}
	}

	private void gaze(Event event) throws IOException {
		if (!connect()) return;
		String mode = event.getString("mode", "default");
		if (!mode.equals("eyes"))
			mode = "headNeck";
		if (mode.equals("headNeck")) {
			headX = event.getFloat("x");
			headY = event.getFloat("y");
		}
		beginSequence();
		beginPose(500);
		lookAtAng(event.getFloat("x"), event.getFloat("y"), mode);
		end(); // Pose
		end(); // Sequence
	}
	
	private void nodHead() throws IOException {
		if (!connect()) return;
		beginSequence();
		beginPose(500);
		lookAtAng(headX, headY - 8, "headNeck");	
		end(); // Pose
		beginPose(1000);
		lookAtAng(headX, headY, "headNeck");	
		end(); // Pose
		end(); // Sequence
	}
	
	private void shakeHead() throws IOException {
		if (!connect()) return;
		beginSequence();
		beginPose(500);
		lookAtAng(headX + 8, headY, "headNeck");	
		end(); // Pose
		beginPose(1000);
		lookAtAng(headX - 8, headY, "headNeck");	
		end(); // Pose
		beginPose(1500);
		lookAtAng(headX, headY, "headNeck");	
		end(); // Pose
		end(); // Sequence
	}

	private void lipSync(Event event) throws UnknownHostException, IOException, JAXBException {
		if (!connect()) return;
		beginSequence();
		Phones phones = phonesMarshaller.unmarshal((Node)event.get("phones"));
		int i = 0;
		String target = null;
		for (Phone phone : phones.getPhone()) {
			//System.out.println(phone.getName());
			i++;
			beginPose((int)(phone.getStart() * 1000f));
			if (target != null && !target.equals("sil")) {
				morphTarget(target, 0f);
			}
			target = ups2embr.map(phone.getName());
			if (!target.equals("sil")) {
				morphTarget(target, 1f);
			}
			end(); // Pose
			if (i == phones.getPhone().size() && !target.equals("sil")) {
				beginPose((int)(phone.getEnd() * 1000f));
				morphTarget(target, 0f);
				end(); // Pose
			}
		}
		end(); // Sequence
	}

	private void morphTarget(int hold, String... params) throws IOException {
		if (!connect()) return;
		beginSequence();
		beginPose(500, hold);
		for (String param : params) {
			morphTarget(param, 1f);
		}
		end(); // Pose
		beginPose(hold + 1000);
		for (String param : params) {
			morphTarget(param, 0f);
		}
		end(); // Pose
		end(); // Sequence
	}
	
	private void idle() throws IOException {
		if (!connect()) return;
		beginSequence();
		beginPose(1000);
		//poseTarget("all", "default");
		position("rhand", "rarm", -0.2f, -0.03f, 0.02f);
		position("lhand", "larm", 0.2f, -0.03f, 0.02f);
		poseTarget("rhand", "hands_open-relaxed");
		poseTarget("lhand", "hands_open-relaxed");
		end(); // Pose
		end(); // Sequence
	}

	private void poseTarget(String group, String key) throws IOException {
		send("BEGIN POSE_TARGET");
		send("BODY_GROUP:" + group); 
		send("POSE_KEY:" + key);  
		send("END");
	}
	
	private void position(String joint, String group, float x, float y, float z) throws IOException {
		send("BEGIN POSITION_CONSTRAINT");
		send("BODY_GROUP:" + group); 
		send("JOINT:" + joint); 
		send(String.format(Locale.US, "TARGET:%.2f;%.2f;%.2f", x, y, z));
		send("OFFSET:0.0;0.0;0.0"); 
		send("END");
	}
	
	private void lookAtAng(float x, float y, String mode) throws IOException {
		if (x > 45) x = 45;
		if (x < -45) x = -45;
		x = (float) (10d * Math.tan(Math.toRadians(x)));
		if (y > 45) y = 45;
		if (y < -45) y = -45;
		y = (float) (10d * Math.tan(Math.toRadians(y)));
		lookAt(x, -10, y, mode);
	}
	
	private void lookAt(float x, float y, float z, String mode) throws IOException {
		send("BEGIN LOOK_AT_CONSTRAINT");
		send("BODY_GROUP:" + mode); 
		// FIRST: left/right, neg=left LAST: up/down, pos=up
		send(String.format(Locale.US, "TARGET:%.2f;%.2f;%.2f", x, y, z)); 
		send("END");
	}

	private void morphTarget(String target, float value) throws IOException {
		send("BEGIN MORPH_TARGET");
		send("MORPH_KEY:" + target);
		send("MORPH_VALUE:" + String.format(Locale.US, "%.2f", value));
		send("END");
	}

	private void beginPose(int time) throws IOException {
		send("BEGIN K_POSE");
		send("TIME_POINT:+" + time);
	}
	
	private void beginPose(int time, int hold) throws IOException {
		send("BEGIN K_POSE");
		send("TIME_POINT:+" + time);
		send("HOLD:+" + hold);
	}

	private void beginSequence() throws IOException {
		send("BEGIN K_POSE_SEQUENCE");
		send("CHARACTER:Amber");
		send("START:asap");
	}

	private void end() throws IOException {
		send("END");
	}

	@Override
	public void init() throws InitializationException {
		try {
			idle();
		} catch (IOException e) {
			System.err.println("Cannot connect to EMBR: " + e.getMessage());
		}
	}

	private void send(String command) throws IOException {
		//System.out.println(command);
		outToServer.writeBytes(command + "\n");
	}

	private boolean connect() {
		try {
			socket = new Socket("localhost", 5555);
			//inFromServer = new ParsedInputStream(socket.getInputStream());
			outToServer = new DataOutputStream(socket.getOutputStream());
			//new ClientThread().start();
			return true;
		} catch (Exception e) {
			System.err.println("Cannot connect to EMBR: " + e.getMessage());
		} 
		return false;
	}

	private void closeConnection() {
		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (IOException e) {
		}
	}

}
