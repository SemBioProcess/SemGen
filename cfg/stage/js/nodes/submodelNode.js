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
	
	ParentNode.prototype.constructor.call(this, graph, data.name, parent, inputs, 10, "#CA9485", 16, "Submodel", -300);
	this.dependencies = data.dependencies;
	
	this.addClassName("submodelNode");
	
	this.addBehavior(Hull);
	this.addBehavior(HiddenLabelNodeGenerator);
}

SubmodelNode.prototype.onDoubleClick = function () {
		node = this;
		// Create dependency nodes from the submodel's dependency data
		addChildNodes(this, this.dependencies, function (data) {
			return new DependencyNode(node.graph, data, node);
		});
		
		
}