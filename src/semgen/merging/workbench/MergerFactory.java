package semgen.merging.workbench;

import semgen.utilities.WorkbenchFactory;

public class MergerFactory extends WorkbenchFactory<MergerWorkbench>{

	@Override
	protected boolean makeWorkbench() {
		workbench = new MergerWorkbench();
		return true;
	}

}
