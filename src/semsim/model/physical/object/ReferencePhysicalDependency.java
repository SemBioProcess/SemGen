package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimLibrary;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.definitions.SemSimRelations.SemSimRelation;

public class ReferencePhysicalDependency extends PhysicalDependency implements ReferenceTerm {

	public ReferencePhysicalDependency(URI uri, String description, SemSimLibrary lib){
		addReferenceOntologyAnnotation(SemSimRelation.HAS_PHYSICAL_DEFINITION, uri, description, lib);
	}

	public ReferenceOntologyAnnotation getPhysicalDefinitionReferenceOntologyAnnotation(SemSimLibrary lib){
		if(hasPhysicalDefinitionAnnotation()){
			return new ReferenceOntologyAnnotation(SemSimRelation.HAS_PHYSICAL_DEFINITION, referenceuri, getDescription(), lib);
		}
		return null;
	}
	
	/**
	 * @return The name of the knowledge base that contains the URI used as the annotation value
	 */
	public String getNamewithOntologyAbreviation(SemSimLibrary semsimlib) {
		return getName() + " (" + semsimlib.getReferenceOntologyAbbreviation(referenceuri) + ")";
	}
	
	public URI getPhysicalDefinitionURI() {
		return URI.create(referenceuri.toString());
	}
	
	@Override
	public String getOntologyName(SemSimLibrary semsimlib) {
		return semsimlib.getReferenceOntologyName(referenceuri);
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
