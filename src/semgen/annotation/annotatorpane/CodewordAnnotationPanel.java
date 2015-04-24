package semgen.annotation.annotatorpane;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.annotation.workbench.CodewordToolDrawer;

public class CodewordAnnotationPanel extends AnnotationPanel<CodewordToolDrawer> {

	private static final long serialVersionUID = 1L;

	public CodewordAnnotationPanel(AnnotatorWorkbench wb, SemGenSettings sets,
			GlobalActions gacts) {
		super(wb, sets, gacts);
	}

}
