package semgen.stage;

import java.io.File;

import semgen.utilities.WorkbenchFactory;

public class StageWorkbenchFactory extends WorkbenchFactory<StageWorkbench> {

	@Override
	protected boolean makeWorkbenches() {
		makeWorkbench(null);
		return true;
	}
	
	@Override
	protected void makeWorkbench(File file) {
		workbenches.add(new StageWorkbench());
	}
}
