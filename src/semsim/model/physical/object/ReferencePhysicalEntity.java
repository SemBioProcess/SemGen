package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimLibrary;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.definitions.SemSimConstants;
import semsim.definitions.SemSimTypes;
import semsim.model.physical.PhysicalEntity;

public class ReferencePhysicalEntity extends PhysicalEntity implements ReferenceTerm{
	
	public ReferencePhysicalEntity(URI uri, String description){
		super(SemSimTypes.REFERENCE_PHYSICAL_ENTITY);
		referenceuri = uri;
		setName(description);
	}
	
	public ReferenceOntologyAnnotation getPhysicalDefinitionReferenceOntologyAnnotation(){
		if(hasPhysicalDefinitionAnnotation()){
			return new ReferenceOntologyAnnotation(SemSimConstants.HAS_PHYSICAL_DEFINITION_RELATION, referenceuri, getDescription());
		}
		return null;
	}
	
	public URI getPhysicalDefinitionURI() {
		return URI.create(referenceuri.toString());
	}
	
	/**
	 * @return The name of the knowledge base that contains the URI used as the annotation value
	 */
	public String getNamewithOntologyAbreviation(SemSimLibrary semsimlib) {
		return getName() + " (" + semsimlib.getReferenceOntologyAbbreviation(referenceuri) + ")";
	}
	
	@Override
	protected boolean isEquivalent(Object obj) {
		return ((ReferencePhysicalEntity)obj).getPhysicalDefinitionURI().toString().equals(referenceuri.toString());
	}
	
	@Override
	public String getOntologyName(SemSimLibrary semsimlib) {
		return semsimlib.getReferenceOntologyName(referenceuri);
	}

	@Override
	public String getTermID() {
		return referenceuri.getFragment();
	}
}
