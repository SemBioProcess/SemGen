package semgen.stage.stagetasks.merge;

import java.util.HashMap;

import semsim.model.collection.Submodel;
import semsim.model.computational.Computation;
import semsim.model.computational.datastructures.DataStructure;

public class CenteredSubmodel extends Submodel {

	private DataStructure focusds;
	
	public CenteredSubmodel(DataStructure ds) {
		super(ds.getName());
		getConnections(ds);
	}
	
	public CenteredSubmodel(CenteredSubmodel submodel) {
		super(submodel);
		
		this.focusds = submodel.focusds;
	}
	
	private Submodel getConnections(DataStructure ds) {
		HashMap<DataStructure, DataStructure> dsmap = new HashMap<DataStructure, DataStructure>();
		focusds = copyDataStructure(ds);
		dsmap.put(ds, focusds);

		for (DataStructure compds : ds.getComputationInputs()) {
			dsmap.put(compds, copyDataStructure(compds));
		}
		
		for (DataStructure compds : ds.getUsedToCompute()) {
			dsmap.put(compds, copyDataStructure(compds));
		}
		
		
		Submodel dsconnections = new Submodel(ds.getName());
		
		for (DataStructure sourceds : dsmap.keySet()) {
			dsconnections.addDataStructure(dsmap.get(sourceds));
			replaceDataStructures(dsmap, sourceds, dsmap.get(sourceds));
		}

		return dsconnections;
	}
	
	private DataStructure copyDataStructure(DataStructure dstocopy) {
		DataStructure copy = dstocopy.copy();
		
		copy.setComputation(new Computation(copy));
		
		return copy;
	}
	
	//Replace inputs and outputs for a DataStructure with their copies
	private void replaceDataStructures(HashMap<DataStructure, DataStructure> dsmap, DataStructure original, DataStructure copy) {
		for (DataStructure input : original.getComputationInputs()) {
			DataStructure match = dsmap.get(input);
			if (match!=null) {
				copy.getComputation().addInput(match);
			}
		}
		for (DataStructure output : original.getUsedToCompute()) {
			DataStructure match = dsmap.get(output);
			if (match!=null) {
				copy.addUsedToCompute(match);
			}
		}
	}

	public DataStructure getFocusDataStructure() {
		return focusds;
	}

}
