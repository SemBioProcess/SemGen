package semsim.model.physical;

public abstract class PhysicalEntity extends PhysicalModelComponent{

public PhysicalEntity() {}

	public PhysicalEntity(PhysicalEntity petocopy) {
		super(petocopy);
	}
	
	@Override
	public String getComponentTypeasString() {
		return "entity";
	}

}
