package semgen.stage;

import java.io.File;

import semgen.utilities.WorkbenchFactory;

public class StageWorkbenchFactory extends WorkbenchFactory<StageWorkbench> {

	
	public StageWorkbenchFactory() {
		super("Loading Stage");
	}
	
	@Override
	protected boolean makeWorkbenches() {
		setStatus("Loading Stage");
		makeWorkbench(null);
		return true;
	}
	
	@Override
	protected void makeWorkbench(File file) {
		workbenches.add(new StageWorkbench());
	}
}
