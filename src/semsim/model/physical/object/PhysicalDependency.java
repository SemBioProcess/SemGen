package semsim.model.physical.object;

import semsim.definitions.SemSimTypes;
import semsim.model.computational.Computation;
import semsim.model.physical.PhysicalModelComponent;

public class PhysicalDependency extends PhysicalModelComponent{
	private Computation associatedComputation;
	
	public PhysicalDependency() {
		super(SemSimTypes.PHYSICAL_DEPENDENCY);
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
