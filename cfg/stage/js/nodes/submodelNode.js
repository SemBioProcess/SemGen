/**
 * Sub model nodes
 */
SubmodelNode.prototype = new ParentNode();
SubmodelNode.prototype.constructor = ParentNode;
function SubmodelNode (graph, data, parent) {
	// Add all dependency node inputs to this node
	// so it references the correct nodes
	var inputs = [];
	data.dependencies.forEach(function (dependency) {
		if(!dependency.inputs)
			return;
		
		inputs = inputs.concat(dependency.inputs);
	});
	
	ParentNode.prototype.constructor.call(this, graph, data.name, parent, inputs, 10, "#CA9485", 16, "Submodel", -1000);
	this.dependencies = data.dependencies;
	
	this.addClassName("submodelNode");
	
	this.addBehavior(Hull);
	this.addBehavior(HiddenLabelNodeGenerator);
}

SubmodelNode.prototype.createVisualElement = function (element, graph) {
	ParentNode.prototype.createVisualElement.call(this, element, graph);
	
	// When the submodel is clicked created dependency nodes from it's dependency data
	this.rootElement.select("circle").on("dblclick", function (node) {
		// Create dependency nodes from the submodel's dependency data
		addChildNodes(node, node.dependencies, function (data) {
			return new DependencyNode(node.graph, data, node);
		});
		
		d3.event.stopPropagation();
	});
}