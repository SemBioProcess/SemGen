package semgen.merging;

import java.io.File;
import java.util.Set;

import semgen.GlobalActions;
import semgen.SemGenSettings;
import semgen.merging.workbench.MergerWorkbench;
import semgen.utilities.uicomponent.TabFactory;

public class MergerTabFactory extends TabFactory<MergerWorkbench> {

	private Set<File> _existingModels;
	
	public MergerTabFactory(SemGenSettings sets, GlobalActions actions) {
		super(sets, actions);
	}
	
	public MergerTabFactory(SemGenSettings sets, GlobalActions actions, Set<File> existingModels) {
		super(sets, actions);
		_existingModels = existingModels;
	}
	
	public MergerTab makeTab(MergerWorkbench workbench) {
		MergerTab tab = new MergerTab(settings, globalactions, workbench, _existingModels);
		return tab;
	}
}
