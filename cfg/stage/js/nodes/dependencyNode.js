/**
 * Represents a dependency node in the d3 graph
 */

DependencyNode.prototype = new Node();
DependencyNode.prototype.constructor = DependencyNode;

function DependencyNode (graph, data, parentNode) {
	// Ensure the node type is formatted properly
	data.nodeType = data.nodeType.toLowerCase().capitalizeFirstLetter();

	// Get the correct group from the node type
	this.group = data.id;
	if(this.group == "undefined")
		throw "invalid dependency node type: " + data.nodeType;
		
	Node.prototype.constructor.call(this, graph, data.name, parentNode, data.inputs, 5, 14, data.nodeType, defaultcharge);

	this.depindex = typeToGroup[data.nodeType];
	
	this.color = typeToColor[this.depindex];
	DependencyNode.prototype.color = this.color;
	
	this.addClassName("dependencyNode");
	//this.addBehavior(Columns);
	this.addBehavior(HiddenLabelNodeGenerator);
}

function depenKey(i) {
	return {
		nodeType: groupToType[i], 
		color: typeToColor[i], 
		canShowHide: true,
	};
}

// Maps node type to group number
var typeToGroup = {
		"State": 0,
		"Rate": 1,
		"Constitutive": 2,
};

var groupToType = [
		"State",
		"Rate",
		"Constitutive",
];

// Maps node type to node color
var typeToColor = [
		"#1F77B4",
		"#2CA02C",
		"#FF7F0E"
	];


