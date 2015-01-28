/**
 * This factory produces annotator workbenches.
 */
package semgen.annotation.workbench;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import semgen.utilities.WorkbenchFactory;
import semgen.utilities.file.LoadSemSimModel;
import semgen.utilities.file.SemGenOpenFileChooser;
import semsim.model.SemSimModel;

public class AnnotatorFactory extends WorkbenchFactory<AnnotatorWorkbench>{
	boolean autoannotate = false;
	
	public AnnotatorFactory(boolean aannotate) {
		super("Loading File");
		autoannotate = aannotate;
		SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select legacy code or SemSim model to annotate", true);
		for (File file : sgc.getSelectedFiles()) {
			sourcefile.add(file);
		}
		if (sourcefile.size()==0) 
			abort();
	}
	
	public AnnotatorFactory(boolean autoannotate, File existing) {
		super("Loading File");
		sourcefile.add(existing);
	}
	
	protected void makeWorkbench(File file) {	
    	System.out.println("Loading " + file.getName());
    	setStatus("Creating SemSimModel");
		SemSimModel semsimmodel = LoadSemSimModel.loadSemSimModelFromFile(file, autoannotate);
		
		if(!semsimmodel.getErrors().isEmpty()){
			return;
		}
		
		AnnotatorWorkbench wb = new AnnotatorWorkbench(file, semsimmodel);
		wb.initialize();
		workbenches.add(wb);
	}	
	
	public ArrayList<URI> getFileURIs() {
		ArrayList<URI> uris = new ArrayList<URI>();
		for (File file : sourcefile) {
			uris.add(file.toURI());
		}
		return uris;
	}
	
	public boolean removeFilebyIndex(int index) {
		sourcefile.remove(index);
		return (sourcefile.size()==0);
	}
}
