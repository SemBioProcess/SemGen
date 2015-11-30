package semsim.model;

import java.net.URI;

import semsim.SemSimObject;

/**
 * A SemSimComponent is a representation of a mathematical or physical element
 */
public abstract class SemSimComponent extends SemSimObject {
	
	protected URI referenceuri = URI.create(new String(""));
	public SemSimComponent() {}
	
	public SemSimComponent(SemSimComponent ssctocopy) {
		super (ssctocopy);
		referenceuri = ssctocopy.referenceuri;
	}
	
	public Boolean hasPhysicalDefinitionAnnotation() {
		return !referenceuri.toString().isEmpty();
	}
	
	
}
