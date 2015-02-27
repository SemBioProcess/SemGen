/**
 * Defines nodes with children
 */
ParentNode.prototype = new Node();
ParentNode.prototype.constructor = Node;
function ParentNode(graph, id, displayName, r, group, textSize) {
	Node.prototype.constructor.call(this, graph, id, displayName, r, group, textSize);
	this.children = null;
}

ParentNode.prototype.setChildren = function (children) {
	// Remove existing child nodes from the graph
	if(this.children) {
		this.children.forEach(function (child) {
			this.graph.removeNode(child.id);
		}, this);
	}
	
	this.children = children;
	
	// If we added new children...
	if(this.children) {
		this.children.forEach(function (child) {
			// Place the children around the parent (plus jitter)
			child.x = this.x + Math.random();
			child.y = this.y + Math.random();
			
			// Add the child to the graph
			this.graph.addNode(child);
		}, this);
	}

	// Show/Hide the correct elements depending on the model's state
	var circleDisplay = this.children ? "none" : "inherit";
	this.rootElement.select("circle").style("display", circleDisplay);
	
	$(this).triggerHandler('childrenSet', [children]);
	
	this.graph.update();
}