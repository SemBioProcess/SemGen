package semgen.extraction;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.extraction.workbench.ExtractorWorkbench;
import semgen.utilities.uicomponent.TabFactory;

public class ExtractorTabFactory extends TabFactory<ExtractorWorkbench> {

	public ExtractorTabFactory(SemGenSettings sets, GlobalActions actions) {
		super(sets, actions);
	}
	
	public ExtractorTab makeTab(ExtractorWorkbench workbench) {
		ExtractorTab tab = new ExtractorTab(settings, globalactions, workbench);
		return tab;
	}
}
