package semsim.model.physical;

import semsim.definitions.SemSimTypes;

public abstract class PhysicalEntity extends PhysicalModelComponent{

public PhysicalEntity(SemSimTypes type) {
	super(type);
}

	public PhysicalEntity(PhysicalEntity petocopy) {
		super(petocopy);
	}
	
	@Override
	public String getComponentTypeasString() {
		return "entity";
	}

}
