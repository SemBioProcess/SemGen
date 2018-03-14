package semgen.stage.serialization;

import com.google.gson.annotations.Expose;
import semgen.stage.stagetasks.extractor.Extractor;
import semsim.model.collection.SemSimCollection;
import semsim.model.physical.PhysicalProcess;

import java.util.ArrayList;

public class PhysioMapNode extends LinkableNode<PhysicalProcess> {
	@Expose public ArrayList<String> sourcenames = new ArrayList<String>();
	@Expose public ArrayList<String> sinknames = new ArrayList<String>();
	@Expose public ArrayList<String> mediatornames = new ArrayList<String>();	
	
	public PhysioMapNode(PhysicalProcess proc, Node<? extends SemSimCollection> parent) {
		super(proc, parent);
		typeIndex = PROCESS;
	}
	
	public void addSourceLink(PhysioMapEntityNode source) {
		inputs.add(new Link(this, source));
		sourcenames.add(source.name + ": " + this.sourceobj.getSourceStoichiometry(source.sourceobj));
	}
	
	public void addSinkLink(PhysioMapEntityNode sink) {
		sink.addLink(this);
		sinknames.add(sink.name + ": " + this.sourceobj.getSinkStoichiometry(sink.sourceobj));
	}

	public void addMediatorLink(PhysioMapEntityNode mediator) {
		inputs.add(new Link(this, mediator, Node.MEDIATOR));
		mediatornames.add(mediator.name);
	}
	
	@Override
	public void collectforExtraction(Extractor extractor) {
		extractor.addProcess(sourceobj);
	}
	
}
