package semgen.merging.workbench;

import semgen.SemGen;
import semsim.model.computational.datastructures.DataStructure;

import java.util.HashMap;
import java.util.Set;

public class DataStructureDescriptor {
	public enum Descriptor {
		name, description, units, computationalcode, inputs, inputfor, type, annotation;
	}
	
	private HashMap<Descriptor, String> descriptormap = new HashMap<Descriptor, String>();
	
	public DataStructureDescriptor(DataStructure ds) {
		descriptormap.put(Descriptor.name, ds.getName());
		descriptormap.put(Descriptor.description, ds.getDescription());
		descriptormap.put(Descriptor.type, ds.getPropertyType(SemGen.semsimlib).toString());
		if(ds.hasUnits())
			descriptormap.put(Descriptor.units, ds.getUnit().getComputationalCode());
		else descriptormap.put(Descriptor.units, "");
		
		if(ds.getComputation().getComputationalCode() != null)
			descriptormap.put(Descriptor.computationalcode, ds.getComputation().getComputationalCode());
		else if(ds.getStartValue() != null)
			descriptormap.put(Descriptor.computationalcode, ds.getName().substring(ds.getName().lastIndexOf(".") + 1)+"="+ds.getStartValue());
		else descriptormap.put(Descriptor.computationalcode, "");

		if(ds.getCompositeAnnotationAsString(false) != "[unspecified]")
			descriptormap.put(Descriptor.annotation, ds.getCompositeAnnotationAsString(false));
		else descriptormap.put(Descriptor.annotation, ds.getSingularTerm().getName());

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
	
