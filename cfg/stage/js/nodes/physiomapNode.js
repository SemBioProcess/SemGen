/**
 * Represents a physiomap node in the d3 graph
 */

PhysioMapNode.prototype = new Node();
PhysioMapNode.prototype.constructor = PhysioMapNode;

function PhysioMapNode (graph, data, parentNode) {

	Node.prototype.constructor.call(this, graph, data, parentNode, 5, 12, graph.nodecharge);

	if(data.name.includes("Portion of ")) {
		this.displayName = data.name.replace("Portion of ", "").capitalizeFirstLetter();
	}

	this.displayName = limitWords(this.displayName, 3);
	if (typeof this.description == "undefined") this.description = this.displayName;
	this.addClassName("physiomapNode");
	this.addBehavior(HiddenLabelNodeGenerator);

	this.getInputs = function() {
		var inputs = [];
		this.srcobj.inputs.forEach(function (link) {
			var inputnode = _node.graph.findNode(link.input.id);
			//Return only node types that are visible
			if (inputnode.graph.nodesVisible[inputnode.nodeType.id]) {
				inputs.push(inputnode);
			}
			
		});
		return inputs;
	}
	
}

PhysioMapNode.prototype.getFirstLinkableAncestor = function() {
	var outputNode = this;
	while (!outputNode.isVisible() && outputNode.canlink) {
		outputNode = outputNode.parent;
	}
	if (!outputNode.canlink) outputNode = null;
	return outputNode;
}

PhysioMapNode.prototype.getLinks = function () {
	
	// Build an array of links from our list of inputs
	var links = [];
	if (!this.graph.nodesVisible[this.nodeType.id]) {
		return links;
	}
	var graph = this.graph;
	
	this.srcobj.inputs.forEach(function (link) {
		var outputNode = graph.findNode(link.output.id);
			if (!outputNode) return links; 
			
		var inputNode = graph.findNode(link.input.id);
		if (!inputNode.graph.nodesVisible[inputNode.nodeType.id]) {
			return;
		}

		if (!inputNode || inputNode==outputNode) return;
		
		var length = outputNode.graph.linklength;

		links.push(new Link(outputNode.graph, link, outputNode, inputNode, length));
	});
	
	return links;
}

// Limit displayName to 5 words
var limitWords = function (text, wordLimit) {
	var finalText = "";
	var text2 = text.replace(/\s+/g, ' ');
	var text3 = text2.split(' ');
	var numberOfWords = text3.length;
	var i=0;
	if(numberOfWords > wordLimit) {
		for(i=0; i< wordLimit; i++)
			finalText = finalText+" "+ text3[i]+" ";

		return finalText+"...";
	}
	else return text;
}

PhysioMapNode.prototype.getContextMenu = function() {
	return [{text: 'Select Node Inputs', action : 'selectinputs'}, {text : "Extract Selected", action : 'extract'}, {text: 'Extract Unselected', action : 'extractexclude'}];
}

PhysioMapNode.prototype.updateInfo = function() {
	$("#nodemenuUnitRow").hide();
	$("#nodemenuEquationRow").hide();
	$("#nodemenuAnnotationRow").hide();

	var partstoich = "Sources: \n";
	for (i=0; i < this.srcobj.sourcenames.length; i++) {
		partstoich += this.srcobj.sourcenames[i].replace(": ", " (") + ") \n";
	}
	partstoich += "\nSinks: \n";
	for (i=0; i < this.srcobj.sourcenames.length; i++) {
		partstoich += this.srcobj.sourcenames[i].replace(": ", " (") + ") \n";
	}
	partstoich += "\nMediators: \n";
	for (i=0; i < this.srcobj.sourcenames.length; i++) {
		partstoich += this.srcobj.sourcenames[i].replace(": ", " (") + ") \n";
	}
	
	$("#nodemenuParticipants > pre").text(partstoich);

}