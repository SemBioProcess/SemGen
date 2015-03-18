/**
 * Sub model nodes
 */
SubmodelNode.prototype = new ParentNode();
SubmodelNode.prototype.constructor = ParentNode;
function SubmodelNode (graph, data, parent) {
	ParentNode.prototype.constructor.call(this, graph, data.name, parent, data.inputs, 10, "#CA9485", 14, "Submodel");
	
	this.addClassName("submodelNode");
}

SelectionManager.getInstance().onSelected(function (e, element, node) {
	if(!(node instanceof SubmodelNode))
		return;
	
	sender.submodelClicked(node.parent.name, node.name);
	
	e.stopPropagation();
});