package semgen.annotation.common;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;


public class ComponentPanelLabel extends JLabel {
	private static final long serialVersionUID = 1L;

	public ComponentPanelLabel(Icon icon, String tooltip) {
		super(icon);
		setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		setBackground(Color.white);
		addMouseListener(new LabelMouseBehavior());
		setToolTipText(tooltip);
	}
	
	public void onClick() {}
	
	class LabelMouseBehavior extends MouseAdapter {
		public void mouseClicked(MouseEvent arg0) {
			if (arg0.getComponent().isEnabled()) {
				onClick();
			}
		}
		
		public void mouseEntered(MouseEvent e) {
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		public void mouseExited(MouseEvent e) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		public void mousePressed(MouseEvent arg0) {
			if (arg0.getComponent().isEnabled()) {
				setBorder(BorderFactory.createLineBorder(Color.blue,1));
			}
		}

		public void mouseReleased(MouseEvent arg0) {
			setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		}
	}
}