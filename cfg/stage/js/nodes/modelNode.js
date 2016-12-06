/**
 * Represents a model node in the d3 graph
 */

ModelNode.prototype = new ParentNode();
ModelNode.prototype.constructor = ModelNode;

function ModelNode (graph, srcobj) {
	ParentNode.prototype.constructor.call(this, graph, srcobj, null, 16, 20, 0);
	this.fixed = true;
	this.modelindex = srcobj.modelindex;
	this.addClassName("modelNode");
	this.canlink = false;
	this.displaymode = DisplayModes.SHOWSUBMODELS.id;

		this.addBehavior(Hull);	
		this.addBehavior(parentDrag);	
}

ModelNode.prototype.createVisualElement = function (element, graph) {
	
	ParentNode.prototype.createVisualElement.call(this, element, graph);

	
}

ModelNode.prototype.createVisualization = function (modeid, expand) {
	modelnode = this;
	this.children = {};
	
	if (modeid == 0) {
		this.createChildren();
	}
	else if (modeid == 2) {
		var physionodes = this.srcobj.physionetwork.processes.concat(this.srcobj.physionetwork.entities);
		physionodes.forEach(function (d) {
			node.createChild(d);
		}, this);
		console.log("Showing PhysioMap for model " + this.name);
	}
	else if (modeid == 1) {
		this.createChildren();
		var dependencies = {};
			
		this.globalApply(function(node){
			if (node.nodeType == NodeType.STATE || node.nodeType == NodeType.RATE || node.nodeType == NodeType.CONSTITUTIVE) {
				dependencies[node.name] = node;
				node.parent = modelnode;
			}
		});
		this.children = dependencies;
	}
	else {
		throw "Display mode not recognized";
		return;
	}
	this.displaymode = modeid;
	this.showchildren = expand;
}

ModelNode.prototype.showChildren = function() {
	if (this.mode == 0) {
		ParentNode.prototype.showChildren.call();
		return;
	}
	this.showchildren = true;
	$(this).triggerHandler('childrenSet', [this.children]);
}

ModelNode.prototype.multiDrag = function() {
	return main.task.selectedModels;
}