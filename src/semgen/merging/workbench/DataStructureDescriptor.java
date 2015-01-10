package semgen.merging.workbench;

import java.util.Set;

import semsim.model.computational.datastructures.DataStructure;

public class DataStructureDescriptor {
	public enum Descriptor {
		name, description, computationalcode, inputs, inputfor;
		private String attributevalue;	
		
		Descriptor() {} 
			
		protected String getDescriptor() {
			return attributevalue;
		}
		
		protected void setDescriptor(String value) {
			attributevalue = value;
		}
	}
	
	public DataStructureDescriptor(DataStructure ds) {
		Descriptor.name.setDescriptor(ds.getName());
		Descriptor.description.setDescriptor(ds.getDescription());
		Descriptor.computationalcode.setDescriptor(ds.getComputation().getComputationalCode());
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
		desc.setDescriptor(stringlist);
	}
	
	public String getDescriptorValue(Descriptor desc) {
		return desc.getDescriptor();
	}
}
	
