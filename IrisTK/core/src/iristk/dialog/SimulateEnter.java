package iristk.dialog;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import iristk.system.Event;
import iristk.system.IrisModule;

public class SimulateEnter extends IrisModule {

	private HashMap<String, JLabel> labels = new HashMap<String, JLabel>();
	private Integer nUsers = 2;
	private ArrayList<String> present = new ArrayList<String>();
	Color[] colors = new Color[]{new Color(57, 174, 212), new Color(212, 57, 86)};
	
	public SimulateEnter() {
	}

	public void setUsers(Integer nUsers) {
		if (nUsers != null)
			this.nUsers = nUsers;
	}
	
	@Override
	public void init() {		
		JFrame window = new JFrame("Simulate Enter");
		window.setLayout(new FlowLayout());

		for (int useri = 1; useri <= nUsers; useri++) {
			final int userI = useri;
			final String mic = "microphone-" + useri;
			final String user = "user-" + useri;
			TitledBorder title = BorderFactory.createTitledBorder(user);
			title.setTitleColor(colors[useri-1]);
			JPanel userPanel = new JPanel(new FlowLayout());
			userPanel.setBorder(title);
			title.setBorder(BorderFactory.createLineBorder(colors[useri-1]));

			JButton button = new JButton("Enter");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Event enter = new Event("sense.body");
					enter.put("agent", user);
					enter.put("location", mic);
					enter.put("proximate", true);
					if (userI == 1)
						enter.put("x", new Float(-20));
					else
						enter.put("x", new Float(20));
					enter.put("y", new Float(0));
					send(enter);
				}
			});
			userPanel.add(button);
			button = new JButton("Leave");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Event leave = new Event("sense.body");
					leave.put("agent", user);
					leave.put("location", mic);
					leave.put("proximate", false);
					send(leave);
				}
			});
			userPanel.add(button);
			JLabel ulabel = new JLabel("PRESENT");
			ulabel.setForeground(Color.gray);
			userPanel.add(ulabel);
			labels.put(user, ulabel);
			window.add(userPanel);
		}
		
		/*
		if (calibration != null) {
			JButton button = new JButton("Calibrate");
			window.add(button);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					calibration.calibrate();
				}
			});
		}
		*/

		window.pack();
		window.setVisible(true);
	}

	@Override
	public void onEvent(Event event) {
		if (event.getName().startsWith("sense.enter")) {
			String agent = event.getString("agent");
			labels.get(agent).setForeground(Color.green);
			present.add(agent);
			monitor();
		} else if (event.getName().startsWith("sense.leave")) {
			String agent = event.getString("agent");
			labels.get(agent).setForeground(Color.gray);
			present.remove(agent);
			monitor();
		}
	}

	private void monitor() {
		monitorState(present.toArray(new String[0]));
	}
	

}
