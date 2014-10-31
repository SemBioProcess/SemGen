package semsim.model.physical;

import semsim.model.computational.datastructures.DataStructure;

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
}
