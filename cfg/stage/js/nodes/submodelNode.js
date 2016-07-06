/**
 * Sub model nodes
 */
SubmodelNode.prototype = new ParentNode();
SubmodelNode.prototype.constructor = ParentNode;
SubmodelNode.prototype.color = "#CA9485";
function SubmodelNode (graph, data, parent) {
	// Add all dependency node inputs to this node
	// so it references the correct nodes
	var inputs = [];
	data.dependencies.forEach(function (dependency) {
		if(!dependency.inputs)
			return;

		inputs = inputs.concat(dependency.inputs);
	});

	ParentNode.prototype.constructor.call(this, graph, data, parent, 10, 12, "Submodel", graph.nodecharge);
	this.dependencies = data.dependencies;
	this.dependencytypecount = data.deptypecounts;

	this.addClassName("submodelNode");

	this.addBehavior(Hull);
	this.addBehavior(parentDrag);
	this.addBehavior(HiddenLabelNodeGenerator);
}
