package semgen.utilities.uicomponent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import semgen.SemGenSettings;
import semgen.utilities.SemGenFont;

public class SemGenTabToolbar extends JToolBar {
	private static final long serialVersionUID = 1L;
	protected SemGenSettings settings;
	
	public SemGenTabToolbar(SemGenSettings sets) {
		settings = sets;
		setFloatable(false);
		setOpaque(true);
		setAlignmentY(JPanel.TOP_ALIGNMENT);
	}
	
	public SemGenTabToolbar(int orient) {
		super(orient);
		setFloatable(false);
		setOpaque(true);
		setAlignmentY(JPanel.TOP_ALIGNMENT);
	}
	
	protected class ToolBarLabel extends JLabel {
		private static final long serialVersionUID = 1L;

		public ToolBarLabel(String caption) {
			super(caption);
			setFont(SemGenFont.defaultBold());
		}
	}
	
	protected class SemGenToolbarButton extends JButton {
		private static final long serialVersionUID = 1L;

		public SemGenToolbarButton(ImageIcon icon) {
			super(icon);
			
			setSize(new Dimension(30, 30));
			setRolloverEnabled(true);
			setPreferredSize(new Dimension(30, 30));
			setAlignmentY(JButton.TOP_ALIGNMENT);
		}
	}
	protected class SemGenToolbarRadioButton extends JRadioButton {
		private static final long serialVersionUID = 1L;
		private Color selcol = new Color(233,236,242);

		public SemGenToolbarRadioButton(ImageIcon icon) {
			super(icon);
			setOpaque(false);
			setMaximumSize(new Dimension(29, 29));
			setRolloverEnabled(true);
			setPreferredSize(new Dimension(29, 29));
			setAlignmentY(JButton.TOP_ALIGNMENT);
			addMouseListener(new btnbehavior(this));
			setHorizontalAlignment(SwingConstants.CENTER);
			setVerticalTextPosition(SwingConstants.CENTER);
			setBorderPainted(true);

			
			setBackground(selcol);
		}
		
		public void toggleSelectionGraphic() {
			radioMouseGraphics(this, isSelected());
		}
		
		private class btnbehavior extends MouseAdapter {
			AbstractButton button;
			public btnbehavior(AbstractButton btn) {
				button = btn;
			}
			public void mouseEntered(MouseEvent e) {
				if (!isSelected() && isEnabled()) {
					radioMouseGraphics(button, true);
				}
			}

			public void mouseExited(MouseEvent e) {
				if (!isSelected() && isEnabled()) {
					radioMouseGraphics(button, false);
				}
			}
		}
	}
	
	private void radioMouseGraphics(AbstractButton button, boolean dostuff) {
		button.setOpaque(dostuff);
		if (dostuff) {
			button.setBorder(BorderFactory.createRaisedBevelBorder());
		}
		else button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
	}
}
