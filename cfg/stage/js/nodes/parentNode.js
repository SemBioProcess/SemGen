/**
 * Defines nodes with children
 */
ParentNode.prototype = new Node();
ParentNode.prototype.constructor = ParentNode;

function ParentNode(graph, srcobj, parent, r, group, textSize, nodeType, charge) {
	Node.prototype.constructor.call(this, graph, srcobj, parent, r, group, textSize, nodeType, charge);
	this.children;
	this.showchildren;
	
	this.xmin = null;
	this.xmax = null;
	this.ymin = null;
	this.ymax = null;
	
	//this.createChildren(srcobj.);
}

ParentNode.prototype.createVisualElement = function (element, graph) {

	Node.prototype.createVisualElement.call(this, element, graph);
	this.rootElement.select("circle").style("display", this.children ? "none" : "inherit");
	
	this.lockhull = false; //Set to true to lock the node in the expanded or closed position
	
}

//If children are displayed, keep text above the highest node.
ParentNode.prototype.spaceBetweenTextAndNode = function() {
	var dist = this.r * 0.2 + this.textSize; 
	if (this.children && this.ymin) {
		dist += (this.ypos() - this.ymin)+4; 
	}
	return dist;
}

ParentNode.prototype.canLink = function () {
	return !this.children;
}

ParentNode.prototype.createChild = function(data) {
	var node = this;
	if (data.type=="Submodel") {
		return new SubmodelNode(node.graph, data, node);
	}
	else if (data.type=="Dependency") {
		return new DependencyNode(node.graph, data, node);		
	}
	else if (data.type=="PhysProc") {}
	else if (data.type=="PhysEnt") {}
	
	return null;
}

ParentNode.prototype.createChildren = function(data) {
	this.children = {};
		data.forEach(function (d) {
			var child = createChild(d);
			if (!child) return null;
			child.setLocation(this.xpos() + Math.random(),	this.ypos() + Math.random());
			this.children[d] = child;
		}, this);
}

ParentNode.prototype.showChildren = function () {
	
	 $(this).triggerHandler('childrenSet', [this.children]);
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

ParentNode.prototype.applytoChildren = function(funct) {
	for (var key in this.children) {
		var child = this.children[key];
		child.globalApply(funct);
	}	
}

ParentNode.prototype.globalApply = function(funct) {
	funct(this);
	for (var key in this.children) {
		var child = this.children[key];
		child.globalApply(funct);
	}	
}

ParentNode.prototype.onDoubleClick = function () {
	node = this;
	if (this.srcobj.childsubmodels.length>0) {
		// Create submodel nodes from the model's dependency data
		this.showSubmodelNetwork();
	}
	else {
		this.showDependencyNetwork();
	}
}

ParentNode.prototype.showSubmodelNetwork = function () {
	var node = this;
	console.log("Showing submodels for " + this.name);
	this.graph.displaymode = DisplayModes.SHOWSUBMODELS;
	this.setChildren(this.srcobj.childsubmodels, function (data) {
		return new SubmodelNode(node.graph, data, node);
	});
};

ParentNode.prototype.showDependencyNetwork = function () {
	console.log("Showing dependencies for " + this.name);	
	this.graph.displaymode = DisplayModes.SHOWDEPENDENCIES;
	var node = this;

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
	if (visiblenodes > 0) {
		// Create dependency nodes from the submodel's dependency data
		this.setChildren(node.requestAllChildDependencies(), function (data) {
			return new DependencyNode(node.graph, data, node);
		});
	}

};
