/**
 * Defines nodes with children
 */
ParentNode.prototype = new Node();
ParentNode.prototype.constructor = ParentNode;

function ParentNode(graph, name, parent, links, r, group, textSize, nodeType, charge) {
	Node.prototype.constructor.call(this, graph, name, parent, links, r, group, textSize, nodeType, charge);
	this.children = null;
	
	this.xmin = null;
	this.xmax = null;
	this.ymin = null;
	this.ymax = null;
}

ParentNode.prototype.createVisualElement = function (element, graph) {

	Node.prototype.createVisualElement.call(this, element, graph);
	this.rootElement.select("circle").style("display", this.children ? "none" : "inherit");
	this.addBehavior(parentDrag);
	
}

//If children are displayed, keep text above the highest node.
ParentNode.prototype.spaceBetweenTextAndNode = function() {
	var dist = this.r * 0.2 + this.textSize; 
	if (this.children && this.ymin) {
		dist += (this.y - this.ymin)+4; 
	}
	return dist;
}

ParentNode.prototype.canLink = function () {
	return !this.children;
}

ParentNode.prototype.setChildren = function (data, createNode) {
	this.children = null;
	 if (data) {
		this.children = {};
		data.forEach(function (d) {
			var child = createNode(d);
			child.x = this.x + Math.random();
			child.y = this.y + Math.random();
			this.children[d.name] = child
		}, this);
		$(this).triggerHandler('childrenSet', [this.children]);
	}
	this.graph.update();
	
}

ParentNode.prototype.getChildNode = function(id) {
	var node = this.children[id];
	if (node) {
		return node;
	}
	for (var key in this.children) {
		if (this.children[key].children) {
			node = children.getChildNode(id);
			if (node) return node;
		}
	}
	return null;
}

ParentNode.prototype.getAllChildNodes = function() {
	if (!this.children) return null;
	var immediate = getSymbolArray(this.children);
	var childnodes = [];
	immediate.forEach(function(child) {
		child.globalApply(function(d){
			immediate.push(d);
		});
	});
	return childnodes;
}

ParentNode.prototype.globalApply = function(funct) {
	funct(this);
	for (var key in this.children) {
		var child = this.children[key];
		child.globalApply(funct);
	}	
}
