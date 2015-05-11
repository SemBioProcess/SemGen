package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimConstants;
import semsim.model.physical.PhysicalModelComponent;

public class PhysicalProperty extends PhysicalModelComponent{
		
	public PhysicalProperty(String label, URI uri) {
		referenceuri = uri;
		setName(label);
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
