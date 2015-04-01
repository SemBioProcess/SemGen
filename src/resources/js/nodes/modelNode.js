/**
 * Represents a model node in the d3 graph
 */

ModelNode.prototype = new ParentNode();
ModelNode.prototype.constructor = ModelNode;
function ModelNode (graph, name) {
	ParentNode.prototype.constructor.call(this, graph, name, null, null, 16, "#BBB0AF", 20, "Model", 0);
	this.fixed = true;
	
	this.addClassName("modelNode");
	this.x = (Math.random() * (graph.w-graph.w/3))+graph.w/6;
	this.y = (Math.random() * (graph.h-graph.h/3))+graph.h/6;
	this.addBehavior(Hull);
	this.addBehavior(ModelPopover);
	this.addBehavior(DragToMerge);
}