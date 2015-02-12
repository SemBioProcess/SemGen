/**
 * Represents a node in the d3 graph
 */
function Node(id, r) {
	this.id = id;
	this.r = r;
	this.className = "node";
	this.element;
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