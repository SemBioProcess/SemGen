package semgen.stage.serialization;

import semgen.stage.stagetasks.extractor.Extractor;
import semsim.model.collection.SemSimCollection;
import semsim.model.physical.PhysicalProcess;

public class PhysioMapNode extends LinkableNode<PhysicalProcess> {

	public PhysioMapNode(PhysicalProcess proc, Node<? extends SemSimCollection> parent) {
		super(proc, parent);
		typeIndex = PROCESS;
	}
	
	public void addSourceLink(PhysioMapEntityNode source) {
		inputs.add(new Link(this, source));
	}
	
	public void addSinkLink(PhysioMapEntityNode sink) {
		inputs.add(new Link(sink, this));
	}

	public void addMediatorLink(PhysioMapEntityNode mediator) {
		inputs.add(new Link(this, mediator, Node.MEDIATOR));
	}
	
	@Override
	public void collectforExtraction(Extractor extractor) {
		extractor.addProcess(sourceobj);
	}
	
}
