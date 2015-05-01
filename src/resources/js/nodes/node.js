/**
 * Represents a node in the d3 graph
 */
function Node(graph, name, parent, inputs, sources, sinks, mediators, r, color, textSize, nodeType, charge) {
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
	this.sources = sources || [];
	this.sinks = sinks || [];
	this.mediators = mediators || [];
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
	if(!this.inputs) // Is this in the right place? Should it be in "canLink" function?
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
	
	// Get PhysioMap source links
	for(var i = 0; i < this.sources.length; i++) {
		var sourceData = this.sources[i];
		var sourceNodeId;
		var type;
		
		// If the source is an object it specifies a parent
		if(typeof sourceData == "object") {
			type = "external";
			var parent = this.graph.findNode(sourceData.parents.join(''));
			
			if(!parent) {
				console.log("External link without a parent!");
				continue;
			}
			
			// If the parent can link add a link to it
			if(parent.canLink())
				sourceNodeId = parent.id;
			// Otherwise, add a link to its child
			else
				sourceNodeId = parent.id + sourceData.name;
		}
		// Otherwise, it is a string referring to the source node
		else {
			type = "internal";
			sourceNodeId = this.parent.id + sourceData;
		}
		
		// Get the source node
		var sourceNode = this.graph.findNode(sourceNodeId);
		
		if(!sourceNode) {
			console.log("source node '" + sourceNodeId + "' does not exist. Can't build link.");
			continue;
		}
		
		// If the parent has children it's circle is hidden
		// so we don't want to show any sources to it
		if(!sourceNode.canLink()) {
			console.log("source node '" + sourceNodeId + "' has its circle hidden. Can't build link.");
			continue;
		}
		
		links.push({
			source: sourceNode,
			target: this,
			type: type,
			length: type == "external" ? 200 : 40,
			value: 1,
		});
	}
	
	// Get PhysioMap sink links
	for(var i = 0; i < this.sinks.length; i++) {
		var sinkData = this.sinks[i];
		var sinkNodeId;
		var type;
		
		// If the sink is an object it specifies a parent
		if(typeof sinkData == "object") {
			type = "external";
			var parent = this.graph.findNode(sinkData.parents.join(''));
			
			if(!parent) {
				console.log("External link without a parent!");
				continue;
			}
			
			// If the parent can link add a link to it
			if(parent.canLink())
				sinkNodeId = parent.id;
			// Otherwise, add a link to its child
			else
				sinkNodeId = parent.id + sinkData.name;
		}
		// Otherwise, it is a string referring to the sink node
		else {
			type = "internal";
			sinkNodeId = this.parent.id + sinkData;
		}
		
		// Get the sink node
		var sinkNode = this.graph.findNode(sinkNodeId);
		
		if(!sinkNode) {
			console.log("sink node '" + sinkNodeId + "' does not exist. Can't build link.");
			continue;
		}
		
		// If the parent has children it's circle is hidden
		// so we don't want to show any sinks to it
		if(!sinkNode.canLink()) {
			console.log("sink node '" + sinkNodeId + "' has its circle hidden. Can't build link.");
			continue;
		}
		
		links.push({
			source: this,
			target: sinkNode,
			type: type,
			length: type == "external" ? 200 : 40,
			value: 1,
		});
	}
	
	// Get PhysioMap mediator links
	for(var i = 0; i < this.mediators.length; i++) {
		var mediatorData = this.mediators[i];
		var mediatorNodeId;
		var type;
		
		// If the mediator is an object it specifies a parent
		if(typeof mediatorData == "object") {
			type = "external";
			var parent = this.graph.findNode(mediatorData.parents.join(''));
			
			if(!parent) {
				console.log("External link without a parent!");
				continue;
			}
			
			// If the parent can link add a link to it
			if(parent.canLink())
				mediatorNodeId = parent.id;
			// Otherwise, add a link to its child
			else
				mediatorNodeId = parent.id + mediatorData.name;
		}
		// Otherwise, it is a string referring to the mediator node
		else {
			type = "internal";
			mediatorNodeId = this.parent.id + mediatorData;
		}
		
		// Get the mediator node
		var mediatorNode = this.graph.findNode(mediatorNodeId);
		
		if(!mediatorNode) {
			console.log("mediator node '" + mediatorNodeId + "' does not exist. Can't build link.");
			continue;
		}
		
		// If the parent has children it's circle is hidden
		// so we don't want to show any mediators to it
		if(!mediatorNode.canLink()) {
			console.log("mediator node '" + mediatorNodeId + "' has its circle hidden. Can't build link.");
			continue;
		}
		
		links.push({
			source: mediatorNode,
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
