package semsim.model.physical.object;

import semsim.model.computational.Computation;
import semsim.model.physical.PhysicalModelComponent;

public class PhysicalDependency extends PhysicalModelComponent{
	private Computation associatedComputation;
	
	
	public void setAssociatedComputation(Computation associatedComputation) {
		this.associatedComputation = associatedComputation;
	}

	public Computation getAssociatedComputation() {
		return associatedComputation;
	}

	@Override
	public String getComponentTypeasString() {
		return "dependency";
	}
	
}
