/**
 * Represents a model node in the d3 graph
 */

ModelNode.prototype = new ParentNode();
ModelNode.prototype.constructor = ModelNode;

function ModelNode (graph, name, index) {
	ParentNode.prototype.constructor.call(this, graph, name, null, null, 16, 20, "Model", 0);
	this.fixed = true;
	this.index = index;
	this.addClassName("modelNode");
	this.x = (Math.random() * (graph.w-graph.w/3))+graph.w/6;
	this.y = (Math.random() * (graph.h-graph.h/2))+graph.h/6;
	this.addBehavior(Hull);	
	
	this.addBehavior(parentDrag);
	
}

ModelNode.prototype.createVisualElement = function (element, graph) {
	
	ParentNode.prototype.createVisualElement.call(this, element, graph);
	
}

ModelNode.prototype.onDoubleClick = function () {
	node = this;
	CallWaiting = function(hassubmodels) {
		if (hassubmodels=="true") {
			// Create submodel nodes from the model's dependency data
			sender.taskClicked(node.id, "submodels");
		}
		else {
			sender.taskClicked(node.id, "dependencies");
		}

	}
	sender.queryModel(node.name, "hassubmodels");
}
