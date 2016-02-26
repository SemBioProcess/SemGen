/**
 * 
 */

function Task(graph) {

	this.graph = graph;
	this.nodes = {};
	this.selectedModels = [];
	this.selectedNodes = [];

	this.addModelNode = function(modelName) {
		if(this.nodes[modelName])
			throw "Model already exists";
		
		var modelNode = new ModelNode(this.graph, modelName);
		
		this.nodes[modelName] = modelNode;
		graph.update();
	};
	
	//Get a model node
	this.getModelNode = function(modelName) {
		var modelNode = this.nodes[modelName];
		if(!modelNode)
			throw "model doesn't exist";
		
		return modelNode;
	};

	this.taskClicked = function(element) {
		var task = element.innerHTML.toLowerCase();
		sender.taskClicked(this.getFirstSelectedModel().id, task);
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

Task.prototype.onModelSelection = function(node) {}
Task.prototype.onMinimize = function() {}
Task.prototype.onClose = function() {}
