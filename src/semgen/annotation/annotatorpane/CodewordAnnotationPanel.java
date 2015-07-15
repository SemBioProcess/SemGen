package semgen.annotation.annotatorpane;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.annotatorpane.subpanels.CompositeAnnotationPanel;
import semgen.annotation.annotatorpane.subpanels.SingularAnnotationPanel;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semgen.annotation.workbench.SemSimTermLibrary.LibraryEvent;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenSeparator;

public class CodewordAnnotationPanel extends AnnotationPanel<CodewordToolDrawer> {
	private static final long serialVersionUID = 1L;

	private AnnotatorButton copyannsbutton;
	private CompositeAnnotationPanel compositepanel;
	private SingularAnnotationPanel singularannpanel;
	
	
	public CodewordAnnotationPanel(AnnotatorWorkbench wb, SemGenSettings sets,
			GlobalActions gacts) {
		super(wb, wb.openCodewordDrawer(), sets, gacts);
		drawUI();
	}

	@Override
	protected void formatHeader(Box mainheader) {	
		if(drawer.isMapped()){
			copyannsbutton = new AnnotatorButton(SemGenIcon.copyicon, "Copy annotations to all mapped variables");
			copyannsbutton.addMouseListener(this);
			mainheader.add(copyannsbutton);
			codewordlabel.setBorder(BorderFactory.createEmptyBorder(5, indent, 5, 10));
		}
		codewordlabel.setText(codewordlabel.getText() + " (" + drawer.getUnits() + ")");
	}

	@Override
	protected void createUniqueElements() {
		createEquationPane();

		compositepanel = new CompositeAnnotationPanel(termlib, drawer, BoxLayout.Y_AXIS);
	
		JLabel compositelabel = new JLabel("Composite annotation");
		compositelabel.setFont(SemGenFont.defaultBold());
		compositelabel.setBorder(BorderFactory.createEmptyBorder(10, indent, 0, 0));
		
		mainpanel.add(compositelabel);
		
		mainpanel.add(Box.createGlue());
		mainpanel.add(compositepanel);
		mainpanel.add(Box.createGlue());
		mainpanel.add(new SemGenSeparator());
		
		JLabel singularannlabel = new JLabel("Singular annotation");
		singularannlabel.setFont(SemGenFont.defaultBold());
		singularannlabel.setBorder(BorderFactory.createEmptyBorder(10, indent, 5, 0));
		mainpanel.add(singularannlabel);
		addSingularAnnotationPanel();
	}
	
	private void addSingularAnnotationPanel() {
		singularannpanel = new SingularAnnotationPanel(drawer, termlib);
		singularannpanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		mainpanel.add(singularannpanel);
	}
	
	private void createEquationPane() {
		JEditorPane eqpane = new JEditorPane();
		eqpane.setEditable(false);
		eqpane.setText(drawer.getEquationasString());
		eqpane.setFont(SemGenFont.defaultItalic(-1));
		eqpane.setOpaque(false);
		eqpane.setAlignmentX(JEditorPane.LEFT_ALIGNMENT);
		eqpane.setBorder(BorderFactory.createEmptyBorder(2, 2*indent, 2, 2));
		eqpane.setBackground(new Color(0,0,0,0));
		
		mainpanel.add(eqpane);
		mainpanel.add(new SemGenSeparator());
	}

	@Override
	public void updateUnique(Observable o, Object arg1) {
		if (arg1==modeledit.propertychanged) {
			compositepanel.onPropertyChange();
		}
		if (o.equals(termlib)) {
			compositepanel.onTermUpdate(arg1);
			if (arg1==LibraryEvent.SINGULAR_TERM_CHANGE || arg1==LibraryEvent.SINGULAR_TERM_CREATED) {
				singularannpanel.refreshSingularAnnotation();
			}
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		if(e.getComponent() == copyannsbutton){
			int x = JOptionPane.showConfirmDialog(this, "Really copy annotations to mapped variables?", "Confirm", JOptionPane.YES_NO_OPTION);
			if(x==JOptionPane.YES_OPTION){
				drawer.copyToLocallyMappedVariables();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}


}
