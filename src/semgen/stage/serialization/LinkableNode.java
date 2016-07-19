package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.annotations.Expose;

import semsim.model.SemSimComponent;

public class LinkableNode<O extends SemSimComponent> extends Node<O> {

	@Expose public ArrayList<Link> inputs = new ArrayList<Link>();
	
	protected LinkableNode(O obj, Node<?> parent) {
		super(obj, parent);
		
	}

	public LinkableNode(LinkableNode<O> original) {
		super(original);
		this.inputs = new ArrayList<Link>(original.inputs);
	}
	
	public LinkableNode(LinkableNode<O> original, Node<?> parent) {
		super(original, parent);
		this.inputs = original.inputs;
		this.inputs = new ArrayList<Link>(original.inputs);
	}
	
	public void replaceLinks(HashMap<? extends LinkableNode<O>, ? extends LinkableNode<O>> dsnodemap) {
		ArrayList<Link> newinputs = new ArrayList<Link>();

		for (Link oldlink : inputs) {
			LinkableNode<O> replacement = dsnodemap.get(oldlink.input);
			if (replacement!=null) {
				newinputs.add(new Link(this, replacement));
			}
		}
		inputs = newinputs;
	}
}
