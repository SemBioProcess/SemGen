package semsim.model.physical;

import semsim.model.computational.Computation;

public class PhysicalDependency extends PhysicalModelComponent{
	private Computation associatedComputation;
	
	
	public void setAssociatedComputation(Computation associatedComputation) {
		this.associatedComputation = associatedComputation;
	}

	public Computation getAssociatedComputation() {
		return associatedComputation;
	}
	
}
