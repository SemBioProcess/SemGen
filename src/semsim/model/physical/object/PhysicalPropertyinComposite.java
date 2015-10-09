package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimConstants;
import semsim.SemSimLibrary;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.model.SemSimTypes;
import semsim.model.physical.PhysicalModelComponent;

public class PhysicalPropertyinComposite extends PhysicalModelComponent implements ReferenceTerm{
		
	public PhysicalPropertyinComposite(String label, URI uri) {
		referenceuri = uri;
		setName(label);
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
	public String getComponentTypeasString() {
		return "property";
	}
	
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.PHYSICAL_PROPERTY_CLASS_URI;
	}


	@Override
	protected boolean isEquivalent(Object obj) {
		return ((PhysicalPropertyinComposite)obj).getPhysicalDefinitionURI().compareTo(referenceuri)==0;
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
	public SemSimTypes getSemSimType() {
		return SemSimTypes.PHYSICAL_PROPERTY_IN_COMPOSITE;
	}
}
