/**
 * 
 */

function Task(graph) {
	
	this.graph = graph;
	
	
	this.nodes = {};
	this.selectedModels = [];
	this.selectedNodes = [];
	var modelindex = 0;
	var task = this;
	$("#leftSidebar").empty();
	
	this.addModelNode = function(model, optbehaviors) {
		if(this.nodes[model.name])
			throw "Model already exists";
		
		var modelNode = new ModelNode(this.graph, model, modelindex);
		modelindex++;
		optbehaviors.forEach(function(b){
			modelNode.addBehavior(b);
		});
		
		this.nodes[model.name] = modelNode;
		task.graph.update();

	};
	
	//Get a model node
	this.getModelNode = function(modelName) {
		var modelNode = this.nodes[modelName];
		if(!modelNode)
			throw "model doesn't exist";
		
		return modelNode;
	};

	this.taskClicked = function(element) {
		var taskid = element.innerHTML.toLowerCase();
		sender.taskClicked(this.getFirstSelectedModel().id, taskid, this);
	};
	
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
