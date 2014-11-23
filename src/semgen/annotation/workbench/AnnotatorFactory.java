package semgen.annotation.workbench;

import java.io.File;

import semgen.resource.WorkbenchFactory;
import semgen.resource.file.LoadSemSimModel;
import semgen.resource.file.SemGenOpenFileChooser;
import semsim.model.SemSimModel;

public class AnnotatorFactory extends WorkbenchFactory<AnnotatorWorkbench>{
	File sourcefile;
	boolean autoannotate = false;
	
	public AnnotatorFactory(boolean aannotate) {
		super("Loading File");
		autoannotate = aannotate;
		SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select legacy code or SemSim model to annotate");
		sourcefile = sgc.getSelectedFile();
		if (sourcefile == null) 
			abort();
	}
	
	public AnnotatorFactory(boolean autoannotate, File existing) {
		super("Loading File");
		sourcefile = existing;
	}
	
	protected boolean makeWorkbench() {	
    	System.out.println("Loading " + sourcefile.getName());
    	setStatus("Creating SemSimModel");
		SemSimModel semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(sourcefile, autoannotate);
		
		if(!semsimmodel.getErrors().isEmpty()){
			return false;
		}
		
		workbench = new AnnotatorWorkbench(sourcefile, semsimmodel);
		workbench.initialize();
		
		return true;
	}	
}
