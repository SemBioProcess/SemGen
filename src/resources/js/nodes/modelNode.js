/**
 * Represents a model node in the d3 graph
 */

ModelNode.prototype = new ParentNode();
ModelNode.prototype.constructor = ParentNode;
function ModelNode (graph, name) {
	ParentNode.prototype.constructor.call(this, graph, name, name, 16, 0, 20);
	this.fixed = true;
	
	this.addClassName("modelNode");
	
	this.addBehavior(Hull);
	this.addBehavior(ModelPopover);
}