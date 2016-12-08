package semgen.stage.stagetasks.extractor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import semgen.utilities.Workbench;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.datastructures.DataStructure;
import semsim.reading.ModelAccessor;


public class ExtractorWorkbench extends Workbench {
	private ModelAccessor modelaccessor;
	private SemSimModel sourcemodel;

	public ExtractorWorkbench(ModelAccessor accessor, SemSimModel model) {
		modelaccessor = accessor;
		sourcemodel = model;
	}
	
	@Override
	public void initialize() {}

	@Override
	public void setModelSaved(boolean val) {}

	@Override
	public String getCurrentModelName() {
		return sourcemodel.getName();
	}

	@Override
	public ModelAccessor getModelSourceLocation() {
		return sourcemodel.getLegacyCodeLocation();
	}
	
	@Override
	public ModelAccessor saveModel() {
		return null;
	}

	@Override
	public ModelAccessor saveModelAs() {
		return null;
	}

	public ModelAccessor getModelAccessor() {
		return modelaccessor;
	}
	
	public SemSimModel getSourceModel() {
		return sourcemodel;
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

	@Override
	public void update(Observable arg0, Object arg1) {
		
	}
}
