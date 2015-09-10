package semsim.model.physical.object;

import java.net.URI;

import semgen.SemGen;
import semsim.SemSimConstants;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.model.SemSimTypes;
import semsim.model.physical.PhysicalModelComponent;

public class PhysicalPropertyinComposite extends PhysicalModelComponent implements ReferenceTerm{
		
	public PhysicalPropertyinComposite(String label, URI uri) {
		referenceuri = uri;
		setName(label);
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
	
	/**
	 * @return The name of the knowledge base that contains the URI used as the annotation value
	 */
	public String getNamewithOntologyAbreviation() {
		return getName() + " (" + SemGen.semsimlib.getReferenceOntologyAbbreviation(referenceuri) + ")";
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
		return ((PhysicalPropertyinComposite)obj).getReferstoURI().compareTo(referenceuri)==0;
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
	public SemSimTypes getSemSimType() {
		return SemSimTypes.PHYSICAL_PROPERTY_IN_COMPOSITE;
	}
}
