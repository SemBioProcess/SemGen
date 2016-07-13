package semgen.stage.serialization;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

import semsim.model.collection.SemSimCollection;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalEntity;

public class PhysioMapNode extends Node<PhysicalProcess> {

	@Expose public ArrayList<Node<PhysicalEntity>> sources = new ArrayList<Node<PhysicalEntity>>();
	@Expose public ArrayList<Node<PhysicalEntity>> sinks = new ArrayList<Node<PhysicalEntity>>();
	@Expose public ArrayList<Node<PhysicalEntity>> mediators = new ArrayList<Node<PhysicalEntity>>();
	
	public PhysioMapNode(PhysicalProcess proc, Node<? extends SemSimCollection> parent) {
		super(proc, parent);
		typeIndex = PROCESS;
	}
}
