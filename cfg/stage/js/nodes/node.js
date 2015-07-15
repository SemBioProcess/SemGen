/**
 * Represents a node in the d3 graph
 */
function Node(graph, name, parent, inputs, r, color, textSize, nodeType, charge) {
	if(!graph)
		return;

	this.graph = graph;
	this.name = name;
	this.id = name;
	this.displayName = name;
	this.r = r;
	this.color = color;
	this.textSize = textSize;
	this.nodeType = nodeType;
	this.charge = charge;
	this.className = "node";
	this.element;
	this.parent = parent;
	this.inputs = inputs || [];
	this.userCanHide = true;
	this.hidden = false;
	this.spaceBetweenTextAndNode = this.r * 0.2 + this.textSize;
	
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
	    .attr("class","nodeStrokeClass")
	    .on("mouseover", function (d) {
	    	graph.highlightMode(d);
	    })
	    .on("mouseout", function () {
	    	graph.highlightMode(null);
	    });;
	
	// Create the text elements
	this.createTextElement("shadow");
	this.createTextElement("real");
	
	$(this).triggerHandler('createVisualization', [this.rootElement]);
}

Node.prototype.canLink = function () {
	return true;
}

Node.prototype.getLinks = function () {
	if(!this.inputs)
		return null;
	
	// Don't show any inputs to this node can't link
	if(!this.canLink())
		return;
	
	// Build an array of links from our list of inputs
	var links = [];
	for(var i = 0; i < this.inputs.length; i++) {
		var inputData = this.inputs[i];
		var inputNodeId;
		var type;
		
		// If the input is an object it specifies a parent
		if(typeof inputData == "object") {
			type = "external";
			var parent = this.graph.findNode(inputData.parents.join(''));
			
			if(!parent) {
				console.log("External link without a parent!");
				continue;
			}
			
			// If the parent can link add a link to it
			if(parent.canLink())
				inputNodeId = parent.id;
			// Otherwise, add a link to its child
			else
				inputNodeId = parent.id + inputData.name;
		}
		// Otherwise, it is a string referring to the input node
		else {
			type = "internal";
			inputNodeId = this.parent.id + inputData;
		}
		
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
		
		links.push({
			source: inputNode,
			target: this,
			type: type,
			length: type == "external" ? 200 : 40,
			value: 1,
		});
	}
	
	return links;
}

Node.prototype.tickHandler = function (element, graph) {
	$(this).triggerHandler('preTick');
	
	// Don't let nodes overlap their parent labels
	if(this.parent && this.y < this.parent.y)
		this.y = this.parent.y;
	
	
	this.x = Math.max(this.r, Math.min(graph.w - this.r, this.x));
	this.y = Math.max(this.r + this.spaceBetweenTextAndNode, Math.min(graph.h - this.r, this.y));
	
	var root = d3.select(element);
	root.attr("transform", "translate(" + this.x + "," + this.y + ")");
	
	$(this).triggerHandler('postTick');
}

Node.prototype.getKeyInfo = function () {
	return {
		nodeType: this.nodeType,
		color: this.color,
		canShowHide: this.userCanHide,
	};
}

Node.prototype.createTextElement = function (className) {
	
	this.rootElement.append("svg:text")
		.attr("font-size", this.textSize + "px")
	    .attr("x", 0)
	    .attr("y", -this.spaceBetweenTextAndNode)
	    .text(this.displayName)
	    .attr("class", className)
	    .attr("text-anchor", "middle");
}
