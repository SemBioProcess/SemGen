package semgen.annotation.annotatorpane;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Observable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JEditorPane;
import javax.swing.JLabel;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.utilities.SemGenFont;
import semgen.utilities.SemGenIcon;
import semgen.utilities.uicomponent.SemGenSeparator;

public class CodewordAnnotationPanel extends AnnotationPanel<CodewordToolDrawer> {
	private static final long serialVersionUID = 1L;

	protected AnnotatorButton copyannsbutton = new AnnotatorButton(SemGenIcon.copyicon, "Copy annotations to all mapped variables");
	//private CompositeAnnotationPanel compositepanel;
	
	public CodewordAnnotationPanel(AnnotatorWorkbench wb, SemGenSettings sets,
			GlobalActions gacts) {
		super(wb, wb.openCodewordDrawer(), sets, gacts);
		drawUI();
	}

	@Override
	protected void formatHeader(Box mainheader) {	
		if(drawer.isMapped()){
			mainheader.add(copyannsbutton);
			codewordlabel.setBorder(BorderFactory.createEmptyBorder(5, indent, 5, 10));
		}
		codewordlabel.setText(codewordlabel.getText() + " (" + drawer.getUnits() + ")");
	}

	@Override
	protected void createUniqueElements() {
		createEquationPane();

		//compositepanel = new CompositeAnnotationPanel(workbench, BoxLayout.Y_AXIS, settings);
		
		JLabel compositelabel = new JLabel("Composite annotation");
		compositelabel.setFont(SemGenFont.defaultBold());
		compositelabel.setBorder(BorderFactory.createEmptyBorder(10, indent, 0, 0));
		
		mainpanel.add(compositelabel);
		
		mainpanel.add(Box.createGlue());
		//mainpanel.add(compositepanel);
		mainpanel.add(Box.createGlue());
		mainpanel.add(new SemGenSeparator());
		
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
	protected void refreshData() {

	}

	@Override
	public void updateUnique(Observable o, Object arg1) {
		if (arg1==modeledit.compositechanged) {
			//compositepanel.refreshUI();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
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
