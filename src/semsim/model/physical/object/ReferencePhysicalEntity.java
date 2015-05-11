package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimConstants;
import semsim.model.physical.PhysicalEntity;

public class ReferencePhysicalEntity extends PhysicalEntity{
	
	public ReferencePhysicalEntity(URI uri, String description){
		referenceuri = uri;
		setName(description);
	}
	
	@Override
	protected boolean isEquivalent(Object obj) {
		return ((ReferencePhysicalEntity)obj).getReferstoURI().compareTo(referenceuri)==0;
	}
	
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.REFERENCE_PHYSICAL_ENTITY_CLASS_URI;
	}
}
