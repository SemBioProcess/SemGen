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
	if (this.displaymode == null) this.createChildren();
	this.displaymode = DisplayModes.SHOWMODEL;
    this.submodelVizSize = srcobj.childsubmodels.length;
    this.dependencyVizSize = this.getAllChildNodes().length - this.submodelVizSize;

    this.addBehavior(Hull);
    this.addBehavior(parentDrag);
}

ModelNode.prototype.createVisualElement = function (element, graph) {
	ParentNode.prototype.createVisualElement.call(this, element, graph);
}

ModelNode.prototype.createVisualization = function (modeid, expand) {
	modelnode = this;

    if (modelnode.displaymode == modeid) {
        if (!modelnode.showchildren && expand) {
            modelnode.showChildren();
        }
        return;
    }


	if (modeid == DisplayModes.SHOWSUBMODELS) {
        if (this.submodelVizSize >= 200) {
            var cont = confirm("This Submodel network contains " + this.submodelVizSize + " nodes. Loading the full visualization may take longer.");
            if (!cont) {
                graph.delayfixonupdate = false;
                return;
            }
        }

        for (x in DisplayModes) {
        	$('#' + DisplayModes[x].btnid).removeClass("active");
        }
        $('#' + modeid.btnid).addClass("active");

        this.children = {};
		this.createChildren();
	}
    //Show physiomap
    else if (modeid == DisplayModes.SHOWPHYSIOMAP) {
        var physionodes = this.srcobj.physionetwork.processes.concat(this.srcobj.physionetwork.entities);
        var physiomapVizSize = physionodes.length;
        if (physiomapVizSize >= 200) {
            var cont = confirm("This PhysioMap network contains " + physiomapVizSize + " nodes. Loading the full visualization may take longer.");
            if (!cont) {
                graph.delayfixonupdate = false;
                return;
            }
        }
        for (x in DisplayModes) {
        	$('#' + DisplayModes[x].btnid).removeClass("active");
        }
        $('#' + modeid.btnid).addClass("active");

        this.children = {};

        physionodes.forEach(function (d) {
            modelnode.createChild(d);
        }, this);
        console.log("Showing PhysioMap for model " + this.name);
    }
    //Show all dependencies
    else if (modeid == DisplayModes.SHOWDEPENDENCIES) {
        if (this.dependencyVizSize >= 200) {
            var cont = confirm("The Dependency network contains "+ this.dependencyVizSize + " nodes. Loading the full visualization may take longer.");
            if (!cont) {
                graph.delayfixonupdate = false;
                return;
            }
        }

        this.children = {};
        this.createChildren();
        var dependencies = {};

        this.globalApply(function (node) {
            if (node.nodeType == NodeType.STATE || node.nodeType == NodeType.RATE || node.nodeType == NodeType.CONSTITUTIVE || node.nodeType == NodeType.FORCE) {
                dependencies[node.name] = node;
                node.parent = modelnode;
            }
        });

        for (x in DisplayModes) {
        	$('#' + DisplayModes[x].btnid).removeClass("active");
        }
        $('#' + modeid.btnid).addClass("active");

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
//	if (this.displaymode == DisplayModes.SHOWSUBMODELS) {
//		ParentNode.prototype.showChildren.call(this);
//		return;
//	}
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


