package semsim.model;

import java.net.URI;

import semsim.SemSimObject;
import semsim.definitions.SemSimTypes;

/**
 * A SemSimComponent is a representation of a mathematical or physical element
 */
public abstract class SemSimComponent extends SemSimObject {
	
	protected URI referenceuri = URI.create(new String(""));
	public SemSimComponent(SemSimTypes type) {
		super(type);
		
	}
	
	public SemSimComponent(SemSimComponent ssctocopy) {
		super (ssctocopy);
		referenceuri = ssctocopy.referenceuri;
	}
	
	public Boolean hasPhysicalDefinitionAnnotation() {
		return !referenceuri.toString().isEmpty();
	}
	
	
}
