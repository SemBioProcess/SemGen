/**
 * Represents a node in the d3 graph
 */
function Node(id, displayName, r, group, textSize) {
	this.id = id;
	this.displayName = displayName;
	this.r = r;
	this.group = group;
	this.textSize = textSize;
	this.className = "node";
	this.element;
	this.links = [];
}

Node.prototype.addClassName = function (className) {
	this.className += " " + className;
}

Node.prototype.createVisualElement = function (element, graph) {
	this.rootElement = d3.select(element);

	this.rootElement.attr("class", this.className)
		.call(graph.force.drag)
    	.style("fill", graph.color(this.group))
    	
    this.rootElement.append("svg:circle")
	    .attr("r", this.r)
	    .attr("id", "Node;"+this.id)
	    .attr("class","nodeStrokeClass");
	
	// Create the text element
	Node.appendTextElement(this.rootElement, this.textSize, this.displayName);
}

Node.prototype.tickHandler = function (element, graph) {
	this.x = Math.max(this.r, Math.min(graph.w - this.r, this.x));
	this.y = Math.max(this.r, Math.min(graph.h - this.r, this.y));
	
	var root = d3.select(element);
	root.attr("transform", "translate(" + this.x + "," + this.y + ")");
}

Node.appendTextElement = function (root, size, text) {
	// Create the text element
	var createElement = function (className) {
		className = className || "";
		root.append("svg:text")
			.attr("font-size", size + "px")
		    .attr("x", 0)
		    .attr("y", -size - 5)
		    .text(text)
		    .attr("class", className)
		    .attr("text-anchor", "middle");
	};
	
	// Create text with a thick white stroke for legibility.
	createElement("shadow");
	createElement();
}