package iristk.system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.UIManager;

import iristk.util.ColorGenerator;
import iristk.util.Record;

public class IrisMonitorGUI implements IrisMonitor, ActionListener {

	private static final int STATE_HEIGHT = 25;
	private static final int REDRAW_THRESHOLD = 100;
	private static final int TRACK_LABEL_WIDTH = 100;
	private static final int REDRAW_ADJUST = 300;
	
	JFrame window;
	private IrisSystem system;
	private TrackPane trackPane;
	private BufferedImage canvas; 
	private long startTime = -1;

	private int msecPerPixel = 64;
	
	public int canvasOffset = 0;
	
	private ArrayList<Track> tracks = new ArrayList<Track>();
	private Timer timer;
	private JPanel controlPane;
	private JCheckBox followNow;
	
	private float[] skips = new float[]{0.1f, 0.5f, 1f, 5f, 10f, 15f, 20f, 30f, 60f, 300f, 600f};
	private JTextArea console;
	private JSplitPane splitPane;
	
	public IrisMonitorGUI(IrisSystem system) {
		this.system = system;
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		window = new JFrame("IrisTK Monitor");
		window.setPreferredSize(new Dimension(1024, 768));
		
		trackPane = new TrackPane();
		
		controlPane = new JPanel(new BorderLayout());
		
		followNow = new JCheckBox("Follow");
		followNow.setSelected(true);
		controlPane.add(followNow, BorderLayout.PAGE_START);
		
		console = new JTextArea();
		console.setEditable(false);
		console.setFont(new Font("Courier", Font.PLAIN, 12));
		controlPane.add(new JScrollPane(console));
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, trackPane, controlPane);
		splitPane.setDividerLocation(400);
		window.add(splitPane);
		window.pack();
		window.setVisible(true);
		system.addMonitor(this);
		timer = new Timer(100, this);
		timer.start();
	}

	private void zoomCanvas(int dir, int x) {
		if (dir > 0 && msecPerPixel < 600) {
			int time = canvasTime(x);
			msecPerPixel = msecPerPixel * 2;
			canvasOffset = (time / msecPerPixel) + TRACK_LABEL_WIDTH - x;
			drawCanvas();
			trackPane.repaint();
		} else if (dir < 0 && msecPerPixel > 1) {
			int time = canvasTime(x);
			msecPerPixel = msecPerPixel / 2;
			canvasOffset = (time / msecPerPixel) + TRACK_LABEL_WIDTH - x;
			drawCanvas();
			trackPane.repaint();
		}
	}

	private void moveCanvas(int diff) {
		canvasOffset += diff;
		drawCanvas();
		trackPane.repaint();
	}

	private int canvasX(long time) {
		return (int) (time / msecPerPixel) - canvasOffset + TRACK_LABEL_WIDTH;
	}
	
	private int canvasX() {
		return canvasX(System.currentTimeMillis() - startTime);
	}
	
	private int canvasTime(int x) {
		return (x + canvasOffset - TRACK_LABEL_WIDTH) * msecPerPixel;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (canvas != null && startTime != -1) {
			if (followNow.isSelected()) {
				boolean redraw = false;
				while (canvasX() > canvas.getWidth() - REDRAW_THRESHOLD) {
					canvasOffset += REDRAW_ADJUST;
					redraw = true;
				}
				while (canvasX() < TRACK_LABEL_WIDTH) {
					canvasOffset -= 100;
					redraw = true;
				}
				if (redraw) {
					drawCanvas();
				}
			}
			trackPane.repaint();
		}
	}
	
	private synchronized void drawCanvas() {
		int cHeight = STATE_HEIGHT;
		for (Track track : tracks) {
			cHeight += track.getHeight();
		}
		canvas = new BufferedImage(trackPane.getWidth(), cHeight, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = canvas.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, canvas.getWidth(), cHeight);
		g.setColor(Color.gray);
		g.fillRect(0, 0, TRACK_LABEL_WIDTH, cHeight - 1);
		for (Track track : tracks) {
			g.setColor(Color.black);
			g.drawLine(0, track.top(), canvas.getWidth(), track.top());		
			
			g.setColor(Color.white);
			g.drawString(track.name, 5, track.top() + 16);
			
			// Don't draw states that will be overdrawn anyway, find the first one that we should draw
			int start = 0;
			for (int i = track.states.size() - 1; i >= 0; i--) {
				if (track.states.get(i).willCover() && canvasX(track.states.get(i).time) < TRACK_LABEL_WIDTH) {
					start = i;
					break;
				}
			}
			for (int i = start; i < track.states.size(); i++) {
				track.states.get(i).draw(g);
			}
			for (EventMark event : track.events) {
				event.draw(g);
			}
		}
		
		// Draw timeline
		g.setColor(Color.lightGray);
		g.fillRect(0, canvas.getHeight() - STATE_HEIGHT, canvas.getWidth(), STATE_HEIGHT);
		g.setColor(Color.black);
		float skip = 1f;
		float minw = Float.MAX_VALUE;
		for (float s : skips) {
			float w = Math.abs(((s * 1000f) / msecPerPixel) - 100);
			if (w < minw) {
				skip = s;
				minw = w;
			}
		}
		float from = Math.round(canvasOffset * msecPerPixel / 1000f);
		from = Math.round(from / skip) * skip;
		float to = from + Math.round(canvas.getWidth() * msecPerPixel / 1000f);
		for (float t = from; t < to; t += skip) {
			String label;
			if (skip < 1)
				label = String.format(Locale.US, "%.1fs", t);
			else if (skip < 60)
				label = String.format(Locale.US, "%.0fs", t);
			else
				label = String.format(Locale.US, "%.0fm", t / 60);
			int x = canvasX((int)(t * 1000f));
			g.drawLine(x, canvas.getHeight() - STATE_HEIGHT, x, canvas.getHeight());
			g.drawString(label, x + 3, canvas.getHeight() - 5);
		}
	}

	private void setupTracks() {
		startTime = System.currentTimeMillis();
		for (int i = 0; i < system.getModules().size(); i++) {
			tracks.add(new Track(system.getModules().get(i).getName()));
		}
		drawCanvas();
		trackPane.repaint();
	}
	
	@Override
	public synchronized void monitorEvent(String sender, Event event) {
		if (event.getName().equals("monitor.system.start")) {
			setupTracks();
		} else {
			Track track = getTrack(sender);
			if (track != null) {
				EventMark mark = new EventMark(track, event);
				track.events.add(mark);
				if (canvas != null) {
					Graphics2D g = canvas.createGraphics();
					mark.draw(g);
				}
			}
		}
	}
	
	private Track getTrack(String name) {
		for (Track track : tracks) {
			if (track.name.equals(name))
				return track;
		}
		return null;
	}
	
	@Override
	public synchronized void monitorState(String sender, String[] states) {
		Track track = getTrack(sender);
		if (track == null) 
			return;
		if (states == null) {
			states = new String[]{null};
		}
		String[] prevStates;
		if (track.states.size() > 0)
			prevStates = track.states.get(track.states.size()-1).states;
		else
			prevStates = new String[0];
		StateMark stateMark = new StateMark(track, states, prevStates);
		track.states.add(stateMark);
		if (canvas != null) {
			if (track.nStates < states.length) {
				track.nStates = states.length;
				drawCanvas();
			}
			Graphics2D g = canvas.createGraphics();
			stateMark.draw(g);
		}
	}
	
	private class EventMark {
		
		public Event event;
		public int time;
		private Track track;
		
		public EventMark(Track track, Event event) {
			this.event = event;
			this.track = track;
			this.time = (int) (System.currentTimeMillis() - startTime);
		}
		
		public int getX() {
			return canvasX(time);
		}
		
		public int getY() {
			return track.top();
		}
		
		public void draw(Graphics2D g) {
			int x = getX();
			if (x <= TRACK_LABEL_WIDTH || x > canvas.getWidth())
				return;
			int y = getY();
			if (event.getName().startsWith("monitor")) {
				g.setColor(Color.orange);
			} else if (event.getName().startsWith("sense")) {
				g.setColor(Color.blue);
			} else if (event.getName().startsWith("action")) {
				g.setColor(Color.red);
			} else {
				g.setColor(Color.gray);
			}
			g.drawLine(x, y + 1, x, y + STATE_HEIGHT - 1);
		}

		public String getLabel() {
			Record lrec = new Record(event);
			if (event.getId() != null)
				lrec.put("id", event.getId());
			if (event.getTime() != null)
				lrec.put("time", event.getTime());
			return event.getName() + "\n" + lrec.toStringIndent();
		}
		
	}
	
	private static boolean contains(String[] array, String value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null && array[i].equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	private class StateMark {
		
		public String[] states;
		public boolean[] keepState;
		public int time;
		private Track track;
		
		public StateMark(Track track, String[] newStates, String[] prevStates) {
			states = new String[Math.max(newStates.length, prevStates.length)];
			Arrays.fill(states, null);
			keepState = new boolean[states.length];
			Arrays.fill(keepState, false);
			for (int i = 0; i < prevStates.length; i++) {
				if (contains(newStates, prevStates[i])) {
					keepState[i] = true;
					states[i] = prevStates[i];
					continue;
				}
			}
			OUTER:
			for (int i = 0; i < newStates.length; i++) {
				String newState = newStates[i];
				if (newState != null && !newState.equals("") && !contains(prevStates, newState)) {
					for (int j = 0; j < states.length; j++) {
						if (states[j] == null) {
							states[j] = newState;
							continue OUTER;
						}
					}
				}
			}
			this.track = track;
			this.time = (int) (System.currentTimeMillis() - startTime);
		}
		
		public boolean willCover() {
			for (int i = 0; i < states.length; i++) {
				if (keepState[i]) {
					return false;
				}
			}
			return true;
		}
		
		public void draw(Graphics2D g) {
			int x = canvasX(time);
			if (x > canvas.getWidth())
				return;
			if (x < TRACK_LABEL_WIDTH)
				x = TRACK_LABEL_WIDTH;
			int y = track.top() + STATE_HEIGHT;
			for (int i = 0; i < states.length; i++) {
				String label = states[i];
				if (keepState[i]) {
				} else if (label == null) {
					g.setColor(Color.white);
					g.fillRect(x, y, canvas.getWidth() - x, STATE_HEIGHT);
				} else {
					g.setColor(ColorGenerator.getColor(label));
					g.fillRect(x, y, canvas.getWidth() - x, STATE_HEIGHT);
					g.setColor(Color.gray);
					g.drawString(label, x + 2, y + 18);
					g.drawLine(x, y, x, y + STATE_HEIGHT);
				}
				y += STATE_HEIGHT;
			}
		}
		
	}
	
	private class Track {
		
		public ArrayList<EventMark> events = new ArrayList<EventMark>();
		public ArrayList<StateMark> states = new ArrayList<StateMark>();
		public int nStates = 0;
		public String name;
		
		public Track(String name) {
			this.name = name;
		}
		
		public int top() {
			int top = 0;
			for (int i = 0; i < tracks.size(); i++) {
				if (tracks.get(i) == this)
					return top;
				else
					top += tracks.get(i).getHeight();
			}
			return 0;
		}

		public int getHeight() {
			return STATE_HEIGHT * (nStates + 1);
		}

		public ArrayList<EventMark> getEvents(int time) {
			ArrayList<EventMark> result = new ArrayList<EventMark>();
			for (EventMark event : events) {
				if (Math.abs(event.time - time) <= msecPerPixel * 2) {
					result.add(event);
				}
			}
			return result;
		}
		
	}
	
	private class TrackPane extends JPanel implements MouseMotionListener, MouseWheelListener, MouseListener {
		
		private List<EventMark> currentEvents = null;

		public TrackPane() {
			addMouseListener(this);
			addMouseMotionListener(this);
			addMouseWheelListener(this);
		}

		@Override
		protected void paintComponent(Graphics g) {
			if (canvas != null) {
				if (canvas.getWidth() != trackPane.getWidth()) 
					drawCanvas();
				g.clearRect(0, 0, trackPane.getWidth(), trackPane.getHeight());
				g.drawImage(canvas, 0, 0, null);
				g.setColor(Color.green);
				int x = canvasX();
				if (x > TRACK_LABEL_WIDTH)
					g.drawLine(x, 0, x, canvas.getHeight() - 1);
				if (currentEvents != null && currentEvents.size() > 0) {
					String label = "";
					for (EventMark mark : currentEvents) {
						if (label.length() > 0)
							label += "\n";
						label += mark.getLabel();
					}
					drawLabel(g, label, currentEvents.get(0).getX(), currentEvents.get(0).getY() + STATE_HEIGHT);
				}
			}
		}

		private List<EventMark> getEvents(int x, int y) {
			int time = canvasTime(x);
			int top = 0;
			for (int i = 0; i < tracks.size(); i++) {
				if (y > top && y < top + STATE_HEIGHT) {
					return tracks.get(i).getEvents(time);
				} else {
					top += tracks.get(i).getHeight();
				}
			}
			return null;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			currentEvents = getEvents(e.getX(), e.getY());
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int rot = e.getWheelRotation();
			int mod = e.getModifiersEx();
			if ((mod & InputEvent.ALT_DOWN_MASK) > 0) {
				zoomCanvas(rot, e.getX());
			} else {
				moveCanvas(rot * 50);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			List<EventMark> marks = getEvents(e.getX(), e.getY());
			if (marks != null)
				for (EventMark mark : marks)
					console.append(mark.getLabel() + "\n");
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
		
	}
	
	private static void drawLabel(Graphics g, String text, int x, int y) {
		FontMetrics m = g.getFontMetrics(g.getFont());
		String[] label = text.split("\n");
		int w = 0;
		for (String l : label) {
			w = Math.max(m.stringWidth(l), w);
		}
		w += 4;
		int h = m.getHeight() * label.length + 4;
		g.setColor(new Color(255, 255, 200));
		g.fillRect(x, y, w, h);
		g.setColor(Color.black);
		g.drawRect(x, y, w, h);
		for (int i = 0; i < label.length; i++) {
			g.drawString(label[i], x + 3, y + (i + 1) * m.getHeight());
		}
	}
	
	public class Button extends JButton {

		public Button(String label, ActionListener actionListener) {
			super(label);
			addActionListener(actionListener);
		}

	}

	
}
