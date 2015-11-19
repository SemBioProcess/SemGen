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
	this.textlocx = 0;
	this.defaultcharge = charge;

	if(this.parent) {
		// We need to keep the ids of each node unique by prefixing
		// it with its parent node's id
		this.id = this.parent.id + this.id;
	}
}

Node.prototype.addClassName = function (className) {
	this.className += " " + className;
}

Node.prototype.spaceBetweenTextAndNode = function() {
	return this.r * 0.2 + this.textSize; 
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
			});

	//Blue circle with black border to indicate mediator entities
	if(this.nodeType == "Mediator") {
		this.rootElement.append("svg:circle")
				.attr("r", this.r)
				.attr("stroke", "black")
				.attr("stroke-width", "2")
				.attr("id", "Node;"+this.id)
				.attr("class","nodeStrokeClass")
				.on("mouseover", function (d) {
					graph.highlightMode(d);
				})
				.on("mouseout", function () {
					graph.highlightMode(null);
				});
	}
	
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
		var outputNodeId;
		var type;
		var linkLabel = inputData.label;

		// If the linked node is in a different parent, mark it as external
		if(inputData.parentModelId != this.parent.id) {
			type = "external";
			var parent = this.graph.findNode(inputData.parentModelId);
			if (!parent) {
				console.log("External link without a parent!");
				continue;
			}

			// If the parent can link add a link to it
			if (parent.canLink())
				inputNodeId = parent.id;
			// Otherwise, add a link to its child
			else {
				type = "internal";
				inputNodeId = this.parent.id + inputData.name;
			}
		}

		else if(this.nodeType == "Entity" || this.nodeType == "Process") {
			type = "physiomap";
			inputNodeId = inputData.sourceId;
			outputNodeId = inputData.sinkId;
			if(inputData.linkType == "Mediator") {
				type += " mediator";
			}

		}

		else {
			type = "internal";
			inputNodeId = inputData.sourceId;
			outputNodeId = inputData.sinkId;
		}

		// Get the input node
		var inputNode = this.graph.findNode(inputNodeId);
		
		// Get the sink node
		var outputNode = outputNodeId === undefined ? this : this.graph.findNode(outputNodeId);
		
		if(!inputNode) {
			console.log("input node '" + inputNodeId + "' does not exist. Can't build link.");
			continue;
		}
		
		if(!outputNode) {
			console.log("sink node '" + outputNodeId + "' does not exist. Can't build link.");
			continue;
		}
		
		// If the parent has children it's circle is hidden
		// so we don't want to show any inputs to it
		if(!inputNode.canLink()) {
			console.log("input node '" + inputNodeId + "' has its circle hidden. Can't build link.");
			continue;
		}

		if(type == "external") length = 300;
		else if(type == "physiomap") length = 200;
		else length = 60;

		var newLink = new Link(this.graph, linkLabel, this.parent, inputNode, outputNode, length, type);
		links.push(newLink);

	}

	
	return links;
}

Node.prototype.tickHandler = function (element, graph) {
	$(this).triggerHandler('preTick');
	
	//Keep child nodes centered on parent
	var forcey = 0;
	var forcex = 0;
	
	if (this.parent) {
		var k = .0005; 
		forcey = (this.parent.y - this.y) * k;
		forcex = (this.parent.x - this.x) * k;

	}
	this.x = Math.max(this.r, Math.min(graph.w - this.r, this.x)) + forcex;
	this.y = Math.max(this.r, Math.min(graph.h - this.r + this.spaceBetweenTextAndNode(), this.y)) + forcey;
	
	var root = d3.select(element);
	//Keep the text above hull when an parent node is opened.
	if (this.children) {
		this.rootElement.selectAll("text").attr("y", -this.spaceBetweenTextAndNode());
		this.rootElement.selectAll("text").attr("x", -(this.x - (this.xmax + this.xmin)/2.0));
	}
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
	    .attr("y", -this.spaceBetweenTextAndNode())
	    .text(this.displayName)
	    .attr("class", className)
	    .attr("text-anchor", "middle");
}
