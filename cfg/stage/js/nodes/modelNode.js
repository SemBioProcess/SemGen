/**
 * Represents a model node in the d3 graph
 */

ModelNode.prototype = new ParentNode();
ModelNode.prototype.constructor = ModelNode;

function ModelNode (graph, srcobj) {
	ParentNode.prototype.constructor.call(this, graph, srcobj, null, 16, 16, 0);
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
	if (modelnode.displaymode==modeid) return;
	this.children = {};
	for (x in DisplayModes) {
		$('#' + DisplayModes[x].btnid).removeClass("active");
	}
	$('#' + modeid.btnid).addClass("active");
	
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

ModelNode.prototype.getIndexAddress = function() {
	return [-1, this.modelindex];
}

ModelNode.prototype.updateInfo = function() {
	$("#nodemenuUnitRow").hide();
	$("#nodemenuEquationRow").hide();
    $("#nodemenuAnnotationRow").hide();
    $("#nodemenuParticipantsRow").hide();
}


