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
	$("#leftSidebar").empty();
	
	this.addModelNode = function(model, optbehaviors) {
		
		var modelNode = new ModelNode(this.graph, model);
		modelNode.createVisualization(DisplayModes.SHOWSUBMODELS.id, false);
		optbehaviors.forEach(function(b){
			modelNode.addBehavior(b);
		});
		
		this.nodes[model.id] = modelNode;
		task.graph.update();

	};
	
	//Get a model node
	this.getModelNode = function(model) {
		var modelNode = this.nodes[model];
		if(!modelNode)
			throw "model doesn't exist";
		
		return modelNode;
	};
	
	//Get a model node
	this.getModelNodebyIndex = function(modelindex) {
		var modelNode;
		for (x in this.nodes) {
			if (this.nodes[x].index == modelindex) {
				modelNode = this.nodes[x];
				break;
			}
		}
		if(!modelNode)
			throw "model doesn't exist";
		
		return modelNode;
	};
	
	this.taskClicked = function(element) {
		var taskid = element.innerHTML.toLowerCase();
		sender.taskClicked(parseInt(this.getFirstSelectedModel().index), taskid);
	};
	
	this.doModelAction = function(action) {
		action(this.getFirstSelectedModel());
		task.graph.update();
	}
	
	this.getFirstSelectedModel = function () {
		if (this.selectedModels.length > 0) {
			return this.selectedModels[0];
		}
		return null;
	};
	
	this.selectNode = function(node) {
		if (node.nodeType==NodeType.MODEL) {
			this.selectedModels.forEach(function(selnode) {
				//if (selnode == node) { return; }
				selnode.removeHighlight();
				
			});
			
			this.selectedModels = [];
			this.selectedModels.push(node);
			
			this.onModelSelection(node);
		}
		else {
			this.selectedNodes.forEach(function(selnode) {
				//if (selnode == node) { return; }
				selnode.removeHighlight();
			});
			this.selectedNodes = [];
			this.selectedNodes.push(node);
		}
		
		node.highlight();
	};
}

Task.prototype.onInitialize = function() {}

Task.prototype.onMinimize = function() {}
Task.prototype.onModelSelection = function(node) {}
Task.prototype.onClose = function() {}

Task.prototype.getTaskType = function() {}
