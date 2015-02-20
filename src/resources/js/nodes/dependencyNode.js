/**
 * Represents a dependency node in the d3 graph
 */

DependencyNode.prototype = new Node();
DependencyNode.prototype.constructor = Node;
function DependencyNode (data, parentNode) {
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
	
	// Place parent model's location (plus jitter)
	this.x = parentNode.x + Math.random();
	this.y = parentNode.y + Math.random();
	
	Node.prototype.constructor.call(this, id, data.name, 5, data.group);
	this.links = data.links;
	
	this.addClassName("dependencyNode");
}