package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimLibrary;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.definitions.SemSimTypes;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.model.physical.PhysicalModelComponent;

public class PhysicalPropertyinComposite extends PhysicalModelComponent implements ReferenceTerm{
		
	public PhysicalPropertyinComposite(String label, URI uri) {
		super(SemSimTypes.PHYSICAL_PROPERTY_IN_COMPOSITE);
		referenceuri = uri;
		setName(label);
	}

	
	public ReferenceOntologyAnnotation getPhysicalDefinitionReferenceOntologyAnnotation(SemSimLibrary lib){
		if(hasPhysicalDefinitionAnnotation()){
			return new ReferenceOntologyAnnotation(SemSimRelation.HAS_PHYSICAL_DEFINITION, referenceuri, getDescription(), lib);
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
}
