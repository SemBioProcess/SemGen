/**
 * Represents a model node in the d3 graph
 */

var openPopover;

ModelNode.prototype = new Node();
ModelNode.prototype.constructor = Node;
function ModelNode (graph, name) {
	Node.prototype.constructor.call(this, graph, name, name, 16, 0, 20);
	this.fixed = true;
	this.children = null;
	
	this.addClassName("modelNode");
	
	var hull = new Hull(this);
	var popover = new ModelPopover(this);
}

ModelNode.prototype.setChildren = function (children) {
	// Remove existing child nodes from the graph
	if(this.children) {
		this.children.forEach(function (child) {
			this.graph.removeNode(child.id);
		}, this);
	}
	
	this.children = children;

	// Show/Hide the correct elements depending on the model's state
	var circleDisplay = this.children ? "none" : "inherit";
	this.rootElement.select("circle").style("display", circleDisplay);
	
	$(this).triggerHandler('childrenSet', [children]);
}