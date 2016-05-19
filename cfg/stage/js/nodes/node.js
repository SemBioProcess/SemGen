/**
 * Represents a node in the d3 graph
 */


function Node(graph, name, parent, inputs, r, textSize, nodeType, charge) {
	if(!graph)
		return;

	this.graph = graph;
	this.name = name;
	this.id = name;
	this.displayName = name;
	this.r = r;
	this.textSize = textSize;
	this.nodeType = NodeTypeMap[nodeType];
	this.charge = charge;
	this.className = "node";
	this.element;
	this.parent = parent;
	this.inputs = inputs || [];
	this.hidden = false;
	this.textlocx = 0;
	this.wasfixed = false;
	
	this.timer = null;
	this.clicks = 0;
	if(this.parent) {
		// We need to keep the ids of each node unique by prefixing
		// it with its parent node's id
		if (this.parent.id != "null") {
			this.id = this.parent.id + this.id;
		}
	}
	validateNode(this);
	this.dragstart = [];
	this.drag = [];
	this.dragend = [];
	this.addBehavior(NodeDrag);
}

//Get this node's top level parent
Node.prototype.getRootParent = function () {
	var node = this;
	while (node.parent!=null) {
		node = node.parent;
	}
	return node;
}

Node.prototype.addClassName = function (className) {
	this.className += " " + className;
}

Node.prototype.hasClassName = function (className) {
	return this.className.indexOf(className) > -1;
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
    	.style("fill", this.nodeType.color)
    	.attr("id", "Node;"+this.id);

	var circleSelection = this.rootElement.append("svg:circle")
										.attr("r", this.r)

										.attr("class","nodeStrokeClass")
										.on("mouseover", function (d) {
											graph.highlightMode(d);
										})
										.on("mouseout", function () {
											graph.highlightMode(null);
										});

	if(this.nodeType.nodeType == "Null") {
		circleSelection.attr("stroke", "black")
			.attr("stroke-width", 0.5);
		this.rootElement.append("svg:line")
			.attr("x1", this.r)
			.attr("x2", -this.r)
			.attr("y1", -this.r)
			.attr("y2", this.r)
			.attr("stroke", "black")
			.attr("stroke-width", 1);
	};

	this.rootElement.on("click", function (node) {
		node.onClick();
	});

	//Append highlight circle
	this.rootElement.append("svg:circle")
		.attr("class", "highlight")
		.attr("r", this.r + 4)
		.attr("stroke", "yellow")
		.attr("stroke-width", "4");

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
			if (this.parent.id != "null" && inputData.parentModelId != "null") { 
				
				var parent = this.graph.findVisibleNode(inputData.parentModelId);
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
			else {
				inputNodeId = inputData.name;
			}
		}

		else if(this.nodeType == NodeType.ENTITY || this.nodeType == NodeType.PROCESS || this.nodeType == NodeType.NULLNODE) {
			type = "physiomap";
			inputNodeId = inputData.sourceId;
			outputNodeId = inputData.sinkId;
			if(inputData.linkType == "Mediator") {
				type = "Mediator";
			}

		}

		else {
			type = "internal";
			inputNodeId = inputData.sourceId;
			outputNodeId = inputData.sinkId;
		}

		// Get the input node
		var inputNode = this.graph.findVisibleNode(inputNodeId);

		// Get the sink node
		var outputNode = outputNodeId === undefined ? this : this.graph.findVisibleNode(outputNodeId);

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

		if(type == "external") length = this.graph.linklength;
		else if(type == "physiomap") length = Math.round(this.graph.linklength/3);
		else length = Math.round(this.graph.linklength/5);

		if(type == "Mediator")
			var newLink = new MediatorLink(this.graph, linkLabel, inputNode, outputNode, length, type);
		else var newLink = new Link(this.graph, linkLabel, inputNode, outputNode, length, type);
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
		if (this.parent.id != "null") {
			var k = .0005;
			forcey = (this.parent.y - this.y) * k;
			forcex = (this.parent.x - this.x) * k;
		}

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

Node.prototype.createTextElement = function (className) {

	this.rootElement.append("svg:text")
		.attr("font-size", this.textSize + "px")
	    .attr("x", 0)
	    .attr("y", -this.spaceBetweenTextAndNode())
	    .text(this.displayName)
	    .attr("class", className)
	    .attr("text-anchor", "middle");
}


Node.prototype.highlight = function () {
		this.rootElement.classed("selected", this.rootElement.select("circle").style("display")!="none");
}

Node.prototype.removeHighlight = function () {
	this.rootElement.classed("selected", false);
}

Node.prototype.onClick = function () {
	this.clicks++;

	if(this.clicks == 1) {

		node = this;
		this.timer = setTimeout(function() {
			node.clicks = 0;             //after action performed, reset counter
	        main.task.selectNode(node);
	    }, 500);

	}
    else {
        clearTimeout(this.timer);    //prevent single-click action
        this.clicks = 0;             //after action performed, reset counter
    	this.onDoubleClick();

    }
      d3.event.stopPropagation();

}

Node.prototype.isVisible = function () {
	return (!this.hidden && this.graph.nodesVisible[this.nodeType.id]);
}

Node.prototype.onDoubleClick = function () {}

Node.prototype.globalApply = function (funct) {
	funct(this);
}

Node.prototype.applytoChildren = function(funct) {}

function validateNode(nodeData) {
	if(!nodeData)
		throw "Invalid node data";

	if(typeof nodeData.id != "string")
		throw "Node id must be a string";

	if(typeof nodeData.r != "number")
		throw "Node radius must be a number";

	if(typeof nodeData.charge != "number")
		throw "Charge must be a number";

	if(typeof nodeData.getLinks != "function")
		throw "Node getLinks is not defined";

	if(typeof nodeData.createVisualElement != "function")
		throw "Node createVisualElement is not defined";

	if(typeof nodeData.tickHandler != "function")
		throw "Node tickHandler is not defined";

};
