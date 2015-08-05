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
		
		if(modelNodes[modelName])
			throw "Model already exists";
		
		var modelNode = new ModelNode(graph, modelName);
		modelNodes[modelName] = modelNode;
		graph.addNode(modelNode);
		graph.update();
	});
	
	//Remove the named model node
	receiver.onRemoveModel(function(modelName) {
		console.log("Removing model " + modelName);
		graph.removeNode(modelName);
		delete modelNodes[modelName];
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
		};
		searchResultsList.appendChild(makeUL(searchResults));
	});
	
	// Add model when clicked
	$('#searchResultsList').on('click', 'li', function() {
		var modelName = $(this).text().trim();
		sender.addModelByName(modelName);
		
		// Hide the search box
		$(".stageSearch .searchValueContainer").hide();
	});
});

$(window).load(function() {
	// When you mouseover the search element show the search box and results
	$(".stageSearch").mouseover(function (){
		$(".stageSearch .searchValueContainer").css('display', 'inline-block');
	});
	
	// When you mouseout of the search element hide the search box and results
	$(".stageSearch").mouseout(function (){
		$(".stageSearch .searchValueContainer").hide();
	});
	
	$(".searchString").keyup(function() {
		if( $(this).val() ) {
			$(".stageSearch .searchValueContainer .searchResults").show()
			sender.search($( this ).val());
		}
		else {
			$(".stageSearch .searchValueContainer .searchResults").hide()
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

function makeUL(array) {
    var list = document.createElement('ul');
    for(var i = 0; i < array.length; i++) {
        var item = document.createElement('li');
        item.appendChild(document.createTextNode(array[i]));
        list.appendChild(item);
    }
    return list;
};