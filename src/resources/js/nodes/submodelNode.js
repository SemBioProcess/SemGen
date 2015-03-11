/**
 * Sub model nodes
 */
SubmodelNode.prototype = new ParentNode();
SubmodelNode.prototype.constructor = ParentNode;
function SubmodelNode (graph, data, parent) {
	ParentNode.prototype.constructor.call(this, graph, data.name, parent, data.links, 10, "#CA9485", 14, "Submodel");
	
	this.addClassName("submodelNode");
}