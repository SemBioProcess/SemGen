package semsim.model.physical.object;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;
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

	@Override
	public CustomPhysicalEntity addToModel(SemSimModel model) {
		return model.addCustomPhysicalEntity(this);
		
	}
	
	@Override
	public void removeFromModel(SemSimModel model) {
		model.removePhysicalEntityFromCache(this);
	}
}
