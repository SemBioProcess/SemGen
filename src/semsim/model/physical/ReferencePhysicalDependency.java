package semsim.model.physical;

import java.net.URI;

import semsim.SemSimConstants;

public class ReferencePhysicalDependency extends PhysicalDependency{

	public ReferencePhysicalDependency(URI uri, String description){
		addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, uri, description);
	}
}
