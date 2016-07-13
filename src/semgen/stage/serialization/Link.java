package semgen.stage.serialization;

import semsim.model.SemSimComponent;

import com.google.gson.annotations.Expose;

public class Link {
	@Expose public String id;
	@Expose public String linkType;
	@Expose public Boolean external;
	@Expose public Node<? extends SemSimComponent> input;
	@Expose public Node<? extends SemSimComponent> output;
	
	public Link(Node<? extends SemSimComponent> outputnode, Node<? extends SemSimComponent> inputnode) {
		input = inputnode;
		output = outputnode;
		
		id = inputnode.id + "-" + outputnode.id;
		
		external = (inputnode.parent.id!=outputnode.parent.id); 
	}
}
