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
	
	// Get a model node
	var getModelNode = function (modelName) {
		var modelNode = modelNodes[modelName];
		if(!modelNode)
			throw "model doesn't exist";
		
		return modelNode;
	};
	
	// Add child nodes to a model node
	var addChildNodes = function (parentNode, data, createNode) {
		// Create nodes from the data
		var nodes = [];
		data.forEach(function (d) {
			nodes.push(createNode(d));
		});
		
		parentNode.setChildren(nodes);
	};
	
	// Adds a dependency network to the d3 graph
	receiver.onShowDependencyNetwork(function (modelName, dependencyNodeData) {
		console.log("Showing dependencies for model " + modelName);
		
		var modelNode = getModelNode(modelName);
		addChildNodes(modelNode, dependencyNodeData, function (data) {
			return new DependencyNode(graph, data, modelNode);
		});
	});
	
	// Adds a submodel network to the d3 graph
	receiver.onShowSubmodelNetwork(function (modelName, submodelData) {
		console.log("Showing submodels for model " + modelName);
		
		var modelNode = getModelNode(modelName);
		addChildNodes(modelNode, submodelData, function (data) {
			return new SubmodelNode(graph, data, modelNode);
		});
	});
	
	// Show submodel dependency network
	receiver.onShowSubmodelDependencyNetwork(function (modelName, submodelName, submodelDependencyData) {
		console.log("Showing submodel dependency network for model " + submodelName);
		
		// Find the parent model
		var modelNode = getModelNode(modelName);
		if(modelNode.children) {
			// Find the submodel
			for(var index = 0; index < modelNode.children.length; index++) {
				var submodel = modelNode.children[index];
				if(submodel.name == submodelName) {
					// Show the submodel dependency network
					addChildNodes(submodel, submodelDependencyData, function (data) {
						return new DependencyNode(graph, data, submodel);
					});
					return;
				}
			}
		}
		
		// If we didnt find the submodel throw an exception
		throw "Submodel '" + submodelName + "' not found in parent model '" + modelName + "'";
	});
});