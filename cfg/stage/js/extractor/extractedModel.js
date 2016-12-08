/**
 * 
 */

ExtractedModel.prototype = new ModelNode();
ExtractedModel.prototype.constructor = ExtractedModel;

function ExtractedModel (graph, srcobj) {
	ModelNode.prototype.constructor.call(this, graph, srcobj, null, 16, 20, 0);
	this.addClassName("ExtractedModel");
}

ExtractedModel.prototype.createVisualization = function (modeid, expand) {
	ExtractedModel = this;
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
				node.parent = ExtractedModel;
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

ExtractedModel.prototype.showChildren = function() {
	if (this.mode == 0) {
		ParentNode.prototype.showChildren.call();
		return;
	}
	this.showchildren = true;
	$(this).triggerHandler('childrenSet', [this.children]);
}

ExtractedModel.prototype.multiDrag = function() {
	return main.task.selectedModels;
}