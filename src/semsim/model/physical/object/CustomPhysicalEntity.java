package semsim.model.physical.object;

import semsim.definitions.SemSimTypes;
import semsim.model.physical.PhysicalEntity;


public class CustomPhysicalEntity extends PhysicalEntity{
	
	public CustomPhysicalEntity(String name, String description){
		super(SemSimTypes.CUSTOM_PHYSICAL_ENTITY);
		setName(name);
		setDescription(description);
	}
	
	/** 
	 * Copy constructur
	 * @param cupe
	 */
	public CustomPhysicalEntity(CustomPhysicalEntity cupe) {
		super(cupe);
	}
	
	@Override
	protected boolean isEquivalent(Object obj) {
		return ((CustomPhysicalEntity)obj).getName().equals(getName());
	}
}
