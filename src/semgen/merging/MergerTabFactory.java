package semgen.merging;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.merging.workbench.MergerWorkbench;
import semgen.utilities.uicomponent.TabFactory;

public class MergerTabFactory extends TabFactory<MergerWorkbench> {

	public MergerTabFactory(SemGenSettings sets, GlobalActions actions) {
		super(sets, actions);
	}
	
	public MergerTab makeTab(MergerWorkbench workbench) {
		MergerTab tab = new MergerTab(settings, globalactions, workbench);
		return tab;
	}
}
