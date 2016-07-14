package semgen.stage.serialization;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

import semsim.model.collection.SemSimCollection;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalEntity;

public class PhysioMapNode extends Node<PhysicalProcess> {

	@Expose public ArrayList<Link> sources = new ArrayList<Link> ();
	@Expose public ArrayList<Link> sinks = new ArrayList<Link>();
	@Expose public ArrayList<Link> mediators = new ArrayList<Link> ();
	
	public PhysioMapNode(PhysicalProcess proc, Node<? extends SemSimCollection> parent) {
		super(proc, parent);
		typeIndex = PROCESS;
	}
	
	public void addSourceLink(Node<PhysicalEntity> source) {
		sources.add(new Link(this, source));
	}
	
	public void addSinkLink(Node<PhysicalEntity> sink) {
		sources.add(new Link(this, sink));
	}

	public void addMediatorLink(Node<PhysicalEntity> mediator) {
		new Link(this, mediator, Node.MEDIATOR);
	}
	
	
}
