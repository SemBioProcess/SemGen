package semgen.stage.serialization;

import semsim.model.collection.SemSimCollection;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalEntity;

public class PhysioMapNode extends LinkableNode<PhysicalProcess> {

	public PhysioMapNode(PhysicalProcess proc, Node<? extends SemSimCollection> parent) {
		super(proc, parent);
		typeIndex = PROCESS;
	}
	
	public void addSourceLink(LinkableNode<PhysicalEntity> source) {
		inputs.add(new Link(this, source));
	}
	
	public void addSinkLink(LinkableNode<PhysicalEntity> sink) {
		inputs.add(new Link(sink, this));
	}

	public void addMediatorLink(LinkableNode<PhysicalEntity> mediator) {
		inputs.add(new Link(this, mediator, Node.MEDIATOR));
	}
	

	
}
