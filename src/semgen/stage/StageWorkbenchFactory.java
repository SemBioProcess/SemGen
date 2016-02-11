package semgen.stage;

import semgen.utilities.WorkbenchFactory;
import semsim.reading.ModelAccessor;

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
	protected void makeWorkbench(ModelAccessor modelaccessor) {
		workbenches.add(new StageWorkbench());
	}
}
