/**
 * Represents a model node in the d3 graph
 */

ModelNode.prototype = new ParentNode();
ModelNode.prototype.constructor = ModelNode;
function ModelNode (graph, name) {
	ParentNode.prototype.constructor.call(this, graph, name, null, null, 16, "#BBB0AF", 20, "Model", 0);
	this.fixed = true;
	
	this.addClassName("modelNode");
	this.x = (Math.random() * (graph.w-graph.w/3))+graph.w/6;
	this.y = (Math.random() * (graph.h-graph.h/2))+graph.h/6;
	this.addBehavior(Hull);
	this.addBehavior(DragToMerge);
}

ModelNode.prototype.onDoubleClick = function (node) {
	sender.consoleOut(node.id + " double clicked")
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



