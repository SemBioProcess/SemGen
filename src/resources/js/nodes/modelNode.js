/**
 * Represents a model node in the d3 graph
 */
ModelNode.prototype = new Node();
ModelNode.prototype.constructor = Node;
function ModelNode (id, flyoutMenu) {
	Node.prototype.constructor.call(this, id, 16);
	this.fixed = true;
	
	this.flyoutMenu = flyoutMenu;
}

ModelNode.prototype.onClick = function (e) {
	this.flyoutMenu.positionAroundElement(e.target);
    e.stopPropagation();
}

ModelNode.prototype.onMouseDown = function (e) {
	this.flyoutMenu.getRoot().hide();
}