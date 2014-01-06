package iristk.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

public class TestFrame extends JFrame {

	private JPanel contentPane;
	private JPanel panel;
	private JPanel center;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					TestFrame frame = new TestFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public TestFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 537, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout layout = new GridBagLayout();
		contentPane.setLayout(layout);
		setContentPane(contentPane);

		panel = new JPanel(new FlowLayout());
		
		panel.setBackground(Color.red);
		
		contentPane.add(panel, BorderLayout.PAGE_START);

		//layout.putConstraint(SpringLayout.WEST, panel, 5, SpringLayout.WEST, contentPane);
		//layout.putConstraint(SpringLayout.NORTH, panel, 5, SpringLayout.NORTH, contentPane);
		//layout.putConstraint(SpringLayout.EAST, panel, -5, SpringLayout.EAST, contentPane);

		JLabel lblNewLabel = new JLabel("New label asd ad ad ad ad ad");
		panel.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("New label ad adas ad a");
		panel.add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("New label");
		panel.add(lblNewLabel_2);

		JLabel lblNewLabel_3 = new JLabel("New label ada asd ad ad");
		panel.add(lblNewLabel_3);

		JLabel lblNewLabel_4 = new JLabel("New label");
		panel.add(lblNewLabel_4);

		center = new JPanel(new FlowLayout());
		center.setBackground(Color.blue);
		contentPane.add(center, BorderLayout.CENTER);

		JLabel label = new JLabel("New label asd ad ad ad ad ad");
		center.add(label, BorderLayout.CENTER);

		JLabel label_1 = new JLabel("New label");
		center.add(label_1, BorderLayout.CENTER);

		JLabel label_2 = new JLabel("New label ada asd ad ad");
		center.add(label_2, BorderLayout.CENTER);

		JLabel label_3 = new JLabel("New label");
		center.add(label_3, BorderLayout.CENTER);

		JLabel label_4 = new JLabel("New label ad adas ad a");
		center.add(label_4, BorderLayout.CENTER);
		
		//System.out.println(center.getMinimumSize());
	}

}
