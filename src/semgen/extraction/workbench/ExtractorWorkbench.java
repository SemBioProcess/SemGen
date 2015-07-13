package semgen.extraction.workbench;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import semgen.utilities.Workbench;
import semsim.extraction.Extraction;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;


public class ExtractorWorkbench extends Workbench {
	File sourcefile;
	SemSimModel semsimmodel;
	Extraction extraction;

	public ExtractorWorkbench(File file, SemSimModel model) {
		sourcefile = file;
		semsimmodel = model;
		extraction = new Extraction(semsimmodel);
	}
	
	@Override
	public void initialize() {}

	@Override
	public void setModelSaved(boolean val) {}

	@Override
	public String getCurrentModelName() {
		return semsimmodel.getName();
	}

	@Override
	public String getModelSourceFile() {
		return semsimmodel.getLegacyCodeLocation();
	}
	
	public Extraction getExtraction(){
		return extraction;
	}

	@Override
	public File saveModel() {
		return null;
	}

	@Override
	public File saveModelAs() {
		return null;
	}

	public File getSourceFile() {
		return sourcefile;
	}
	
	public SemSimModel getSourceModel() {
		return semsimmodel;
	}
	
	
	// Retrieve the set of data structures are needed to compute a given data structure
	public Set<DataStructure> getDataStructureDependencyChain(DataStructure startds){
		
		// The hashmap contains the data structure and whether the looping alogrithm here should collect 
		// their inputs (true = collect)
		Map<DataStructure, Boolean> dsandcollectmap = new HashMap<DataStructure, Boolean>();
		dsandcollectmap.put(startds, true);
		DataStructure key = null;
		Boolean cont = true;
		
		while (cont) {
			cont = false; // We don't continue the loop unless we find a data structure with computational inputs
					  	  // that we need to collect (if the value for the DS in the map is 'true')
			for (DataStructure onekey : dsandcollectmap.keySet()) {
				key = onekey;
				if ((Boolean) dsandcollectmap.get(onekey) == true) {
					cont = true;
					for (DataStructure oneaddedinput : onekey.getComputationInputs()) {
						if (!dsandcollectmap.containsKey(oneaddedinput)) {
							dsandcollectmap.put(oneaddedinput, !oneaddedinput.getComputationInputs().isEmpty());
						}
					}
					break;
				}
			}
			dsandcollectmap.remove(key);
			dsandcollectmap.put(key, false);
		}
		
		Set<DataStructure> dsset = new HashSet<DataStructure>(dsandcollectmap.keySet());
		return dsset;
	}
}
