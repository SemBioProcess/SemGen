/**
 * Represents a dependency node in the d3 graph
 */

DependencyNode.prototype = new Node();
DependencyNode.prototype.constructor = Node;
function DependencyNode (graph, data, parentNode) {
	// We need to keep the ids of each dependency node unique by prefixing
	// it with its parent node
	
	// Update the id of each node to contain its parent node
	var id = parentNode.id + data.name;
	
	// Update the id of each link to contain the parent node id
	if(data.links) {
		for(var i = 0; i < data.links.length; i++) {
			data.links[i] = parentNode.id + data.links[i];
		}
	}
	
	Node.prototype.constructor.call(this, graph, id, data.name, 5, data.group, 11);
	this.links = data.links;
	
	this.addClassName("dependencyNode");
}