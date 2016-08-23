
Stage.prototype = new Task();
Stage.prototype.constructor = Stage;
function Stage(graph, stagestate) {
	Task.prototype.constructor.call(this, graph, stagestate);

	var stage = this;
	var nodes = this.nodes;
	this.taskindex = 0;
	
	$("#addModelButton").show();
	$(".stageSearch").show();
	
	
	this.leftsidebar = new LeftSidebar(graph);
	this.rightsidebar = new RightSidebar(graph);

	var leftsidebar = this.leftsidebar;

	// Adds a model node to the d3 graph
	receiver.onAddModel(function (model) {
		console.log("Adding model " + model.name);
		stage.addModelNode(model, [DragToMerge]);
		
	});

	//Remove the named model node
	receiver.onRemoveModel(function(modelindex) {
		var model = stage.getModelNodebyIndex(modelindex);
		sender.consoleOut("Removing model " + model.name);
		delete nodes[model.id];
		leftsidebar.updateModelPanel(null);
		graph.update();
	});

	// Show search results on stage
	receiver.onSearch(function (searchResults) {
		console.log("Showing search results");

		// Remove all elements from the result list
		var searchResultsList = $(".searchResults");
		searchResultsList.empty();

		// Create UI for the results
		searchResults.forEach(function (searchResultSet ) {
			searchResultSet.results.sort(function (a, b) {
				return a.toLowerCase().localeCompare(b.toLowerCase());
			});

			searchResultsList.append(makeResultSet(searchResultSet));
		});
	});

	receiver.onReceiveReply(function (reply) {
		CallWaiting(reply);
	});

	$("#addModelButton").click(function() {
		sender.addModel();
	});

	$("#addModel").click(function() {
		sender.addModel();
	});

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
}

Stage.prototype.onInitialize = function() {
	var stage = this;

	if (stage.state.models.length > 0) {
		stage.state.models.forEach(function(model) {
			stage.addModelNode(model, [DragToMerge]);
		});
	}
	$('#taskModal').hide();
}

Stage.prototype.onModelSelection = function(node) {
	this.leftsidebar.updateModelPanel(node);
}


function makeResultSet(searchResultSet) {
	var resultSet = $(
		"<li class='searchResultSet'>" +
			"<label>" + searchResultSet.source + "</label>" +
		"</li>"
	);

    var list = document.createElement('ul');
    for(var i = 0; i < searchResultSet.results.length; i++) {
        var item = document.createElement('li');
        item.className = "searchResultSetValue";
        item.appendChild(document.createTextNode(searchResultSet.results[i]));
        list.appendChild(item);
        $(item).data("source", searchResultSet.source)
        $(item).click(function() {
			var modelName = $(this).text().trim();
			var source = $(this).data("source");
			sender.addModelByName(source, modelName);

			// Hide the search box
			$(".stageSearch .searchValueContainer").hide();
		});
    }

    resultSet.append(list);
    return resultSet;
};

Stage.prototype.getTaskType = function() { return StageTasks.PROJECT; }