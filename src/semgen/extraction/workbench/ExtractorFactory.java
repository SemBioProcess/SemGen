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
//			if(ModelClassifier.classify(file)==ModelClassifier.CELLML_MODEL) {
//				isCellMLError();
//				continue;
//			}
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
		SemSimModel semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(file, false);
		
		if(!semsimmodel.getErrors().isEmpty()){
			return;
		}
		
//		if(ModelClassifier.classify(file)==ModelClassifier.CELLML_MODEL || semsimmodel.getFunctionalSubmodels().size()>0){
//			isCellMLError();
//			return;
//		}
		
		workbenches.add(new ExtractorWorkbench(file, semsimmodel));
	}	
	
//	private void isCellMLError() {
//		SemGenError.showError("Sorry. Extraction of models with CellML-type components not yet supported.","");
//	}
}
