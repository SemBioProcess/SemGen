package semsim.model.physical.object;

import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalModelComponent;

public class PhysicalProperty extends PhysicalModelComponent{
	
	private DataStructure associatedDataStructure;
	private PhysicalModelComponent physicalPropertyOf;
	
	public DataStructure getAssociatedDataStructure(){
		return associatedDataStructure;
	}
	
	public void setAssociatedDataStructure(DataStructure ds){
		associatedDataStructure = ds;
		setName(ds.getName() + "_property");
	}

	public void setPhysicalPropertyOf(PhysicalModelComponent physicalPropertyOf) {
		this.physicalPropertyOf = physicalPropertyOf;
	}

	public PhysicalModelComponent getPhysicalPropertyOf() {
		return physicalPropertyOf;
	}

	@Override
	public String getComponentTypeasString() {
		return "property";
	}
}
