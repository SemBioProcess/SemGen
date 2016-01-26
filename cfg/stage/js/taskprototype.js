/**
 * 
 */

function Task(graph) {

	this.graph = graph;
	this.AllNodes = [];
	this.modelNodes = {};
	this.selectedModels = [];
	this.selectedNodes = [];

	//Get a model node
	this.getModelNode = function(modelName) {
		var modelNode = modelNodes[modelName];
		if(!modelNode)
			throw "model doesn't exist";
		
		return modelNode;
	};
	
	//Add child nodes to a model node
	this.addChildNodes = function(parentNode, data, createNode) {
		// Create nodes from the data
		var nodes = [];
		data.forEach(function (d) {
			nodes.push(createNode(d));
		});
		
		parentNode.setChildren(nodes);
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
		if (node.nodeType=="Model") {
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

