/**
 * Represents a node in the d3 graph
 */
function Node(id, displayName, r, group) {
	this.id = id;
	this.displayName = displayName;
	this.r = r;
	this.group = group;
	this.className = "node";
	this.element;
	this.links = [];
}

Node.prototype.addClassName = function (className) {
	this.className += " " + className;
}

Node.prototype.createVisualElement = function (element) {
	var root = d3.select(element);
	
    root.append("svg:circle")
	    .attr("r", this.r)
	    .attr("id", "Node;"+this.id)
	    .attr("class","nodeStrokeClass");
    
	// A copy of the text with a thick white stroke for legibility.
    root.append("svg:text")
	    .attr("x", 20)
	    .attr("y", ".31em")
	    .attr("class", "shadow")
	    .text(this.displayName);

    root.append("svg:text")
	    .attr("x", 20)
	    .attr("y", ".31em")
	    .text(this.displayName);
}

Node.prototype.tickHandler = function (element) {
	$(element).attr("transform", "translate(" + this.x + "," + this.y + ")");
}