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

/**
 * Initializes the node.
 * Returns true if this element has already been initialized
 */
Node.prototype.initialize = function (element) {
	var oldElement = this.element;
	this.element = element;
	return oldElement == this.element;
}

Node.prototype.tickHandler = function (element) {
	$(element).attr("transform", "translate(" + this.x + "," + this.y + ")");
}