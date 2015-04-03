/**
 * Generates hidden label nodes based on text label length
 * to prevent labels from touching.
 * 
 * Ex:
 * 		Given a label:
 * 			LabelLabel
 * 		This class will generate a hidden node on each side of the label, like:
 * 		   oLabelLabelo
 * 
 * 		This will prevent other label from overlapping this label
 */
function HiddenLabelNodeGenerator(anchorNode) {
	if(anchorNode.name.length < 5)
		return;
	
	// Create hidden label nodes based on text length and charge
	// If the text is really long we want more hidden nodes so they're not too
	// spread apart
	var numHiddenNodesToCreate = 2 + Math.floor((anchorNode.name.length - 5) / 15);
	var hiddenLabelNodes = [];
	var hiddenNodeCharge = anchorNode.charge / numHiddenNodesToCreate;
	for(var nodeNum = 0; nodeNum < numHiddenNodesToCreate; nodeNum++) {
		var percentageFromLeftOfLabel = nodeNum / (numHiddenNodesToCreate - 1)
		hiddenLabelNodes.push(new HiddenLabelNode(anchorNode, percentageFromLeftOfLabel, hiddenNodeCharge))
	}
	
	// Add hidden nodes when the anchor node is added to the graph
	$(anchorNode.graph).on("nodeAdded", function (e, node) {
		if(anchorNode != node)
			return;
		
		hiddenLabelNodes.forEach(function (hiddenLabelNode) {
			anchorNode.graph.addNode(hiddenLabelNode);
		});
	});
	
	// Remove hidden nodes when the anchor node is removed from the graph
	$(anchorNode.graph).on("nodeRemoved", function (e, node) {
		if(anchorNode != node)
			return;
		
		hiddenLabelNodes.forEach(function (hiddenLabelNode) {
			anchorNode.graph.removeNode(hiddenLabelNode.id);
		});
	});
}