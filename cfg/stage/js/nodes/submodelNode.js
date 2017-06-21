/**
 * Sub model nodes
 */
SubmodelNode.prototype = new ParentNode();
SubmodelNode.prototype.constructor = ParentNode;
SubmodelNode.prototype.color = "#CA9485";
function SubmodelNode (graph, data, parent) {
	// Add all dependency node inputs to this node
	// so it references the correct nodes

	ParentNode.prototype.constructor.call(this, graph, data, parent, 10, 12, graph.nodecharge);
	
	this.addClassName("submodelNode");

	this.addBehavior(Hull);
	this.addBehavior(parentDrag);
	this.addBehavior(HiddenLabelNodeGenerator);
	
	this.getInputs = function() {
		var inputs = [];
		for (x in this.children) {
			inputs = inputs.concat(this.children[x].getInputs());
		}

		return inputs;
	}
}

SubmodelNode.prototype.getContextMenu = function() {
	return [{text : "Extract Selected", action : "extract"}, {text : "Extract Unselected", action : "extractexclude"}];
	
}

SubmodelNode.prototype.updateInfo = function() {
	$("#nodemenuUnitRow").hide();
	$("#nodemenuEquationRow").hide();
	$("#nodemenuParticipantsRow").hide();
	$("#nodemenuAnnotationRow").hide();
}