/**
 * Represents a dependency node in the d3 graph
 */

DependencyNode.prototype = new Node();
DependencyNode.prototype.constructor = DependencyNode;

function DependencyNode (graph, data, parentNode) {
	// Ensure the node type is formatted properly

	Node.prototype.constructor.call(this, graph, data, parentNode, 5, 12, graph.nodecharge);
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

DependencyNode.prototype.getFirstLinkableAncestor = function() {
	var outputNode = this;
	while (!outputNode.isVisible() && outputNode.canlink) {
		outputNode = outputNode.parent;
	}
	if (!outputNode.canlink) outputNode = null;
	return outputNode;
}

DependencyNode.prototype.getLinks = function () {
	
	// Build an array of links from our list of inputs
	var links = [];
	if (!this.graph.nodesVisible[this.nodeType.id]) {
		return links;
	}
	var outputNode = this.getFirstLinkableAncestor();
	if (!outputNode) return links; 
	
	this.srcobj.inputs.forEach(function (link) {
		var inputNode = outputNode.graph.findNode(link.input.id);
		if (!inputNode.graph.nodesVisible[inputNode.nodeType.id]) {
			return;
		}
		inputNode = inputNode.getFirstLinkableAncestor();
		if (!inputNode || inputNode==outputNode) return;
		
		var length = outputNode.graph.linklength;
		if (!link.external) {
			length = Math.round(length/5);
		}
		links.push(new Link(outputNode.graph, link, outputNode, inputNode, length));
	});

	
//		if(type == "external") length = this.graph.linklength;
//		else if(type == "physiomap") length = Math.round(this.graph.linklength/3);


	return links;
}
