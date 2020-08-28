/**
 * This factory produces annotator workbenches.
 */
package semgen.annotation.workbench;

import semgen.utilities.WorkbenchFactory;
import semgen.utilities.file.LoadModelJob;
import semgen.utilities.file.SemGenOpenFileChooser;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;

public class AnnotatorFactory extends WorkbenchFactory<AnnotatorWorkbench>{
	boolean autoannotate = false;
	
	public AnnotatorFactory(boolean aannotate) {
		super("Loading File");
		autoannotate = aannotate;
		SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select legacy code or SemSim model to annotate", true);
		modelaccessors.addAll(sgc.getSelectedFilesAsModelAccessors());
		
		if (modelaccessors.size()==0) 
			abort();
	}
	
	public AnnotatorFactory(boolean autoannotate, ModelAccessor existing) {
		super("Loading File");
		modelaccessors.add(existing);
	}
	
	protected void makeWorkbench(ModelAccessor modelaccessor) {	
    	
    	LoadModelJob loader = new LoadModelJob(modelaccessor, autoannotate, this);
    	loader.run();
    	SemSimModel semsimmodel = loader.getLoadedModel();

		AnnotatorWorkbench wb = new AnnotatorWorkbench(modelaccessor, semsimmodel);
		wb.initialize();
		workbenches.add(wb);
	}	
	
	public boolean removeFilebyIndex(int index) {
		modelaccessors.remove(index);
		return (modelaccessors.size()==0);
	}
}
