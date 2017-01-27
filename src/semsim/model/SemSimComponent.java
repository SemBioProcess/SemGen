package semsim.model;

import java.net.URI;

import semsim.SemSimObject;
import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimCollection;
import semsim.model.collection.SemSimModel;

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
	
	public Boolean isPhysicalComponent() {
		return false;
	}
	
	public abstract void addToModel(SemSimModel model);
}
