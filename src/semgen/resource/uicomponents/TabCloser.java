package semgen.resource.uicomponents;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

public class TabCloser extends JButton{

	private static final long serialVersionUID = 4203672446382874318L;

	public TabCloser() {
		setPreferredSize(new Dimension(17, 17));
		setToolTipText("close this tab");
		// Make the button looks the same for all Laf's
		setUI(new BasicButtonUI());
		// Make it transparent
		setContentAreaFilled(false);
		// No need to be focusable
		setFocusable(false);
		setBorder(BorderFactory.createEtchedBorder());
		setBorderPainted(false);
		setText("x");
		// Making nice rollover effect
		// we use the same listener for all buttons
		addMouseListener(buttonMouseListener);
		// we use the same listener for all buttons
		setRolloverEnabled(true);
		// Close the proper tab by clicking the button
	}

	private final static MouseListener buttonMouseListener = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(true);
			}
		}
	};
}