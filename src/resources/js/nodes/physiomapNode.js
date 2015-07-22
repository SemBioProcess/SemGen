/**
 * Represents a physiomap node in the d3 graph
 */

PhysioMapNode.prototype = new Node();
PhysioMapNode.prototype.constructor = PhysioMapNode;
function PhysioMapNode (graph, data, parentNode) {
	Node.prototype.constructor.call(this, graph, data.id, data.name, parentNode, data.inputs, 5, "#1F77B4", 14, "Entity", -300);
	
	this.displayName = data.name.replace("Portion of ", "").capitalizeFirstLetter();
	this.addClassName("physiomapNode");
	this.addBehavior(HiddenLabelNodeGenerator);

}