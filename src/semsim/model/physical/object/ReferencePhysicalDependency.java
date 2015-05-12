package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimConstants;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;

public class ReferencePhysicalDependency extends PhysicalDependency implements ReferenceTerm {

	public ReferencePhysicalDependency(URI uri, String description){
		addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, uri, description);
	}

	public ReferenceOntologyAnnotation getRefersToReferenceOntologyAnnotation(){
		if(hasRefersToAnnotation()){
			return new ReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, referenceuri, getDescription());
		}
		return null;
	}
	
	public URI getReferstoURI() {
		return URI.create(referenceuri.toString());
	}
	
	@Override
	protected boolean isEquivalent(Object obj) {
		return ((ReferencePhysicalDependency)obj).getReferstoURI().compareTo(referenceuri)==0;
	}
}
