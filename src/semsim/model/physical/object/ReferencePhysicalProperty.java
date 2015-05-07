package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimConstants;
import semsim.model.physical.PhysicalModelComponent;

public class ReferencePhysicalProperty extends PhysicalModelComponent{
	private URI ppuri;
	
	public ReferencePhysicalProperty() {}
	
	public ReferencePhysicalProperty(String name, URI uri) {
		setName(name);
		ppuri = uri;
	}

	public URI getURI() {
		return ppuri;
	}
	
	@Override
	public String getComponentTypeasString() {
		return "property";
	}
	
	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.PHYSICAL_PROPERTY_CLASS_URI;
	}
}
