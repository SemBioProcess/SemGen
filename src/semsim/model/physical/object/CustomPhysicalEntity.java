package semsim.model.physical.object;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
import semsim.model.physical.PhysicalEntity;

/**
 * Class for representing physical entities that are not defined 
 * against a knowledge resource term.
 * @author mneal
 */
public class CustomPhysicalEntity extends PhysicalEntity{
	
	public CustomPhysicalEntity(String name, String description){
		super(SemSimTypes.CUSTOM_PHYSICAL_ENTITY);
		setName(name);
		setDescription(description);
	}
	
	/** 
	 * Copy constructor
	 * @param cupe The CustomPhysicalEntity to copy
	 */
	public CustomPhysicalEntity(CustomPhysicalEntity cupe) {
		super(cupe);
	}
	
	@Override
	protected boolean isEquivalent(Object obj) {
		return ((CustomPhysicalEntity)obj).getName().equals(getName());
	}

	@Override
	public CustomPhysicalEntity addToModel(SemSimModel model) {
		return model.addCustomPhysicalEntity(this);
		
	}
	
	@Override
	public void removeFromModel(SemSimModel model) {
		model.removePhysicalEntityFromCache(this);
	}
}
