
Stage.prototype = new Task();
Stage.prototype.constructor = Stage;
function Stage(graph, stagestate) {
	Task.prototype.constructor.call(this, graph, stagestate);

	var stage = this;
	var nodes = this.nodes;
	this.taskindex = 0;
	
	stage.graph.depBehaviors = [];
	stage.graph.ghostBehaviors = [];
	stage.extractions = {};
	
	$("#addModelButton, .stageSearch").show();
	$("#trash").hide();
	
	this.leftsidebar = new LeftSidebar(graph);
	this.rightsidebar = new RightSidebar(graph);

	var leftsidebar = this.leftsidebar;

	var trash = new StageDoodad(this.graph, "trash", 0.1, 0.9, 2.0, 2.0, "glyphicon glyphicon-scissors");
	this.graph.doodads.push(trash);
	
	var droploc;
	
	// Adds a model node to the d3 graph
	receiver.onAddModel(function (model) {
		console.log("Adding model " + model.name);
		stage.addModelNode(model, [DragToMerge]);
		stage.leftsidebar.addModeltoList(model);
	});

	//Remove the named model node
	receiver.onRemoveModel(function(modelindex) {
		var model = stage.getModelNodebyIndex(modelindex);
		sender.consoleOut("Removing model " + model.name);
		leftsidebar.removeModelfromList(model.id);
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
		event.stopPropagation();
		sender.addModel();
	});

	$("#addModel").click(function() {
		event.stopPropagation();
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
	
	var isExtractionNode = function(node) {
		for (x in extractor.extractions) {
			if (extractor.extractions[x] == node) return true;
		}
		return false;
	}
	
	var isPartofExtraction = function(node) {
		for (x in extractor.extractions) {
			if (extractor.extractions[x] == node.getRootParent()) return true;
		}
		return false;
	}
	
	var promptForExtractionName = function() {
		var name = prompt("Enter name for extraction.", "");
		if (extractor.sourcemodel.name==name) name = promptForExtractionName();
		for (x in extractor.extractions) {
			if (extractor.extractions[x].name==name) return promptForExtractionName();
		}
		
		return name;
	}
	
	//Remove the named model node
	this.removeExtraction = function(extract) {
		sender.consoleOut("Removing extraction " + extract.name);
		var index;
		for (index=0; index< extractor.extractions.length; index++) {
			if (extractor.extractions[index]==extract) break;
		}
		
		extractor.extractions.splice(index, 1);
		delete extractor.nodes[extract.id];
		graph.update();
	};
	
	var onExtractionAction = function(node) {
		//Don't add any extraction actions to the source model node.
		
		if (extractor.sourcemodel == node.srcnode) return;
		node.drag.push(function(selections) {
			if (trash.isOverlappedBy(node, 2.0)) {
				$("#trash").attr("color", "red");
			}
			else {
				$("#trash").attr("color", "transparent");
			}
			
			if (extractor.sourcemodel.hullContainsPoint([node.xpos(), node.ypos()])) {
				extractor.sourcemodel.rootElement.select(".hull").style("stroke","red");
			}
			else {
				extractor.sourcemodel.rootElement.select(".hull").style("stroke", extractor.sourcemodel.nodeType.color);
			}
			
			for (x in extractor.extractions) {
				var extraction = extractor.extractions[x];
				if (extraction.hullContainsPoint([node.xpos(), node.ypos()])) {
					extraction.rootElement.select(".hull").style("stroke","goldenrod");
					break;
				}
				else {
					extraction.rootElement.select(".hull").style("stroke", extraction.nodeType.color);
				}
			}
		});
		
		node.dragEnd.push(function(selections) {
			extractor.graph.shiftIsPressed = false;
			droploc = [node.xpos(), node.ypos()];
			
			//Reset hull colors
			for (x in extractor.extractions) {
				extractor.extractions[x].rootElement.select(".hull").style("fill", extractor.extractions[x].nodeType.color);
			}
			extractor.sourcemodel.rootElement.select(".hull").style("fill", extractor.sourcemodel.nodeType.color);
			
			var extractarray = [];
			for (x in selections) {
				extractarray.push(selections[x].srcnode);
			}
			
			if (extractor.sourcemodel.hullContainsPoint(droploc)) {
				return;
			}

			//Check to see if node is inside an extraction hull
			for (var i=0; i< extractor.extractions.length; i++) {
				if (extractor.extractions[i].hullContainsPoint(droploc)) {
					sender.addNodestoExtraction(i, extractarray);
					return;
				}
			}
			
			//If the node is dragged to the trash
			if (trash.isOverlappedBy(node, 2.0)) {
					droploc= extractor.graph.getCenter();
					var root = node.srcnode.getRootParent();
					if (root==extractor.sourcemodel) {
						//If it's dropped in empty space, create a new extraction
						var name = promptForExtractionName();
							
						//Don't create extraction if user cancels
						if (name==null) return;
						
						if (extractor.sourcemodel.displaymode==DisplayModes.SHOWPHYSIOMAP.id) {
							sender.createPhysioExtractionExclude(extractarray, name);
						}
						else {
							sender.createExtractionExclude(extractarray, name);
						}
					}
					else {
						
						//If an extraction is dragged to the trash, delete it
						if (root==node.srcnode) {
							sender.removeExtraction(root.modelindex);
							extractor.removeExtraction(root);
							return;
						}
						
						if (extractor.sourcemodel.displaymode==DisplayModes.SHOWPHYSIOMAP.id) {
							sender.removePhysioNodesFromExtraction(root.modelindex, extractarray);
						}
						else {
							sender.removeNodesFromExtraction(root.modelindex, extractarray);
						}
					}
					return;
			}
			//If it's dropped in empty space, create a new extraction
			var name = promptForExtractionName();
				
			//Don't create extraction if user cancels
			if (name==null) return;

			if (extractor.sourcemodel.displaymode==DisplayModes.SHOWPHYSIOMAP.id) {
				sender.newPhysioExtraction(extractarray, name);
			}
			else {
				sender.newExtraction(extractarray, name);
			}
		});
	}

	
	this.graph.ghostBehaviors.push(onExtractionAction);
	
	this.addExtractionNode = function(newextraction) {
		var extractionnode = new ExtractedModel(extractor.graph, newextraction);
		extractor.extractions.push(extractionnode);
		extractor.nodes[newextraction.id] = extractionnode;
		if (droploc!=null) {
			extractionnode.setLocation(droploc[0], droploc[1]);
		}
		extractionnode.createVisualization(DisplayModes.SHOWSUBMODELS.id, false);
		extractor.graph.update();
		extractor.selectNode(extractionnode);
	}
	
	this.setExtractionNode = function(index, extraction) {
		var extractionnode = new ExtractedModel(extractor.graph, extraction);
		droploc = [extractor.extractions[index].xpos(), extractor.extractions[index].ypos()];
		extractor.extractions[index] = extractionnode;
		extractor.nodes[extractionnode.id] = extractionnode;
		if (droploc!=null) {
			extractionnode.setLocation(droploc[0], droploc[1]);
		}
		extractionnode.createVisualization(DisplayModes.SHOWSUBMODELS.id, true);
		extractor.graph.update();
		extractor.selectNode(extractionnode);
	}

	receiver.onLoadExtractions(function(extractions) {
		for (x in extractions) {
			extractor.addExtractionNode(extractions[x]);
		}
	});
	
	receiver.onNewExtraction(function(newextraction) {
		extractor.addExtractionNode(newextraction);
	});
	
	receiver.onModifyExtraction(function(index, extraction) {
		extractor.setExtractionNode(index, extraction);
	});
}

Stage.prototype.onInitialize = function() {
	var stage = this;
	
	if (stage.state.models.length > 0) {
		stage.state.models.forEach(function(model) {
			stage.addModelNode(model, [DragToMerge]);
		});		

		sender.requestExtractions();
	}
	
	$('#taskModal').hide();
	this.setSavedState(true);
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
        $(item).data("source", searchResultSet.source);
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