package semgen.merging.workbench;

import java.io.File;

import semgen.utilities.WorkbenchFactory;

public class MergerWorkbenchFactory extends WorkbenchFactory<MergerWorkbench>{
	
	@Override
	protected boolean makeWorkbenches() {
		makeWorkbench(null);
		return true;
	}
	
	@Override
	protected void makeWorkbench(File file) {
		workbenches.add(new MergerWorkbench());
	}
}
