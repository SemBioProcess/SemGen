package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimConstants;
import semsim.model.SemSimTypes;
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
	protected boolean isEquivalent(Object obj) {
		return false;
	}
	
	@Override
	public String getComponentTypeasString() {
		return "dependency";
	}

	@Override
	public URI getSemSimClassURI() {
		return SemSimConstants.PHYSICAL_DEPENDENCY_CLASS_URI;
	}
	
	@Override
	public SemSimTypes getSemSimType() {
		return SemSimTypes.PHYSICAL_DEPENDENCY;
	}
}
