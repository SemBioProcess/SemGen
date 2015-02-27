/**
 * Sub model nodes
 */
SubmodelNode.prototype = new ParentNode();
SubmodelNode.prototype.constructor = ParentNode;
function SubmodelNode (graph, data, parent) {
	ParentNode.prototype.constructor.call(this, graph, data.name, data.name, 10, 0, 14);
	
	this.addClassName("submodelNode");
}