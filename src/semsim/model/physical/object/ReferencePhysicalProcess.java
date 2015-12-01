package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimLibrary;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.definitions.SemSimConstants;
import semsim.definitions.SemSimTypes;
import semsim.model.physical.PhysicalProcess;

public class ReferencePhysicalProcess extends PhysicalProcess implements ReferenceTerm{
	
	public ReferencePhysicalProcess(URI uri, String description){
		super(SemSimTypes.REFERENCE_PHYSICAL_PROCESS);
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
		return referenceuri;
	}
	
	/**
	 * @return The name of the knowledge base that contains the URI used as the annotation value
	 */
	public String getNamewithOntologyAbreviation(SemSimLibrary semsimlib) {
		return getName() + " (" + semsimlib.getReferenceOntologyAbbreviation(referenceuri) + ")";
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
		return ((ReferencePhysicalProcess)obj).getPhysicalDefinitionURI().compareTo(referenceuri)==0;
	}
}
