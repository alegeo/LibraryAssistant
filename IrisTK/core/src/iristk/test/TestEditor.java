package iristk.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import iristk.flow.FlowCompilerException;
import iristk.util.ColorGenerator;
import iristk.util.Mapper;
import iristk.util.XmlUtils;

public class TestEditor {

	JFrame window;
	Mapper colormapper = new Mapper("colormapper", this.getClass().getResourceAsStream("colors.map"));

	public TestEditor() throws FlowCompilerException, SAXException, IOException {
		XmlUtils xmlUtils = new XmlUtils();
		Document doc = xmlUtils.builder.parse(new File("c:/dropbox/iristk/app/chess/src/iristk/app/chess/ChessFlow.flow"));
		window = new JFrame();
		window.setBackground(Color.white);
		window.add(new JScrollPane(new ElementComponent(doc.getDocumentElement()), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
		window.setPreferredSize(new Dimension(800, 600));
		window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String[] args) throws Exception {
		new TestEditor();
	}

	public class ElementComponent extends JPanel {

		public ElementComponent(Element element) {
			super(new BorderLayout());
			setMaximumSize(new Dimension(800, 1000));
			setAlignmentX(LEFT_ALIGNMENT);
			//boolean top = true;
			//if (element.getLocalName().equals("onentry") || element.getLocalName().equals("catch") || element.getLocalName().equals("onSpeech") || element.getLocalName().equals("if")) {
			//	top = false;
			//}
			setBorder(new CompoundBorder(new EmptyBorder(1, 1, 1, 1), new LineBorder(Color.black)));
			//setBorder(new LineBorder(Color.black));

			Color color = ColorGenerator.getColor(colormapper.map(element.getLocalName()));

			JPanel left = new JPanel();
			left.setBackground(color);
			left.add(new JLabel(element.getLocalName()));
			add(left, BorderLayout.LINE_START);

			JPanel right = new JPanel(new BorderLayout(0, 0));
			add(right, BorderLayout.CENTER);

			if (element.getAttributes().getLength() > 0) {
				JPanel header = new JPanel();
				header.setBackground(color);
				header.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 2));

				for (int i = 0; i < element.getAttributes().getLength(); i++) {
					Node attr = element.getAttributes().item(i);
					header.add(new JLabel(attr.getLocalName()));
					JTextField afield = new JTextField(attr.getTextContent());
					afield.setBackground(color);
					afield.setFont(new Font("Courier", Font.PLAIN, 12));
					afield.setForeground(Color.blue);
					afield.setBorder(new EmptyBorder(0, 0, 0, 0));
					header.add(afield);
				}
				right.add(header, BorderLayout.PAGE_START);
			}

			if (element.getChildNodes().getLength() > 0) {
				final JPanel contents = new JPanel();
				contents.setLayout(new BoxLayout(contents, BoxLayout.PAGE_AXIS));
				//contents.setBorder(new EmptyBorder(5, 5, 5, 5));
				for (int i = 0; i < element.getChildNodes().getLength(); i++) {
					Component comp = null;
					final Node child = element.getChildNodes().item(i);
					if (child instanceof Element) {
						comp = new ElementComponent((Element) child);
					} else if (child instanceof Text) {
						String t = ((Text)child).getNodeValue().trim();
						if (t.length() > 0) {
							JTextArea text = new JTextArea();
							text.setText(t);
							comp = text;
						}
					}
					if (comp != null) {
						contents.add(comp);
						if (child instanceof Element) {
							contents.add(new Insert(comp, (Element)child));
						}
					}
				}
				right.add(contents, BorderLayout.CENTER);
			}
		}

	}
	
	public class Insert extends JPanel {
		
		public Insert(final Component fcomp, final Element element) {
			super();
			setBackground(Color.lightGray);
			setPreferredSize(new Dimension(100, 3));
			addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
					setBackground(Color.lightGray);
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					setBackground(Color.gray);
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					Component comp = new ElementComponent(element);
					Container parent = fcomp.getParent();
					parent.add(comp, getComponentIndex(fcomp));
					parent.add(new Insert(comp, element), getComponentIndex(fcomp));
					parent.revalidate();
				}
			});
		}
		
	}

	public static final int getComponentIndex(Component component) {
		if (component != null && component.getParent() != null) {
			Container c = component.getParent();
			for (int i = 0; i < c.getComponentCount(); i++) {
				if (c.getComponent(i) == component)
					return i;
			}
		}
		return -1;
	}

}
