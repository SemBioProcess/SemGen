/**
 * Represents a node in the d3 graph
 */
function Node(graph, name, parent, inputs, r, color, textSize, nodeType) {
	this.graph = graph;
	this.name = name;
	this.id = name;
	this.displayName = name;
	this.r = r;
	this.color = color;
	this.textSize = textSize;
	this.nodeType = nodeType;
	this.className = "node";
	this.element;
	this.parent = parent;
	this.inputs = inputs || [];
	this.userCanHide = true;
	this.hidden = false;
	
	if(this.parent) {
		// We need to keep the ids of each node unique by prefixing
		// it with its parent node's id
		this.id = this.parent.id + this.id;
	}
}

Node.prototype.addClassName = function (className) {
	this.className += " " + className;
}

Node.prototype.addBehavior = function (behavior) {
	// Behaviors are just functions that take in a node as an argument
	// To add a behavior all we need to do is call the function
	//
	// Note: I added this function to make adding a behavior easier to read
	// (e.g. this.addBehavior(SomeBehavior); )
	behavior(this);
}

Node.prototype.createVisualElement = function (element, graph) {
	this.rootElement = d3.select(element);

	this.rootElement.attr("class", this.className)
		.call(graph.force.drag)
    	.style("fill", this.color)
    	
    this.rootElement.append("svg:circle")
	    .attr("r", this.r)
	    .attr("id", "Node;"+this.id)
	    .attr("class","nodeStrokeClass");
	
	// Create the text elements
	this.createTextElement("shadow");
	this.createTextElement();
	
	$(this).triggerHandler('createVisualization', [this.rootElement]);
}

Node.prototype.canLink = function () {
	return true;
}

Node.prototype.getInputs = function () {
	if(!this.inputs)
		return null;
	
	// Don't show any inputs to this node if it has children
	if(!this.canLink())
		return
	
	// Update the id of each input to contain the parent node id
	// so we can look it up by it's id
	var inputs = [];
	for(var i = 0; i < this.inputs.length; i++) {
		var inputData = this.inputs[i];
		var inputNodeId;
		
		// If the input is an object it specifies a parent
		if(typeof inputData == "object") {
			var parent = this.graph.findNode(inputData.parents.join(''));
			
			// If the parent can link add a link to it
			if(parent.canLink())
				inputNodeId = parent.id;
			// Otherwise, add a link to its child
			else
				inputNodeId = parent.id + inputData.name;
		}
		// Otherwise, it is a string referring to the input node
		else
			inputNodeId = this.parent.id + inputData;
		
		// Get the input node
		var inputNode = this.graph.findNode(inputNodeId);
		
		if(!inputNode) {
			console.log("input node '" + inputNodeId + "' does not exist. Can't build link.");
			continue;
		}
		
		// If the parent has children it's circle is hidden
		// so we don't want to show any inputs to it
		if(!inputNode.canLink()) {
			console.log("input node '" + inputNodeId + "' has its circle hidden. Can't build link.");
			continue;
		}
		
		inputs.push(inputNode);
	}
	
	return inputs;
}

Node.prototype.tickHandler = function (element, graph) {
	$(this).triggerHandler('preTick');
	
	this.x = Math.max(this.r, Math.min(graph.w - this.r, this.x));
	this.y = Math.max(this.r, Math.min(graph.h - this.r, this.y));
	
	var root = d3.select(element);
	root.attr("transform", "translate(" + this.x + "," + this.y + ")");
}

Node.prototype.getKeyInfo = function () {
	return {
		nodeType: this.nodeType,
		color: this.color,
		canShowHide: this.userCanHide,
	};
}

Node.prototype.createTextElement = function (className) {
	className = className || "";
	distanceFromNode = this.r * 0.2;
	this.rootElement.append("svg:text")
		.attr("font-size", this.textSize + "px")
	    .attr("x", 0)
	    .attr("y", -this.textSize - distanceFromNode)
	    .text(this.displayName)
	    .attr("class", className)
	    .attr("text-anchor", "middle");
}