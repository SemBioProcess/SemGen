/**
 * 
 */

function Task(graph, stagestate) {
	
	this.graph = graph;
	this.state = stagestate;
	
	this.nodes = {};
	this.selectedModels = [];
	this.selectedNodes = [];
	var modelindex = 0;
	var task = this;
	$("#leftSidebar").empty();
	
	this.addModelNode = function(model, optbehaviors) {
		
		var modelNode = new ModelNode(this.graph, model, modelindex);
		modelindex++;
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
	
	this.taskClicked = function(element) {
		var taskid = element.innerHTML.toLowerCase();
		sender.taskClicked(this.getFirstSelectedModel().id, taskid, this);
	};
	
	this.doModelAction = function(action) {
		action(this.getFirstSelectedModel());
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
