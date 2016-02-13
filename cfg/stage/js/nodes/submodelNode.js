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
	
	ParentNode.prototype.constructor.call(this, graph, data.name, parent, inputs, 10, 16, "Submodel", defaultcharge);
	this.dependencies = data.dependencies;
	this.dependencytypecount = data.deptypecounts;

	this.addClassName("submodelNode");
	
	this.addBehavior(Hull);
	this.addBehavior(HiddenLabelNodeGenerator);
}

SubmodelNode.prototype.onDoubleClick = function () {
		node = this;
		
		var visiblenodes = 0;
		if (this.graph.nodesVisible[NodeType.STATE.id]) {
			visiblenodes = this.dependencytypecount[0];
		}
		if (this.graph.nodesVisible[NodeType.RATE.id]) {
			visiblenodes += this.dependencytypecount[1];
		}
		if (this.graph.nodesVisible[NodeType.CONSTITUTIVE.id]) {
			visiblenodes += this.dependencytypecount[2];
		}
		//sender.consoleOut(visiblenodes + " of " + this.dependencies.length);
		if (visiblenodes > 0) {
			// Create dependency nodes from the submodel's dependency data
			this.setChildren(node.dependencies, function (data) {
				return new DependencyNode(node.graph, data, node);
			});
		}
	
}
