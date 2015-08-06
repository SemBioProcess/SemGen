/**
 * Represents a physiomap node in the d3 graph
 */

PhysioMapNode.prototype = new Node();
PhysioMapNode.prototype.constructor = PhysioMapNode;
function PhysioMapNode (graph, data, parentNode) {
	// Ensure the node type is formatted properly
	data.nodeType = data.nodeType.toLowerCase().capitalizeFirstLetter();

	// Get the correct group from the node type
	this.group = physiomapTypeToGroup[data.nodeType];
	if(this.group == "undefined")
		throw "Invalid PhysioMap node type: " + data.nodeType;

	Node.prototype.constructor.call(this, graph, data.id, data.name, parentNode, data.inputs, 5, physiomapTypeToColor[data.nodeType], 14, data.nodeType, -300);
	
	this.displayName = data.name.replace("Portion of ", "").capitalizeFirstLetter();
	this.displayName = limitWords(this.displayName, 5);
	this.addClassName("physiomapNode");
	this.addBehavior(HiddenLabelNodeGenerator);

}

// Maps node type to group number
var physiomapTypeToGroup = {
	"Entity": 0,
	"Mediator": 1,
};

// Maps node type to node color
var physiomapTypeToColor = {
	"Entity": "#1F77B4",
	"Mediator": "#FF7F0E"
};

// Limit displayName to 5 words
var limitWords = function (text, wordLimit) {
	var finalText = "";
	var text2 = text.replace(/\s+/g, ' ');
	var text3 = text2.split(' ');
	var numberOfWords = text3.length;
	var i=0;
	if(numberOfWords > wordLimit)
	{
		for(i=0; i< wordLimit; i++)
			finalText = finalText+" "+ text3[i]+" ";

		return finalText+"...";
	}
	else return text;
}
