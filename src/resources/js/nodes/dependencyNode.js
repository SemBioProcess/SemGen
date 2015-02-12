/**
 * Represents a dependency node in the d3 graph
 */

DependencyNode.prototype = new Node();
DependencyNode.prototype.constructor = Node;
function DependencyNode (data) {
	Node.prototype.constructor.call(this, data.id, 5, data.group);
	this.links = data.links;
	
	this.addClassName("dependencyNode");
}