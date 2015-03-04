package semgen.stage;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.utilities.uicomponent.TabFactory;

public class StageTabFactory extends TabFactory<StageWorkbench> {

	public StageTabFactory(SemGenSettings sets, GlobalActions actions) {
		super(sets, actions);
	}
	
	public StageTab makeTab(StageWorkbench workbench) {
		StageTab tab = new StageTab(settings, globalactions, workbench);
		return tab;
	}
}
