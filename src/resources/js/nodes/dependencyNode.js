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
	
	// Ensure the node type is formatted properly
	data.nodeType = data.nodeType.toLowerCase().capitalizeFirstLetter();
	
	// Get the correct group from the node type
	this.group = typeToGroup[data.nodeType];
	if(this.group == "undefined")
		throw "invalid dependency node type: " + data.nodeType;
	
	Node.prototype.constructor.call(this, graph, id, data.name, 5, typeToColor[data.nodeType], 11, data.nodeType);
	this.links = data.links;
	
	this.addClassName("dependencyNode");
}

// Maps node type to group number
var typeToGroup = {
		"State": 0,
		"Rate": 1,
		"Constitutive": 2,
};

// Maps node type to node color
var typeToColor = {
		"State": "#1F77B4",
		"Rate": "#2CA02C",
		"Constitutive": "#FF7F0E"
};