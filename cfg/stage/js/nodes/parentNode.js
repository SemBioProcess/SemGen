/**
 * Defines nodes with children
 */
ParentNode.prototype = new Node();
ParentNode.prototype.constructor = ParentNode;

function ParentNode(graph, srcobj, parent, r, group, textSize, nodeType, charge) {
	Node.prototype.constructor.call(this, graph, srcobj, parent, r, group, textSize, charge);
	
	this.children = {};
	this.showchildren = false;
	this.xmin = null;
	this.xmax = null;
	this.ymin = null;
	this.ymax = null;
	
	var parent = this;
	
	this.hasChildWithName = function(name) {
		return parent.globalApplyUntilTrue(function(node) {
			return node.name == name;
		});
	}
}

ParentNode.prototype.createChildren = function() {
	
	var node = this;
	node.dependencytypecount = node.srcobj.deptypecounts;

		node.children = {};
		var data = node.srcobj.childsubmodels.concat(node.srcobj.dependencies);
			data.forEach(function (d) {
				node.createChild(d);
			}, this);
	}

ParentNode.prototype.createVisualElement = function (element, graph) {

	Node.prototype.createVisualElement.call(this, element, graph);
	this.rootElement.select("circle").style("display", this.showchildren ? "none" : "inherit");
	
	this.lockhull = false; //Set to true to lock the node in the expanded or closed position
	if (this.showchildren) {
		this.showChildren();
	}
}

//If children are displayed, keep text above the highest node.
ParentNode.prototype.spaceBetweenTextAndNode = function() {
	var dist = this.r * 0.2 + this.textSize; 
	if (this.showchildren && this.ymin) {
		dist += (this.ypos() - this.ymin)+4; 
	}
	return dist;
}

ParentNode.prototype.createChild = function(data) {
	var node = this;
	var child;
	if (data.typeIndex==1) {
		child = new SubmodelNode(node.graph, data, node);
		child.createChildren();
	}
	else if (data.typeIndex==6) {
		child = new PhysioMapNode(node.graph, data, node);
	}
	else {
		child = new DependencyNode(node.graph, data, node);		
	}
	
	if (!child) return;
	
	node.children[data.id] = child;
	
}

//Expand node and show children and hull. Do not expand if there are no visible children.
ParentNode.prototype.showChildren = function () {
	if (this.srcobj.childsubmodels.length == 0) {
		var visiblenodes = 0;
		if (this.graph.nodesVisible[NodeType.STATE.id]) {
			visiblenodes = this.dependencytypecount[0];
		}
		if (this.graph.nodesVisible[NodeType.RATE.id]) {
			visiblenodes += this.dependencytypecount[1];
		}
		if (this.graph.nodesVisible[NodeType.CONSTITUTIVE.id]) {
			visiblenodes += this.dependencytypecount[2];
		}
		if (visiblenodes == 0) return;
	}
	
	this.showchildren = true;
	 $(this).triggerHandler('childrenSet', [this.children]);

}

//Find the child node with the matching id
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

//Apply to all children
ParentNode.prototype.applytoChildren = function(funct) {
	for (var key in this.children) {
		var child = this.children[key];
		child.globalApply(funct);
	}	
}

//Apply to self and children
ParentNode.prototype.globalApply = function(funct) {
	funct(this);
	for (var key in this.children) {
		var child = this.children[key];
		child.globalApply(funct);
	}	
}

//Apply to children until the function returns true
ParentNode.prototype.globalApplyUntilTrue = function(funct) {
	var stop = funct(this);
	for (var key in this.children) {
		if (stop) return true;
		var child = this.children[key];
		stop = child.globalApplyUntilTrue(funct);
	}
	return stop;
}

//Apply function for visible nodes only
ParentNode.prototype.visibleGlobalApply = function(funct) {
	if (!this.isVisible()) return;
	funct(this);
	if (!this.showchildren) return;
	for (var key in this.children) {
		var child = this.children[key];
		child.globalApply(funct);
	}	
}

ParentNode.prototype.onDoubleClick = function () {
	this.showChildren();
	this.graph.update();
}
