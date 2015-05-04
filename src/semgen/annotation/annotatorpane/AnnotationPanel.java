package semgen.annotation.annotatorpane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.annotatorpane.subpanels.SingularAnnotationPanel;
import semgen.annotation.common.AnnotationClickableTextPane;
import semgen.annotation.dialog.TextChangeDialog;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.WBEvent;
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semgen.annotation.workbench.drawers.AnnotatorDrawer;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semsim.SemSimObject;

public abstract class AnnotationPanel<P extends AnnotatorDrawer<? extends SemSimObject>> extends JPanel implements MouseListener, Observer {
	private static final long serialVersionUID = 1L;

	protected AnnotatorWorkbench workbench;
	protected P drawer;
	protected SemGenSettings settings;
	protected GlobalActions globalacts;
	
	protected JPanel mainpanel = new JPanel();
	protected JLabel codewordlabel = new JLabel();
	protected AnnotatorButton humremovebutton = new AnnotatorButton(SemGenIcon.eraseiconsmall, "Remove this annotation");
	protected AnnotationClickableTextPane humandefpane;
	
	protected SingularAnnotationPanel singularannpanel;
	protected JLabel singularannlabel = new JLabel("Singular annotation");

	protected int indent = 15;
	
	public AnnotationPanel(AnnotatorWorkbench wb, P tooldrawer, SemGenSettings sets, GlobalActions gacts) {
		super(new BorderLayout());
		workbench = wb;
		workbench.addObserver(this);
		settings = sets;
		globalacts = gacts;
		drawer = tooldrawer;
		drawer.addObserver(this);
		
		setBackground(SemGenSettings.lightblue);
	}
	
	protected void drawUI() {
		mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
		mainpanel.setBackground(SemGenSettings.lightblue);
		
		createHeader();
		createHumanDefinitionPanel();
		createUniqueElements();
		singularannlabel.setFont(SemGenFont.defaultBold());
		singularannlabel.setBorder(BorderFactory.createEmptyBorder(10, indent, 5, 0));
		mainpanel.add(singularannlabel);
		addSingularAnnotationPanel();
		
		add(mainpanel, BorderLayout.NORTH);
		add(Box.createVerticalGlue(), BorderLayout.SOUTH);
		
		refreshData();
		
		setVisible(true);
		validate();
		repaint();
	}
	
	private void createHeader() {
		codewordlabel.setBorder(BorderFactory.createEmptyBorder(5, indent, 5, 10));
		codewordlabel.setFont(SemGenFont.defaultBold(3));
		codewordlabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		codewordlabel.setText(drawer.getCodewordName());
		
		Box mainheader = Box.createHorizontalBox();
		mainheader.setBackground(SemGenSettings.lightblue);
		mainheader.setAlignmentX(LEFT_ALIGNMENT);
		
		mainheader.add(codewordlabel);
		formatHeader(mainheader);
		mainheader.add(Box.createGlue());
		mainpanel.add(mainheader);
	}
	
	private void createHumanDefinitionPanel() {
		JPanel humandefpanel = new JPanel(new BorderLayout());
		humandefpanel.setBackground(SemGenSettings.lightblue);
		humandefpanel.setBorder(BorderFactory.createEmptyBorder(0, indent, 0, 0));
		humandefpanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		
		JPanel humandefsubpanel = new JPanel();
		humandefsubpanel.setBackground(SemGenSettings.lightblue);
		humandefsubpanel.setLayout(new BoxLayout(humandefsubpanel, BoxLayout.X_AXIS));
		
		humandefpane = new AnnotationClickableTextPane("",indent, drawer.isEditable());
		humandefpane.setAlignmentX(JTextArea.LEFT_ALIGNMENT);
		humandefpane.addMouseListener(this);
		refreshFreeText();
		
		humandefsubpanel.add(humandefpane);
		humandefsubpanel.add(humremovebutton);
		humremovebutton.addMouseListener(this);
		humandefpanel.add(humandefsubpanel, BorderLayout.WEST);
		humandefpanel.add(Box.createGlue(), BorderLayout.EAST);
		
		mainpanel.add(humandefpanel);
	}
	
	protected void refreshFreeText() {
		humandefpane.setText(drawer.getHumanReadableDef());
		if (drawer.hasHumanReadableDef()) {
			humandefpane.setForeground(Color.blue);
			humremovebutton.setEnabled(true);
		}
		else {
			humandefpane.setForeground(Color.gray);
			humremovebutton.setEnabled(false);
		}
	}
	
	private void addSingularAnnotationPanel() {
		singularannpanel = new SingularAnnotationPanel(drawer);
		singularannpanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		mainpanel.add(singularannpanel);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (arg==WBEvent.freetextrequest) {
			changeFreeText();
		}
		if (arg==modeledit.freetextchange) {
			refreshFreeText();
		}
		updateUnique(o, arg);
	}
		
	private void changeFreeText() {
		String current = drawer.getHumanReadableDef();
		TextChangeDialog hde = new TextChangeDialog("Enter free-text description", drawer.getCodewordName(), current);
		if (!hde.getNewDescription().equals(current)) {
			drawer.setHumanReadableDefinition(hde.getNewDescription());
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		Object obj = e.getSource();
		if (obj==humremovebutton) {
			drawer.setHumanReadableDefinition("");
		}
		if (obj==humandefpane) {
			changeFreeText();
		}
	}
	
	protected abstract void formatHeader(Box mainheader);
	protected abstract void createUniqueElements();
	protected abstract void refreshData();	
	protected abstract void updateUnique(Observable o, Object arg1);	

	protected class AnnotatorButton extends JLabel {
		private static final long serialVersionUID = 1L;
		
		AnnotatorButton(Icon image, String tooltip) {
			super(image);
			addMouseListener(new LabelMouseBehavior(this));
			setToolTipText(tooltip);
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
	
	public void destroy() {
		workbench.deleteObserver(this);
		drawer.deleteObserver(this);
	}
}
