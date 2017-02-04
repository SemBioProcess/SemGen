/**
 * 
 */

ExtractedModel.prototype = new ParentNode();
ExtractedModel.prototype.constructor = ExtractedModel;

function ExtractedModel(graph, srcobj) {
	ParentNode.prototype.constructor.call(this, graph, srcobj, null, 16, 20, 0);
	this.addClassName("ExtractedModel");
	this.modelindex = srcobj.modelindex;
	this.addClassName("modelNode");
	this.canlink = false;
	this.displaymode = DisplayModes.SHOWSUBMODELS.id;
	this.saved = false;
	
	this.addBehavior(Hull);	
	this.addBehavior(parentDrag);	
}

ExtractedModel.prototype.createVisualElement = function (element, graph) {
	ParentNode.prototype.createVisualElement.call(this, element, graph);
}

ExtractedModel.prototype.createVisualization = function (modeid, expand) {
	var modelnode = this;
	
	this.children = {};
	
	if (modeid == 0) {
		this.createChildren();
	}
	//Show physiomap
	else if (modeid == 2) {
		var physionodes = this.srcobj.physionetwork.processes.concat(this.srcobj.physionetwork.entities);
		physionodes.forEach(function (d) {
			modelnode.createChild(d);
		}, this);
		console.log("Showing PhysioMap for model " + this.name);
	}
	//Show all dependencies
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