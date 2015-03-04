/**
 * Represents a node in the d3 graph
 */
function Node(graph, id, displayName, r, color, textSize, nodeType) {
	this.graph = graph;
	this.id = id;
	this.displayName = displayName;
	this.r = r;
	this.color = color;
	this.textSize = textSize;
	this.nodeType = nodeType;
	this.className = "node";
	this.element;
	this.links = [];
	this.userCanHide = true;
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