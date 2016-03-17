package semgen.extraction.workbench;

import semgen.utilities.WorkbenchFactory;
import semgen.utilities.file.LoadSemSimModel;
import semgen.utilities.file.SemGenOpenFileChooser;
import semsim.model.collection.SemSimModel;
import semsim.reading.ModelAccessor;

public class ExtractorFactory extends WorkbenchFactory<ExtractorWorkbench> {
	
	public ExtractorFactory() {
		super("Loading File");
		final SemGenOpenFileChooser sgc =  new SemGenOpenFileChooser("Extractor - Select source SemSim model",
				new String[]{"owl"},true);
		
		modelaccessors.addAll(sgc.getSelectedFilesAsModelAccessors());

		if (modelaccessors.size()==0) 
			abort();
	}
	
//	public ExtractorFactory(File file) {
//		super("Loading File");
//	}
	
	public ExtractorFactory(ModelAccessor accessor) {
		modelaccessors.add(accessor);
	}
	
	protected void makeWorkbench(ModelAccessor modelaccessor) {	
		System.out.println("Loading " + modelaccessor.getModelName());
		
		LoadSemSimModel	loader = new LoadSemSimModel(modelaccessor, false, this);
		loader.run();
		SemSimModel semsimmodel = loader.getLoadedModel();
		
		if(!semsimmodel.getErrors().isEmpty()){
			return;
		}

		workbenches.add(new ExtractorWorkbench(modelaccessor, semsimmodel));
	}	
}
