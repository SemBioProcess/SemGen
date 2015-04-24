package semgen.annotation.annotatorpane;

import javax.swing.JPanel;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.drawers.AnnotatorDrawer;

public class AnnotationPanel<P extends AnnotatorDrawer> extends JPanel {
	private static final long serialVersionUID = 1L;

	protected AnnotatorWorkbench workbench;
	protected SemGenSettings settings;
	protected GlobalActions globalacts;
	
	public AnnotationPanel(AnnotatorWorkbench wb, SemGenSettings sets, GlobalActions gacts) {
		workbench = wb;
		settings = sets;
		globalacts = gacts;
		
		
	}
}
