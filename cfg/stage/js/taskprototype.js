/**
 * 
 */

function Task(graph) {
	
	this.graph = graph;
	this.nodes = {};
	this.selectedModels = [];
	this.selectedNodes = [];
	var task = this;
	
	this.loadStageState = function (state) {
		if (state.nodetree.length != 0) {
			state.nodetree.forEach(function(branch) {
				var modelName = branch.branchroot.name;
				var modelNode = new ModelNode(graph, modelName);
				
				task.nodes[modelName] = modelNode;
			});
		};
	}

	
	this.addModelNode = function(modelName) {
		if(this.nodes[modelName])
			throw "Model already exists";
		
		var modelNode = new ModelNode(this.graph, modelName);
		
		this.nodes[modelName] = modelNode;
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

Task.prototype.onModelSelection = function(node) {}
Task.prototype.onClose = function() {}
