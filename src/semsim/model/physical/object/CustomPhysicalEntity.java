package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimConstants;
import semsim.model.physical.PhysicalEntity;


public class CustomPhysicalEntity extends PhysicalEntity{
	
	public CustomPhysicalEntity(String name, String description){
		setName(name);
		setDescription(description);
	}
	
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.CUSTOM_PHYSICAL_ENTITY_CLASS_URI;
	}
}
