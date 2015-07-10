/**
 * Defines nodes with children
 */
ParentNode.prototype = new Node();
ParentNode.prototype.constructor = ParentNode;
function ParentNode(graph, name, parent, links, r, group, textSize, nodeType, charge) {
	Node.prototype.constructor.call(this, graph, name, parent, links, r, group, textSize, nodeType, charge);
	this.userCanHide = false;
	this.children = null;
}

ParentNode.prototype.createVisualElement = function (element, graph) {
	Node.prototype.createVisualElement.call(this, element, graph);
	
	this.rootElement.select("circle").style("display", this.children ? "none" : "inherit");
}

ParentNode.prototype.canLink = function () {
	return !this.children;
}

ParentNode.prototype.setChildren = function (children) {
	// Remove existing child nodes from the graph
	if(this.children) {
		// Recursively remove all descendant nodes
		var removeChildren = function (childrenArr) {
			childrenArr.forEach(function (child) {
				// If this child has children remove them from the graph as well
				if(child.children)
					removeChildren.call(this, child.children);
				
				this.graph.removeNode(child.id);
			}, this);
		};
		removeChildren.call(this, this.children);
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
		
		// Hide constitutive nodes by default
		this.graph.update();
		
		if(this.graph.hasNodeOfType("State") || this.graph.hasNodeOfType("Rate"))
			this.graph.hideNodes("Constitutive");
	}
	
	$(this).triggerHandler('childrenSet', [children]);
	
	this.graph.update();
}