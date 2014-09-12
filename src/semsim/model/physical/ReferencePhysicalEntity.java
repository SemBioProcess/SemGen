package semsim.model.physical;

import java.net.URI;

import semsim.SemSimConstants;

public class ReferencePhysicalEntity extends PhysicalEntity{
	
	public ReferencePhysicalEntity(URI uri, String description){
		addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, uri, description);
		setName(description);
	}
}
