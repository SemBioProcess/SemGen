package semsim.model.physical;

import semsim.definitions.SemSimTypes;
import semsim.model.computational.Computation;

/**
 * Class for working with the computational relationships between physical properties
 * in a model.
 * 
 * Reflects OPB:Physics dependency
 * "Physics model entity that is a rule that relates the existence and attributes
 *  of physics continuants to the occurrence and time-course of changes of physical
 *  conditions during physical processes."
 * 
 * "Physical dependencies are represented as an axioms, definitions and empirical laws
 * of physics for the observation, understanding, and analysis of real biophysical 
 * systems. They are represented by a broad class of quantitative and qualitative 
 * relationships amongst physical entities and the attributes of physical entities, 
 * physical continuants, and physical processes. These are the rules by which 
 * dynamical and information systems operate and are routinely encoded for analysis
 * and simulation in a variety of computational and mathematical systems.
 * 
 * Examples include: Ohm's law for electricity and for fluid flow, the definition of
 * kinetic energy in terms of mass and velocity."
 * @author mneal
 *
 */

public abstract class PhysicalDependency extends PhysicalModelComponent{
	private Computation associatedComputation;
	
	public PhysicalDependency(SemSimTypes type) {
		super(type);
	}
	
	/**
	 * Copy constructor
	 * @param pdtocopy Object to copy
	 */
	public PhysicalDependency(PhysicalDependency pdtocopy) {
		super(pdtocopy);
	}
	
	/**
	 * Link this PhysicalDependency to a {@link Computation} that provides
	 * its mathematics 
	 * @param associatedComputation The associated {@link Computation}
	 */
	public void setAssociatedComputation(Computation associatedComputation) {
		this.associatedComputation = associatedComputation;
	}

	/** @return The {@link Computation} that provides the mathematical implementation
	 * for this PhysicalDependency */
	public Computation getAssociatedComputation() {
		return associatedComputation;
	}

	@Override
	protected boolean isEquivalent(Object obj) {
		return false;
	}
	
	@Override
	public String getComponentTypeAsString() {
		return "dependency";
	}
}
