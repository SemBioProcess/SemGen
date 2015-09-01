package semsim.model;

import semsim.SemSimObject;

/**
 * A SemSimComponent is a representation of a mathematical or physical element
 */
public abstract class SemSimComponent extends SemSimObject {
	public SemSimComponent() {}
	
	public SemSimComponent(SemSimComponent ssctocopy) {
		super (ssctocopy);
	}
}
