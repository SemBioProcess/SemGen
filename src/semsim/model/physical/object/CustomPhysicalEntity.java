package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimConstants;
import semsim.model.SemSimTypes;
import semsim.model.physical.PhysicalEntity;


public class CustomPhysicalEntity extends PhysicalEntity{
	
	public CustomPhysicalEntity(String name, String description){
		setName(name);
		setDescription(description);
	}
	
	/** 
	 * Copy constructur
	 * @param cupe
	 */
	public CustomPhysicalEntity(CustomPhysicalEntity cupe) {
		setName(new String(cupe.getName()));
		setDescription(new String(cupe.getDescription()));
	}
	
	@Override
	protected boolean isEquivalent(Object obj) {
		return ((CustomPhysicalEntity)obj).getName().compareTo(getName())==0;
	}
	
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.CUSTOM_PHYSICAL_ENTITY_CLASS_URI;
	}
	@Override
	protected SemSimTypes getSemSimType() {
		return SemSimTypes.CUSTOM_PHYSICAL_ENTITY;
	}
}
