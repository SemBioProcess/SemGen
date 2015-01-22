package semgen.merging.workbench;

import java.util.HashMap;
import java.util.Set;

import semsim.model.computational.datastructures.DataStructure;

public class DataStructureDescriptor {
	public enum Descriptor {
		name, description, units, computationalcode, inputs, inputfor;
	}
	
	private HashMap<Descriptor, String> descriptormap = new HashMap<Descriptor, String>();
	
	public DataStructureDescriptor(DataStructure ds) {
		descriptormap.put(Descriptor.name, ds.getName());
		descriptormap.put(Descriptor.description, ds.getDescription());
		descriptormap.put(Descriptor.units, ds.getUnit().getComputationalCode());
		descriptormap.put(Descriptor.computationalcode, ds.getComputation().getComputationalCode());
		
		makeStringListFromSet(Descriptor.inputs, ds.getComputationInputs(), true);
		makeStringListFromSet(Descriptor.inputfor, ds.getUsedToCompute(), false);
	}
	
	private void makeStringListFromSet(Descriptor desc, Set<DataStructure> dsset, Boolean forInput) {
		String stringlist = "  ";
		int n = 0;
		for (DataStructure ds : dsset) {
			if (n == 0) {
				stringlist = ds.getDescription();
			} else {
				stringlist = stringlist + "\n" + "  " + ds.getDescription();
			}
			n++;
		}
		if (dsset.isEmpty()) {
			if (forInput) {
				stringlist = "  user-defined (external) input";
			} else {
				stringlist = "  nothing";
			}
		}
		descriptormap.put(desc, stringlist);
	}
	
	public String getDescriptorValue(Descriptor desc) {
		return descriptormap.get(desc);
	}
}
	
