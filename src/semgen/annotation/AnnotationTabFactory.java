package semgen.annotation;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semgen.resource.uicomponent.TabFactory;

public class AnnotationTabFactory extends TabFactory<AnnotatorWorkbench> {

	public AnnotationTabFactory(SemGenSettings sets, GlobalActions actions) {
		super(sets, actions);
	}
	
	public AnnotatorTab makeTab(AnnotatorWorkbench workbench) {
		AnnotatorTab tab = new AnnotatorTab(settings, globalactions, workbench);
		return tab;
	}
}
