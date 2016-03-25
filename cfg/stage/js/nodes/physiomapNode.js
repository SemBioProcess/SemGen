/**
 * Represents a physiomap node in the d3 graph
 */

PhysioMapNode.prototype = new Node();
PhysioMapNode.prototype.constructor = PhysioMapNode;

function PhysioMapNode (graph, data, parentNode) {
	// Ensure the node type is formatted properly
	data.nodeType = data.nodeType.toLowerCase().capitalizeFirstLetter();

	Node.prototype.constructor.call(this, graph, data.name, parentNode, data.inputs, 5, 12, data.nodeType, -300);

	if(data.name.includes("Portion of ")) {
		this.displayName = data.name.replace("Portion of ", "").capitalizeFirstLetter();
	}
	if(data.name.includes("Null ")) {
		this.name = "";
		this.displayName = "";
		this.nodeType = NodeTypeMap["Null"];
	}

	this.displayName = limitWords(this.displayName, 3);
	this.addClassName("physiomapNode");
	this.addBehavior(HiddenLabelNodeGenerator);

}

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
