/**
 * Represents a dependency node in the d3 graph
 */

DependencyNode.prototype = new Node();
DependencyNode.prototype.constructor = DependencyNode;

function DependencyNode (graph, data, parentNode) {
	// Ensure the node type is formatted properly
	data.nodeType = data.nodeType.toLowerCase().capitalizeFirstLetter();

	Node.prototype.constructor.call(this, graph, data.name, parentNode, data.inputs, 5, 14, data.nodeType, defaultcharge);

	this.addClassName("dependencyNode");
	//this.addBehavior(Columns);
	this.addBehavior(HiddenLabelNodeGenerator);

}
