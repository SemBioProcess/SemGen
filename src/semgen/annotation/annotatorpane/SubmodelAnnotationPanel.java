package semgen.annotation.annotatorpane;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.SubModelToolDrawer;

public class SubmodelAnnotationPanel extends AnnotationPanel<SubModelToolDrawer> {

	private static final long serialVersionUID = 1L;

	public SubmodelAnnotationPanel(AnnotatorWorkbench wb, SemGenSettings sets,
			GlobalActions gacts) {
		super(wb, sets, gacts);
	}

}
