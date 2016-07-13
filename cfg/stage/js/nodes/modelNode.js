/**
 * Represents a model node in the d3 graph
 */

ModelNode.prototype = new ParentNode();
ModelNode.prototype.constructor = ModelNode;

function ModelNode (graph, srcobj, index) {
	ParentNode.prototype.constructor.call(this, graph, srcobj, null, 16, 20, 0);
	this.fixed = true;
	this.index = index;
	this.addClassName("modelNode");
	this.canlink = false;
	
	
	this.addBehavior(Hull);	
	
	this.addBehavior(parentDrag);
	
}

ModelNode.prototype.createVisualElement = function (element, graph) {
	
	ParentNode.prototype.createVisualElement.call(this, element, graph);
	
}


