package semsim.model.physical;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;

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

	public abstract PhysicalEntity addToModel(SemSimModel model);
}
