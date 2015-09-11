package semsim.model.physical.object;

import java.net.URI;

import semgen.SemGen;
import semsim.SemSimConstants;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;

public class ReferencePhysicalDependency extends PhysicalDependency implements ReferenceTerm {

	public ReferencePhysicalDependency(URI uri, String description){
		addReferenceOntologyAnnotation(SemSimConstants.HAS_PHYSICAL_DEFINITION_RELATION, uri, description);
	}

	public ReferenceOntologyAnnotation getPhysicalDefinitionReferenceOntologyAnnotation(){
		if(hasPhysicalDefinitionAnnotation()){
			return new ReferenceOntologyAnnotation(SemSimConstants.HAS_PHYSICAL_DEFINITION_RELATION, referenceuri, getDescription());
		}
		return null;
	}
	
	/**
	 * @return The name of the knowledge base that contains the URI used as the annotation value
	 */
	public String getNamewithOntologyAbreviation() {
		return getName() + " (" + SemGen.semsimlib.getReferenceOntologyAbbreviation(referenceuri) + ")";
	}
	
	public URI getPhysicalDefinitionURI() {
		return URI.create(referenceuri.toString());
	}
	
	@Override
	public String getOntologyName() {
		return SemGen.semsimlib.getReferenceOntologyName(referenceuri);
	}

	@Override
	public String getTermID() {
		return referenceuri.getFragment();
	}
	
	@Override
	protected boolean isEquivalent(Object obj) {
		return ((ReferencePhysicalDependency)obj).getPhysicalDefinitionURI().compareTo(referenceuri)==0;
	}
}
