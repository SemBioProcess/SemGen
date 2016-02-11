/**
 * This factory produces annotator workbenches.
 */
package semgen.annotation.workbench;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import semgen.utilities.WorkbenchFactory;
import semgen.utilities.file.LoadSemSimModel;
import semgen.utilities.file.ProjectFileModelSelectorDialog;
import semgen.utilities.file.SemGenOpenFileChooser;
import semsim.model.collection.SemSimModel;
import semsim.reading.JSimProjectFileReader;
import semsim.reading.ModelAccessor;
import semsim.reading.ModelingFileClassifier;

public class AnnotatorFactory extends WorkbenchFactory<AnnotatorWorkbench>{
	boolean autoannotate = false;
	
	public AnnotatorFactory(boolean aannotate) {
		super("Loading File");
		autoannotate = aannotate;
		SemGenOpenFileChooser sgc = new SemGenOpenFileChooser("Select legacy code or SemSim model to annotate", true);
		
		for (File file : sgc.getSelectedFiles()) {
			
			if(file.getName().toLowerCase().endsWith(".proj")){
				
				JSimProjectFileReader projreader = new JSimProjectFileReader(file);

				ArrayList<String> modelnames = projreader.getNamesOfModelsInProject();
				ProjectFileModelSelectorDialog pfmsd = 
						new ProjectFileModelSelectorDialog("Select model(s) to open", modelnames);

				for(String modelname : pfmsd.getSelectedModelNames()){
					modelaccessors.add(new ModelAccessor(file, modelname));
				}
			}
			else modelaccessors.add(new ModelAccessor(file));
		}
		if (modelaccessors.size()==0) 
			abort();
	}
	
	public AnnotatorFactory(boolean autoannotate, ModelAccessor existing) {
		super("Loading File");
		modelaccessors.add(existing);
	}
	
	protected void makeWorkbench(ModelAccessor modelaccessor) {	
    	System.out.println("Loading " + modelaccessor.toString());
    	
    	LoadSemSimModel loader = new LoadSemSimModel(modelaccessor, autoannotate, this);
    	loader.run();
    	SemSimModel semsimmodel = loader.getLoadedModel();

		AnnotatorWorkbench wb = new AnnotatorWorkbench(modelaccessor, semsimmodel);
		wb.initialize();
		workbenches.add(wb);
	}	
	
	public ArrayList<URI> getFileURIs() {
		ArrayList<URI> uris = new ArrayList<URI>();
		for (ModelAccessor modelaccessor : modelaccessors) {
			uris.add(modelaccessor.getFileThatContainsModel().toURI());
		}
		return uris;
	}
	
	public boolean removeFilebyIndex(int index) {
		modelaccessors.remove(index);
		return (modelaccessors.size()==0);
	}
}
