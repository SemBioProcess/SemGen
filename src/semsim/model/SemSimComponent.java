package semsim.model;

import java.net.URI;

import semsim.SemSimObject;
import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;

/**
 * A SemSimComponent is a {@link SemSimObject} that represents a computational 
 * or physical model element
 */
public abstract class SemSimComponent extends SemSimObject {
	
	protected URI referenceuri = URI.create(new String(""));
	
	public SemSimComponent(SemSimTypes type) {
		super(type);	
	}
	
	/**
	 * Copy constructor
	 * @param ssctocopy The SemSimComponent to copy
	 */
	public SemSimComponent(SemSimComponent ssctocopy) {
		super (ssctocopy);
		referenceuri = ssctocopy.referenceuri;
	}
	
	/** @return Whether the component's physical meaning is defined with an annotation */
	public Boolean hasPhysicalDefinitionAnnotation() {
		return !referenceuri.toString().isEmpty();
	}
	
	/** @return Whether the component is a physical component. Method 
	 * is overriden by subclasses. */
	public Boolean isPhysicalComponent() {
		return false;
	}
	
	/**
	 * Add the component to a specified {@link SemSimModel}
	 * @param model The specified {@link SemSimModel}
	 * @return The SemSimComponent added to the model
	 */
	public abstract SemSimComponent addToModel(SemSimModel model);
}
