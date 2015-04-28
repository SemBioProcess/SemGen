package semgen.annotation.annotatorpane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.drawers.AnnotatorDrawer;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;

public abstract class AnnotationPanel<P extends AnnotatorDrawer<?>> extends JPanel implements MouseListener, Observer {
	private static final long serialVersionUID = 1L;

	protected AnnotatorWorkbench workbench;
	protected AnnotatorDrawer drawer;
	protected SemGenSettings settings;
	protected GlobalActions globalacts;
	
	protected JLabel codewordlabel = new JLabel("");
	protected AnnotatorButton humremovebutton = new AnnotatorButton(SemGenIcon.eraseiconsmall, "Remove this annotation");

	protected JLabel singularannlabel = new JLabel("Singular annotation");

	protected int indent = 15;
	
	public AnnotationPanel(AnnotatorWorkbench wb, P tooldrawer, SemGenSettings sets, GlobalActions gacts) {
		workbench = wb;
		workbench.addObserver(this);
		settings = sets;
		globalacts = gacts;
		drawer = tooldrawer;
		drawer.addObserver(this);
		
		setBackground(SemGenSettings.lightblue);
		setLayout(new BorderLayout());
	}
	
	protected void drawUI() {
		JPanel mainpanel = new JPanel();
		constructMainPanel(mainpanel);
		createHeader();
		
		add(mainpanel, BorderLayout.NORTH);
		add(Box.createVerticalGlue(), BorderLayout.SOUTH);
		
		refreshUI();
	}
	
	private void createHeader() {
		codewordlabel.setBorder(BorderFactory.createEmptyBorder(5, indent, 5, 10));
		codewordlabel.setFont(SemGenFont.defaultBold(3));
		codewordlabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		
		Box mainheader = Box.createHorizontalBox();
		mainheader.setBackground(SemGenSettings.lightblue);
		mainheader.setAlignmentX(LEFT_ALIGNMENT);
		
		
		mainheader.add(codewordlabel);
	}
	
	private void createDefinitionPanel() {
		
	}
	
	protected abstract void formatHeader();
	protected abstract void constructMainPanel(JPanel main);
	protected abstract void refreshUI();
	protected abstract void constructHumanDefinitionPanel();
	

	protected class AnnotatorButton extends JLabel {
		private static final long serialVersionUID = 1L;
		
		AnnotatorButton(Icon image, String tooltip) {
			super(image);
			addMouseListener(new LabelMouseBehavior(this));
			setToolTipText(tooltip);
			setEnabled(drawer.isEditable());
			setBorder(BorderFactory.createEmptyBorder(1,1,1,1));	
		}
		
		private class LabelMouseBehavior extends MouseAdapter {
			JLabel label;
			public LabelMouseBehavior(JLabel lbl) {
				label = lbl;
			}
			
			public void mouseEntered(MouseEvent e) {
				label.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			public void mouseExited(MouseEvent e) {
				label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			public void mousePressed(MouseEvent arg0) {
				label.setBorder(BorderFactory.createLineBorder(Color.blue,1));
			}
			
			public void mouseReleased(MouseEvent arg0) {
				label.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
			}

		}
	}
	
}
