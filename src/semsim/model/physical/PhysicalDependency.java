package semsim.model.physical;

import semsim.definitions.SemSimTypes;
import semsim.model.computational.Computation;

public abstract class PhysicalDependency extends PhysicalModelComponent{
	private Computation associatedComputation;
	
	public PhysicalDependency(SemSimTypes type) {
		super(type);
	}
	
	public PhysicalDependency(PhysicalDependency pdtocopy) {
		super(pdtocopy);
	}
	
	public void setAssociatedComputation(Computation associatedComputation) {
		this.associatedComputation = associatedComputation;
	}

	public Computation getAssociatedComputation() {
		return associatedComputation;
	}

	@Override
	protected boolean isEquivalent(Object obj) {
		return false;
	}
	
	@Override
	public String getComponentTypeasString() {
		return "dependency";
	}
}
