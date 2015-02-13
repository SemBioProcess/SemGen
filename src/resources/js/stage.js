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
		console.log("Adding model " + modelName);
		
		var modelNode = new ModelNode(modelName);
		modelNodes[modelName] = modelNode;
		graph.addNode(modelNode);
		graph.update();
	});
	
	// Adds a dependency network to the d3 graph
	receiver.onShowDependencyNetwork(function (modelName, dependencyNodeData) {
		console.log("Showing dependencies for model " + modelName);
		
		var modelNode = modelNodes[modelName];
		if(!modelNode) {
			alert(modelName + ' does not exist');
			return;
		}

		// Create dependency nodes from the data
		dependencyNodeData.forEach(function (data) {
			var node = new DependencyNode(data);
			graph.addNode(node);
		});
		
		graph.update();
	});
});