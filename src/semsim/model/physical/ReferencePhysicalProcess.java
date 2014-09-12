package semsim.model.physical;

import java.net.URI;

import semsim.SemSimConstants;

public class ReferencePhysicalProcess extends PhysicalProcess{
	
	public ReferencePhysicalProcess(URI uri, String description){
		addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, uri, description);
		setName(description);
	}
}
