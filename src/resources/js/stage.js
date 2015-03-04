var sender;
var receiver;
$(window).bind("cwb-initialized", function(e) {
	receiver = e.originalEvent.commandReceiver;
	sender = e.originalEvent.commandSender;

	var graph = new Graph();
	var modelNodes = {};
	
	SelectionManager.getInstance().initialize(graph);
	KeyElement.getInstance().initialize(graph);
	
	$(".addModelButton").click(function() {
		sender.addModel();
	});
	
	// Adds a model node to the d3 graph
	receiver.onAddModel(function (modelName) {
		console.log("Adding model " + modelName);
		
		var modelNode = new ModelNode(graph, modelName);
		modelNodes[modelName] = modelNode;
		graph.addNode(modelNode);
		graph.update();
	});
	
	// Add child nodes to a model node
	var addChildNodes = function (modelName, data, createNode) {
		var modelNode = modelNodes[modelName];
		if(!modelNode)
			throw "model doesn't exist";

		// Create nodes from the data
		var nodes = [];
		data.forEach(function (d) {
			nodes.push(createNode(d, modelNode));
		});
		
		modelNode.setChildren(nodes);
	};
	
	// Adds a dependency network to the d3 graph
	receiver.onShowDependencyNetwork(function (modelName, dependencyNodeData) {
		console.log("Showing dependencies for model " + modelName);
		
		addChildNodes(modelName, dependencyNodeData, function (data, parent) {
			return new DependencyNode(graph, data, parent);
		});
	});
	
	// Adds a submodel network to the d3 graph
	receiver.onShowSubmodelNetwork(function (modelName, submodelData) {
		console.log("Showing submodels for model " + modelName);
		
		addChildNodes(modelName, submodelData, function (data) {
			return new SubmodelNode(graph, data, parent);
		});
	});
});