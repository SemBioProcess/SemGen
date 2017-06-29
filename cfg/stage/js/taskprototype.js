/**
 * 
 */

function Task(graph, stagestate) {
	
	this.graph = graph;
	this.state = stagestate;
	
	this.nodes = {};
	this.selectedModels = [];
	this.selectedNodes = [];
	
	var task = this;
	$('#leftSidebar').empty();
	this.rightsidebar = new RightSidebar(graph);
	
	this.addModelNode = function(model, optbehaviors) {
		
		var modelNode = new ModelNode(this.graph, model);
		modelNode.createVisualization(DisplayModes.SHOWSUBMODELS.id, false);
		optbehaviors.forEach(function(b){
			modelNode.addBehavior(b);
		});
		
		task.nodes[model.id] = modelNode;
		task.graph.update();
		task.selectNode(modelNode);
        return modelNode;
	};
	
	//Get a model node
	task.getModelNode = function(model) {
		var modelNode = this.nodes[model];
		if(!modelNode)
			throw "model doesn't exist";
		
		return modelNode;
	};
	
	//Get a model node
	this.getModelNodebyIndex = function(modelindex) {
		var modelNode;
		for (x in this.nodes) {
			if (this.nodes[x].nodeType==NodeType.MODEL && this.nodes[x].modelindex == modelindex) {
				modelNode = this.nodes[x];
				break;
			}
		}
		if(!modelNode)
			throw "model doesn't exist";
		
		return modelNode;
	};
	
	task.isSaved =function() {
		return this.saved;
	}
	
	task.setSaved = function(savestate) {
		this.saved = savestate;
	}
	
	this.taskClicked = function(element) {
		var taskid = element.innerHTML.toLowerCase(),
			selections = [];
		task.selectedModels.forEach(function(model) {
			selections.push(model.getIndexAddress());
		});
		sender.taskClicked(selections, taskid);
	};
	
	task.doModelAction = function(action) {
		task.selectedModels.forEach(function(model) {
			action(model);	
		});
		task.graph.update();
	}
	
	task.getFirstSelectedModel = function () {
		if (this.selectedModels.length > 0) {
			return this.selectedModels[0];
		}
		return null;
	};
	
	this.selectNode = function(node) {
		if (!node.graph.shiftIsPressed) {
			if (node.nodeType==NodeType.MODEL || node.nodeType==NodeType.EXTRACTION) {
				if (!this.graph.cntrlIsPressed) {
					this.selectedModels.forEach(function(selnode) {
						selnode.deselect();
					});
					this.selectedModels = [];
					
				}
				else if (node.selected) {
					for (var i = 0; i<this.selectedModels.length; i++) {
						if (this.selectedModels[i]==node) {
							this.selectedModels[i].deselect();
							this.selectedModels.splice(i, 1);
							return;
						}
					}
				}
				this.selectedModels.push(node);
				this.onModelSelection(node);
			}
			else {
				if (!this.graph.cntrlIsPressed) {
					this.selectedNodes.forEach(function(selnode) {
						selnode.deselect();
					});
					this.selectedNodes = [];
				}
				else {
					for (var i = 0; i<this.selectedNodes.length; i++) {
						if (this.selectedNodes[i]==node && this.selectedNodes.length > 1) {
							this.selectedNodes[i].deselect();
							this.selectedNodes.splice(i, 1);
							return;
						}
					}
				}
				
				this.selectedNodes.push(node);
			}
			node.selected = true;
			node.highlight();

			this.rightsidebar.updateNodeDisplay(node);
		}
	};
	
	this.selectNodeOnDrag = function(node) {
		if (!node.graph.shiftIsPressed && !node.selected) {
			if (node.nodeType==NodeType.MODEL || node.nodeType==NodeType.EXTRACTION) {
				this.selectedModels.forEach(function(selnode) {
						selnode.deselect();
				});
				this.selectedModels = [];
				this.selectedModels.push(node);
			}
			else {
				this.selectedNodes.forEach(function(selnode) {
					selnode.deselect();
				});
				this.selectedNodes = [];
				this.selectedNodes.push(node);
			}
			node.selected = true;
			node.highlight();
		}
		
	}
	
	//On an extraction event from the context menu, make a new extraction
	$('#stage').off('selectinputs').on('selectinputs', function(e, caller) {
		if (!caller.selected) task.selectNode(caller);
		var inputs = caller.getInputs(),
		cnrlstate = task.graph.cntrlIsPressed;
		task.graph.cntrlIsPressed = true;
		for (x in inputs) {
			if (!inputs[x].selected) task.selectNode(inputs[x]);
		}
		graph.cntrlIsPressed = cnrlstate;
	});
	
	this.selectNodes = function(selections) {
		var cntrlstate = task.graph.cntrlIsPressed;
		for (i=0; i <  selections.length; i++) {
			//Make sure all bracketed nodes are selected
			if (i > 0)  task.graph.cntrlIsPressed = true;
			if (!selections[i].selected || i ==0) task.selectNode(selections[i]);
		}
		task.graph.cntrlIsPressed = cntrlstate;
	} 
}

Task.prototype.setSavedState = function(issaved) {
	this.saved = issaved;
	$("#saveButton").prop('disabled', issaved);
}

Task.prototype.onInitialize = function() {}

Task.prototype.onMinimize = function() {}
Task.prototype.onModelSelection = function(node) {}
Task.prototype.onClose = function() {}

Task.prototype.getTaskType = function() {}
