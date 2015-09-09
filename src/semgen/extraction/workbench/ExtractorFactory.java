package semgen.extraction.workbench;

import java.io.File;

import semgen.utilities.WorkbenchFactory;
import semgen.utilities.file.LoadSemSimModel;
import semgen.utilities.file.SemGenOpenFileChooser;
import semsim.model.collection.SemSimModel;

public class ExtractorFactory extends WorkbenchFactory<ExtractorWorkbench> {
	public ExtractorFactory() {
		super("Loading File");
		final SemGenOpenFileChooser sgc =  new SemGenOpenFileChooser("Extractor - Select source SemSim model",
				new String[]{"owl"},true);
		for (File file : sgc.getSelectedFiles()) {
			sourcefile.add(file);
		}
		if (sourcefile.size()==0) 
			abort();
	}
	
	public ExtractorFactory(File file) {
		super("Loading File");
		sourcefile.add(file);
	}
	
	protected void makeWorkbench(File file) {	
		System.out.println("Loading " + file.getName());
		
		setStatus("Creating SemSimModel");
		LoadSemSimModel	loader = new LoadSemSimModel(file, false, this);
		loader.run();
		SemSimModel semsimmodel = loader.getLoadedModel();
		
		if(!semsimmodel.getErrors().isEmpty()){
			return;
		}

		workbenches.add(new ExtractorWorkbench(file, semsimmodel));
	}	
}
