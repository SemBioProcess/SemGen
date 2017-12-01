package semgen.merging;

import semgen.utilities.WorkbenchFactory;
import semsim.fileaccessors.ModelAccessor;

public class MergerWorkbenchFactory extends WorkbenchFactory<MergerWorkbench>{
	
	@Override
	protected boolean makeWorkbenches() {
		makeWorkbench(null);
		return true;
	}
	
	@Override
	protected void makeWorkbench(ModelAccessor modelaccessor) {
		workbenches.add(new MergerWorkbench());
	}
}
