/**
 * 
 */

ExtractedModel.prototype = new ParentNode();
ExtractedModel.prototype.constructor = ExtractedModel;

function ExtractedModel(graph, srcobj, basenode) {
	ParentNode.prototype.constructor.call(this, graph, srcobj, null, 16, 20, 0);
	this.addClassName("ExtractedModel");
	this.modelindex = srcobj.modelindex;
	this.sourcenode = basenode;
	this.addClassName("modelNode");
	this.canlink = false;
	this.displaymode = DisplayModes.SHOWSUBMODELS
	this.saved = false;
	this.showchildren = true;
	this.addBehavior(Hull);	
	this.addBehavior(parentDrag);	
}

ExtractedModel.prototype.createVisualElement = function (element, graph) {
	ParentNode.prototype.createVisualElement.call(this, element, graph);
}

ExtractedModel.prototype.createVisualization = function (modeid, expand) {
	var modelnode = this;
	
	this.children = {};
	
	if (modeid == DisplayModes.SHOWSUBMODELS) {
		this.createChildren();
	}
	//Show physiomap
	else if (modeid == DisplayModes.SHOWPHYSIOMAP) {
		var physionodes = this.srcobj.physionetwork.processes.concat(this.srcobj.physionetwork.entities);
		physionodes.forEach(function (d) {
			modelnode.createChild(d);
		}, this);
		console.log("Showing PhysioMap for model " + this.name);
	}
	//Show all dependencies
	else if (modeid == DisplayModes.SHOWDEPENDENCIES) {
		this.createChildren();
		var dependencies = {};
			
		this.globalApply(function(node){
			if (node.nodeType == NodeType.STATE || node.nodeType == NodeType.RATE || node.nodeType == NodeType.CONSTITUTIVE || node.nodeType == NodeType.FORCE) {
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
	if (this.displaymode == DisplayModes.SHOWSUBMODELS) {
		ParentNode.prototype.showChildren.call(this);
		return;
	}
	this.showchildren = true;
	$(this).triggerHandler('childrenSet', [this.children]);
}

ExtractedModel.prototype.multiDrag = function() {
	return main.task.selectedModels;
}

ExtractedModel.prototype.getIndexAddress = function() {
	return [this.sourcenode.modelindex, this.modelindex];
}