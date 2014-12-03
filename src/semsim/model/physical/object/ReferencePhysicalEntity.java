package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimConstants;
import semsim.model.physical.PhysicalEntity;

public class ReferencePhysicalEntity extends PhysicalEntity{
	
	public ReferencePhysicalEntity(URI uri, String description){
		addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, uri, description);
		setName(description);
	}
	
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.REFERENCE_PHYSICAL_ENTITY_CLASS_URI;
	}
}
