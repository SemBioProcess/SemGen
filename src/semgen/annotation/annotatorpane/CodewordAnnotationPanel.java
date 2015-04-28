package semgen.annotation.annotatorpane;

import java.awt.event.MouseEvent;
import java.util.Observable;

import javax.swing.JPanel;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.AnnotatorWorkbench.modeledit;
import semgen.annotation.workbench.drawers.CodewordToolDrawer;
import semgen.utilities.SemGenIcon;

public class CodewordAnnotationPanel extends AnnotationPanel<CodewordToolDrawer> {

	private static final long serialVersionUID = 1L;

	protected AnnotatorButton copyannsbutton = new AnnotatorButton(SemGenIcon.copyicon, "Copy annotations to all mapped variables");
	
	public CodewordAnnotationPanel(AnnotatorWorkbench wb, SemGenSettings sets,
			GlobalActions gacts) {
		super(wb, wb.openCodewordDrawer(), sets, gacts);
		drawUI();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void refreshUI() {

	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1==modeledit.compositechanged) {
			//compositepanel.refreshUI();
		}
	}

	@Override
	protected void constructMainPanel(JPanel main) {
		
	}

	@Override
	protected void formatHeader() {
		
	}

	@Override
	protected void constructHumanDefinitionPanel() {
		
	}

}
