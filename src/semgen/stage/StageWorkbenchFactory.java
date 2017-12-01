package semgen.stage;

import semgen.stage.stagetasks.ProjectTask.ProjectCommandReceiver;
import semgen.utilities.WorkbenchFactory;
import semsim.fileaccessors.ModelAccessor;

public class StageWorkbenchFactory extends WorkbenchFactory<StageWorkbench> {

	private static String LOAD_MSG = "Loading interface...";
	
	public StageWorkbenchFactory() {
		super(LOAD_MSG);
	}
	
	public StageWorkbenchFactory(ModelAccessor accessor) {
		super("Loading " + accessor.getShortLocation());
		this.modelaccessors.add(accessor);
	}
	
	@Override
	protected boolean makeWorkbenches() {
		setStatus(LOAD_MSG);
		
		if(modelaccessors.isEmpty())
			makeWorkbench(null);
		else{
			for(ModelAccessor modelaccessor : modelaccessors){
				makeWorkbench(modelaccessor);
			}
		}
		return true;
	}
	
	@Override
	protected void makeWorkbench(ModelAccessor modelaccessor) {
		StageWorkbench sw = new StageWorkbench();
		workbenches.add(sw);
		
		if(modelaccessor != null){
			sw.initialize();
			ProjectCommandReceiver pcr = (ProjectCommandReceiver)sw.getProjectTask().getCommandReceiver();
			pcr.onAddModelFromAnnotator(modelaccessor);
		}
	}
}
