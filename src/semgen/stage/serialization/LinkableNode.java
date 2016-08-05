package semgen.stage.serialization;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.annotations.Expose;

import semsim.model.SemSimComponent;

public class LinkableNode<O extends SemSimComponent> extends Node<O> {

	@Expose public ArrayList<Link> inputs = new ArrayList<Link>();
	@Expose public boolean isorphaned = false;
	
	protected LinkableNode(O obj, Node<?> parent) {
		super(obj, parent);
		
	}

	protected LinkableNode(O obj, Node<?> parent, Number nodetype) {
		super(obj, parent, nodetype);
		
	}
	
	protected LinkableNode(LinkableNode<O> original) {
		super(original);
		this.inputs = new ArrayList<Link>(original.inputs);
		copyLinks(original.inputs);
	}
	
	public void addLink(LinkableNode<O> inputnode) {
		inputs.add(new Link(this, inputnode));
	}
	
	public LinkableNode(LinkableNode<O> original, Node<?> parent) {
		super(original, parent);
		copyLinks(original.inputs);
	}
	
	private void copyLinks(ArrayList<Link> oldlinks) {		
		for (Link oldlink : oldlinks) {
			inputs.add(new Link(oldlink, this));
		}
	}

	//Use the provide hashmap to replace link inputs, discard any links that don't have a corresponding object
	public void replaceLinkInputs(HashMap<LinkableNode<?>, LinkableNode<?>> dsnodemap) {
		ArrayList<Link> linklist = new ArrayList<Link>();
		for (Link input : inputs) {
			if (input.replaceInput(dsnodemap)) linklist.add(input);			
		}
		inputs = linklist;
	}

}
