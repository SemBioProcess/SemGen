/**
 * Represents a node in the d3 graph
 */
function Node(id, r) {
	this.id = id;
	this.r = r;
}

Node.prototype.tickHandler = function (element) {
	$(element).attr("transform", "translate(" + this.x + "," + this.y + ")");
}