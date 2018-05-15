package semsim.model.physical;

import semsim.definitions.SemSimTypes;
import semsim.model.collection.SemSimModel;

/**
 * Class for working with the physical entities represented in a model.
 * 
 * Analogous to 'OPB:Physics continuant':
 * Physics continuants are portions of primitive physics entites that 
 * may be spatially-bounded (e.g, a heart, a portion of blood, a cell's 
 * cytoplasmic  sodium ions) or may be spatially-unbounded (e.g., an 
 * electrical field or gravitational potential field).
 * @author mneal
 *
 */

public abstract class PhysicalEntity extends PhysicalModelComponent{

public PhysicalEntity(SemSimTypes type) {
	super(type);
}

	/**
	 * Copy constructor
	 * @param petocopy The physical entity to copy
	 */
	public PhysicalEntity(PhysicalEntity petocopy) {
		super(petocopy);
	}
	
	@Override
	public String getComponentTypeAsString() {
		return "entity";
	}

	@Override
	public abstract PhysicalEntity addToModel(SemSimModel model);
}
