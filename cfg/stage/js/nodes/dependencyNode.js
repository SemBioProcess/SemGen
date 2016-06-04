/**
 * Represents a dependency node in the d3 graph
 */

DependencyNode.prototype = new Node();
DependencyNode.prototype.constructor = DependencyNode;

function DependencyNode (graph, data, parentNode) {
	// Ensure the node type is formatted properly
	data.nodeType = data.nodeType.toLowerCase().capitalizeFirstLetter();

	Node.prototype.constructor.call(this, graph, data.name, parentNode, data.inputs, 5, 12, data.nodeType, graph.nodecharge);
	this.submodelid = data.submodelId;
	this.submodelinput = data.issubmodelinput;
	
	this.addClassName("dependencyNode");
	//this.addBehavior(Columns);
	var _node = this;
	graph.depBehaviors.forEach(function(b) {
		_node.addBehavior(b);
	});
	this.addBehavior(HiddenLabelNodeGenerator);
	


}

DependencyNode.prototype.createVisualElement = function (element, graph) {
	Node.prototype.createVisualElement.call(this,element, graph);
	if (this.submodelinput) {
		this.defaultopacity = 0.6;
		this.rootElement.selectAll("circle").attr("opacity", this.defaultopacity);
	}	
}
