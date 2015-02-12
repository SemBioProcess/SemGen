var sender;
var receiver;
$(window).bind("cwb-initialized", function(e) {
	receiver = e.originalEvent.commandReceiver;
	sender = e.originalEvent.commandSender;

	var graph = new Graph();
	var modelNodes = {};
	
	$(".addModelButton").click(function() {
		sender.addModel();
	});
	
	// Adds a model node to the d3 graph
	receiver.onAddModel(function (modelName) {
		var modelNode = new ModelNode(modelName);
		modelNodes[modelName] = modelNode;
		graph.addNode(modelNode);
	});
});