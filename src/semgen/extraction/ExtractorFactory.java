package semgen.extraction;

import java.io.File;

import semgen.resource.SemGenError;
import semgen.resource.WorkbenchFactory;
import semgen.resource.file.LoadSemSimModel;
import semgen.resource.file.SemGenOpenFileChooser;
import semsim.model.SemSimModel;
import semsim.reading.ModelClassifier;

public class ExtractorFactory extends WorkbenchFactory<ExtractorWorkbench> {
	File sourcefile;
	public ExtractorFactory() {
		final SemGenOpenFileChooser sgc =  new SemGenOpenFileChooser("Extractor - Select source SemSim model",
				new String[]{"owl"} );
		sourcefile = sgc.getSelectedFile();
		if (sourcefile == null) {
			abort();
		}
		else if(ModelClassifier.classify(sourcefile)==ModelClassifier.CELLML_MODEL) {
			isCellMLError();
			abort();
		}
	}
	
	public ExtractorFactory(File file) {
		sourcefile = file;
	}
	
	protected boolean makeWorkbench() {	
		System.out.println("Loading " + sourcefile.getName());
				
		SemSimModel semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(sourcefile, false);
		
		if(!semsimmodel.getErrors().isEmpty()){
			return false;
		}
		
		if(ModelClassifier.classify(sourcefile)==ModelClassifier.CELLML_MODEL || semsimmodel.getFunctionalSubmodels().size()>0){
			isCellMLError();
			return false;
		}
		
		workbench = new ExtractorWorkbench(sourcefile, semsimmodel);
		return true;
	}	
	
	private void isCellMLError() {
		SemGenError.showError("Sorry. Extraction of models with CellML-type components not yet supported.","");
	}
}
