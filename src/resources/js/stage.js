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
	
	// Show search results on stage
	receiver.onSearch(function (searchResults) {
		console.log("Showing search results");

		searchResults.sort(function (a, b) {
		    return a.toLowerCase().localeCompare(b.toLowerCase());
		});
		var searchResultsList = document.getElementById("searchResultsList");
		while(searchResultsList.firstChild) {
			searchResultsList.removeChild(searchResultsList.firstChild);
			// Possible memory leak if event handlers aren't removed?
		};
		searchResultsList.appendChild(makeUL(searchResults));
	});
	
	// Add model when clicked
	$('#searchResultsList').on('click', 'li', function() {
		var modelName = $(this).text().trim();
		sender.addModelByName(modelName);
	});
});

// Prevent enter key from submitting form: Once submitted, sender doesn't fire again.
$(document).ready(function() {
	$(window).keydown(function(event){
		if(event.keyCode == 13) {
			event.preventDefault();
			return false;
		}
	});
});

// Add child nodes to a model node
function addChildNodes(parentNode, data, createNode) {
	// Create nodes from the data
	var nodes = [];
	data.forEach(function (d) {
		nodes.push(createNode(d));
	});
	
	parentNode.setChildren(nodes);
};

// Relays string from html form to Java
function search(searchString) {
	sender.search(searchString);
};

function makeUL(array) {
    var list = document.createElement('ul');
    for(var i = 0; i < array.length; i++) {
        var item = document.createElement('li');
        item.appendChild(document.createTextNode(array[i]));
        list.appendChild(item);
    }
    return list;
};