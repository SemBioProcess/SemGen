package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimConstants;
import semsim.annotation.ReferenceOntologyAnnotation;
import semsim.annotation.ReferenceTerm;
import semsim.model.physical.PhysicalModelComponent;

public class PhysicalProperty extends PhysicalModelComponent implements ReferenceTerm{
		
	public PhysicalProperty(String label, URI uri) {
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
		return ((PhysicalProperty)obj).getReferstoURI().compareTo(referenceuri)==0;
	}
}
