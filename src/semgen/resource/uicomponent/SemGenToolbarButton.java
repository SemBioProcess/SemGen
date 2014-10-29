package semgen.resource.uicomponent;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class SemGenToolbarButton extends JButton {
	private static final long serialVersionUID = 1L;

	public SemGenToolbarButton(ImageIcon icon) {
		super(icon);
		
		setSize(new Dimension(30, 30));
		setRolloverEnabled(true);
		setPreferredSize(new Dimension(30, 30));
		setAlignmentY(JButton.TOP_ALIGNMENT);
		addMouseListener(new btnbehavior());
	}
	
	private class btnbehavior extends MouseAdapter {
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(true);
				button.setContentAreaFilled(true);
				button.setOpaque(true);
			}
		}

		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(false);
				button.setContentAreaFilled(false);
				button.setOpaque(false);
			}
		}
	}
}
