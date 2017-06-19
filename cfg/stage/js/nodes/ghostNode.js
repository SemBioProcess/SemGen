/**
 * A temporary visual copy of a node.
 */


function GhostNode(node) {
	var ghost = this;
	this.graph = node.graph;
	this.r = node.r;
	this.nodeType = node.nodeType;
	this.srcnode = node;
	this.className = "ghost";
	this.drag = [];
	this.dragEnd = [];
	
	this.graph.ghostBehaviors.forEach(function(b) {
		ghost.addBehavior(b);
	});
	
	this.x = node.xpos();
	this.y = node.ypos();

	this.xpos = function () {
		return this.x;
	}

	this.ypos = function () {
		return this.y;
	}
	
}

GhostNode.prototype.setLocation = function (x, y) {
	x = Math.max(this.graph.worldsize[0][0] + this.r*3, Math.min(this.graph.worldsize[1][0] - this.r*3, x));
	y = Math.max(this.graph.worldsize[0][1] + this.r*3, Math.min(this.graph.worldsize[1][1] - this.r*3, y));
	
	this.x = x; this.y = y;

}

GhostNode.prototype.getScreenCoordinates = function() {
	var string = $(".canvas").attr("transform");
    if(string === undefined) return [this.xpos(), this.ypos()];	
	
    var	translate = string.substring(string.indexOf("(") + 1, string.indexOf(")")).split(","),
    	scaleStr = string.substring(string.lastIndexOf("(") + 1, string.lastIndexOf(")")),
    	dx = Number(translate[0]), dy = Number(translate[1]), scale = Number(scaleStr);
	
    	return [(this.xpos() + dx), (this.ypos() + dy)];
}

GhostNode.prototype.createVisualElement = function (element, graph) {
	var node = this;

	
	this.rootElement = d3.select(element);
	this.rootElement.attr("class", this.className)
    	.style("fill", this.nodeType.color);
    	
	
	var circleSelection = this.rootElement.append("circle")
			.attr("r", this.r)

			.attr("class","nodeStrokeClass");

	circleSelection.attr("stroke", "black")
		.attr("stroke-width", 0.5);
	
	//Append highlight circle
	this.rootElement.append("circle")
		.attr("class", "highlight")
		.attr("r", this.r + 4)
		.attr("stroke", "yellow")
		.attr("stroke-width", "4");

	this.rootElement.attr("transform", "translate(" + node.x + "," + node.y + ")");
	$(this).triggerHandler('createVisualization', [this.rootElement]);
}

GhostNode.prototype.tickHandler = function (element, graph) {
	$(this).triggerHandler('preTick');
	var node = this;
	this.setLocation(
			this.xpos(), this.ypos()
	)
	
	this.rootElement.attr("transform", "translate(" + node.x + "," + node.y + ")");
	$(this).triggerHandler('postTick');
}

GhostNode.prototype.addBehavior = function (behavior) {
	// Behaviors are just functions that take in a node as an argument
	// To add a behavior all we need to do is call the function
	//
	// Note: I added this function to make adding a behavior easier to read
	// (e.g. this.addBehavior(SomeBehavior); )
	behavior(this);
}