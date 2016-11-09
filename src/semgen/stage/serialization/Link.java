package semgen.stage.serialization;

import java.util.HashMap;

import com.google.gson.annotations.Expose;

public class Link {
	@Expose public String id;
	@Expose public Number linkType;
	@Expose public Number linklevel = 0;
	@Expose public Node<?> input;
	@Expose public Node<?> output;
	
	public Link(LinkableNode<?> outputnode, LinkableNode<?> inputnode) {
		input = inputnode;
		output = outputnode;
		
		id = inputnode.id + "-" + outputnode.id;
		linkType = inputnode.typeIndex;
		findLinkLevel();
	}
	
	public Link(LinkableNode<?> outputnode, LinkableNode<?> inputnode, Number type) {
		input = inputnode;
		output = outputnode;
		linkType = type;
		id = inputnode.id + "-" + outputnode.id;
		
		findLinkLevel();
	}
	
	public Link(Link linktocopy, LinkableNode<?> outputnode) {
		this.id = new String(linktocopy.id);
		this.linkType = linktocopy.linkType;
		this.linklevel = linktocopy.linklevel;
		this.input = linktocopy.input;
		this.output = outputnode;
	}
	
	public boolean hasInput(LinkableNode<?> node) {
		return input==node;
	}
	
	public boolean hasOutput(LinkableNode<?> node) {
		return output==node;
	}
	
	public boolean replaceInput(HashMap<LinkableNode<?>, LinkableNode<?>> dsnodemap) {
		if (dsnodemap.containsKey(input)) {
			input = dsnodemap.get(input);
			return true;
		}
		return false;
	}
	
	public void findLinkLevel() {
		if (input.getFirstAncestor() != output.getFirstAncestor()) {
			linklevel = 2;
		}
		else if (input.parent.id!=output.parent.id) {
			linklevel = 1;
		}
	}
}
