
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
    $(".searchDropDownMenu input:checkbox").prop('disabled', false);

    $('[data-toggle="tooltip"]').tooltip({delay: {show: 1000, hide: 50}});

    this.leftsidebar = new LeftSidebar(graph);


	var leftsidebar = this.leftsidebar;

	var droploc;
	
	//var trash = new StageDoodad(this.graph, "trash", 0.05, 0.9, 2.0, 2.0, "glyphicon glyphicon-scissors", "");
	//this.graph.doodads.push(trash);
	
	$( "#stage" ).removeClass( "taskmode" );

	document.addEventListener("click", function(e) {
		$('contextmenu').hide();
	});
	
	// Adds a model node to the d3 graph
	receiver.onAddModel(function (model) {
		console.log("Adding model " + model.name);
		var modelnode = stage.addModelNode(model, [DragToMerge]);

        if (modelnode.getAllChildNodes().length > 100) {
            window.alert("This model contains over 100 elements. Visualizations may take longer to load.");
        }

        stage.extractions[modelnode.modelindex] = {modextractions: []};
		stage.leftsidebar.addModeltoList(model);
	});

	//Remove the named model node
	receiver.onRemoveModel(function(modelindex) {
		if (modelindex[0] == -1) {
			var model = stage.getModelNodebyIndex(modelindex[1]);
			sender.consoleOut("Removing model " + model.name);
			leftsidebar.removeModelfromList(model.id);
			delete nodes[model.id];
			delete stage.extractions[model];
			
			graph.update();
		}
		else {
			stage.removeExtraction(modelindex[0], modelindex[1]);
		}
		leftsidebar.updateModelPanel(null);
        $("#rightSidebarModelName").text("");
    });

	// Show search results on stage
	receiver.onSearch(function (searchResults) {

		// Remove all elements from the result list
		var searchResultsList = $(".searchResults");

		// Create UI for the results
		searchResults.forEach(function (searchResultSet) {
			searchResultSet.results.sort(function (a, b) {
				return a.toLowerCase().localeCompare(b.toLowerCase());
			});

			searchResultsList.append(makeResultSet(searchResultSet, stage));
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

    var timer;
    $(".stageSearch").on("mouseover", function() {
        clearTimeout(timer);
        openSearch();
    }).on("mouseleave", function() {
        timer = setTimeout(
            closeSearch
            , 500);
    });

	// When you mouseover the search element show the search box and results
	function openSearch() {
		$(".stageSearch .searchValueContainer").css('display', 'inline-block');
    }

    // When you mouseout of the search element hide the search box and results
	function closeSearch() {
		$(".stageSearch .searchValueContainer").hide();
    }

	// Filter stuff for searching
    $(".dropdown-toggle").click(function(e) {
        e.preventDefault();
        e.stopPropagation();
        $(".searchDropDownMenu").toggle(300);
    });

    $("#checkAll").click(function () {
        $(".searchDropDownMenu input:checkbox").not(this).prop('checked', this.checked);
        if($(".searchString".val())) {
            stage.stageSearch($(".searchString").val());
        }
    });
    $(".searchDropDownMenu input:checkbox:not(#checkAll)").change(function () {
        if ($(".searchDropDownMenu input:checkbox:not(#checkAll):checked").length ==
			$(".searchDropDownMenu input:checkbox:not(#checkAll)").length) {
        	$("#checkAll").prop('checked', true);
        }
        else $("#checkAll").prop('checked', false);
	});

    $(".searchDropDownMenu input:checkbox").change(function () {
        if($(".searchString".val())) {
            stage.stageSearch($(".searchString").val());
        }
	});

	$(".searchString").keyup(function() {
        var searchString = $(this).val();
        if (searchString && event.keyCode === 13) {
        	stage.showLoader();
            $(".searchResults").empty();
            $(".stageSearch .searchValueContainer .searchResults").show();

            setTimeout(function() {
                stage.stageSearch(searchString);
                stage.hideLoader();
            }, 20);
        }
        else {
            $(".stageSearch .searchValueContainer .searchResults").hide();
        }
    });

	this.showLoader = function() {
        $(".searchIcon").hide();
        $(".loader").show();
	};

	this.hideLoader = function() {
        $(".searchIcon").show();
        $(".loader").hide();
	};

    this.stageSearch = function(searchString) {
        console.log("Showing search results");

        // Check search filter
        var modelSearchChecked = $("#checkModel").is(':checked');
        var bioModelSearchChecked = $("#checkBioModels").is(':checked');
        var nodeNameSearchChecked = $("#checkNodeName").is(':checked');
        var nodeDescSearchChecked = $("#checkNodeDesc").is(':checked');

        if (modelSearchChecked) {
            sender.search(searchString);
        }

        if (bioModelSearchChecked) {
			sender.bioModelsSearch(searchString);
        }

        // Search for nodes in Project Tab
        if (nodeNameSearchChecked) {
            stage.nodeSearch(graph.getVisibleNodes(), searchString, "label");
        }
        if (nodeDescSearchChecked) {
            stage.nodeSearch(graph.getVisibleNodes(), searchString, "description");
        }
    }

	this.nodeSearch = function(visibleNodes, searchString, filter) {
        var nodeSearchResultSet = {};
        var nodeSearchResults = [];
        var searchResultsList = $(".searchResults");

        for (i in visibleNodes) {
            var node = visibleNodes[i];
            var nodeLabel = node.displayName.toLowerCase();
            var nodeDescription = node.description.toLowerCase();
            var parentModel = node.id.split(".")[0];
            var querryArray = searchString.toLowerCase().split(" ");

            if (querryArray.every(function(keyword) {
                    var foundInName = false;
                    var foundInDescription = false;
                    if(nodeLabel.includes(keyword) && filter == "label") foundInName = true;
                    if(nodeDescription.includes(keyword) && filter == "description") foundInDescription = true;

                    return (foundInName || foundInDescription);
                })) {

                nodeSearchResults.push([node.name, parentModel, node.id]);
            }
        }

        nodeSearchResultSet["source"] = "Nodes in Project " + " - " + filter;
        nodeSearchResultSet["results"] = nodeSearchResults;

        nodeSearchResultSet.results.sort(function (a, b) {
            return a[0].toLowerCase().localeCompare(b[0].toLowerCase());
        });

		if (nodeSearchResultSet.results.length > 0) {
            searchResultsList.append(makeResultSet(nodeSearchResultSet, stage));
        }
    };
	
	$("#saveModel").click(function() {
		var modelstosave = [], count = 0;
		for (i in stage.selectedModels) {
				var model = stage.selectedModels[i];
			
				if (!model.saved && model.selected) {
					modelstosave.push(model.getIndexAddress());
					count++;
				}
		}
		if (count==0) return;
		sender.save(extractstosave);
	});
	
	//******************EXTRACTION FUNCTIONS*************************//
	
	var promptForExtractionName = function() {
		var name = prompt("Enter name for extraction.", "");
		for (x in stage.nodes) {
			if (x==name) return promptForExtractionName();
		}
		
		return name;
	}
	
	//Remove the extraction node
	this.removeExtraction = function(modelindex, extract) {
		var extraction = stage.extractions[modelindex].modextractions[extract];
		sender.consoleOut("Removing extraction " + extraction.name);
		
		stage.extractions[modelindex].modextractions[extract] = null;
		delete stage.nodes[extraction.id];
		graph.update();
	};
	
	var onExtractionAction = function(node) {
		//Don't add any extraction actions to a model node.
		
		if (node.nodeType == NodeType.MODEL) return;
		node.drag.push(function(selections) {

			for (i in stage.nodes) {
				var ithnode = stage.nodes[i];
				if (ithnode.hullContainsPoint([node.xpos(), node.ypos()])) {
					if ( ithnode.nodeType==NodeType.MODEL || ithnode != node.srcnode.getRootParent() ) {
						ithnode.rootElement.select(".hull").style("stroke","red");
					}
					else {
						ithnode.rootElement.select(".hull").style("stroke","goldenrod");
					}
				}
				else {
					ithnode.rootElement.select(".hull").style("stroke", ithnode.nodeType.color);
				}
			}

		});
		
		node.dragEnd.push(function(selections) {
			stage.graph.shiftIsPressed = false;
			droploc = [node.xpos(), node.ypos()];

			//Reset hull colors
			for (x in stage.nodes) {
				stage.nodes[x].rootElement.select(".hull").style("stroke", stage.nodes[x].nodeType.color);
			}
			var root = node.srcnode.getRootParent();
			
			// Ensure all selected nodes share the same parent as the first selected node
			var extractarray = [];
			for (x in selections) {
				var selnode = selections[x].srcnode;
				if (selnode.nodeType == NodeType.MODEL && root!=selnode.getRootParent()) continue;
				extractarray.push(selnode);
			}
			
			var destinationnode = null;
			for (i in stage.nodes) {
				var ithnode = stage.nodes[i];
				
				if (ithnode.hullContainsPoint([node.xpos(), node.ypos()])) {
					//if a model hull contains the dropped ghost node, do nothing.
					if ( ithnode.nodeType==NodeType.MODEL ) {
						return;
					}
					else {
						destinationnode = ithnode;
						break;
					}
				}
			}
			if (destinationnode!=root && destinationnode!=null) {
				return;
			}
			//Check to see if node is inside an extraction hull
			if (destinationnode!=null) {
				sender.addNodestoExtraction(destinationnode.sourcenode.modelindex, destinationnode.modelindex, extractarray);
				return;
			}
			stage.createExtraction(extractarray,root);

		});
	}
	
	this.createExtraction = function(extractarray, root) {
		//If it's dropped in empty space, create a new extraction
		var name = promptForExtractionName();
			
		//Don't create extraction if user cancels
		if (name==null) return;
		
		var baserootindex = root.modelindex;
		if (root.displaymode==DisplayModes.SHOWPHYSIOMAP) {
			sender.newPhysioExtraction(baserootindex, extractarray, name);
		}
		else {
			sender.newExtraction(baserootindex, extractarray, name);
		}
	}

	this.createExtractionandExclude = function(extractarray, root) {
		//If it's dropped in empty space, create a new extraction
		var name = promptForExtractionName();
			
		//Don't create extraction if user cancels
		if (name==null) return;
		
		if (root.displaymode==DisplayModes.SHOWPHYSIOMAP) {
			sender.createPhysioExtractionExclude(root.modelindex, extractarray, name);
		}
		else {
			sender.createExtractionExclude(root.modelindex, extractarray, name);
		}
	}
	
	this.applytoExtractions = function(dothis) {
		for (x in stage.extractions) {
			for (y in stage.extractions[x]) {
				dothis(stage.extractions[x][y]);
			}
		}
	}
	
	//Apply to children until the function returns true
	this.applytoExtractionsUntilTrue = function(funct) {
		for (x in stage.extractions) {
			for (y in stage.extractions[x]) {
				if (dothis(stage.extractions[x][y])) return true;
			}
		}
		return false;
	}
	
	this.graph.ghostBehaviors.push(onExtractionAction);
	
	//On an extraction event from the context menu, make a new extraction
	$('#stage').off('extract').on('extract', function(e, caller) {
		if (!caller.selected) stage.selectNode(caller);
		var root = caller.getRootParent();
		stage.createExtraction(stage.selectedNodes,root);
	});
	
	$('#stage').off('extractexclude').on('extractexclude', function(e, caller) {
		if (!caller.selected) stage.selectNode(caller);
		var root = caller.getRootParent();
		stage.createExtractionandExclude(stage.selectedNodes,root);
	});
	
	$('#stage').off('removeselected').on('removeselected', function(e, caller) {
		var root = caller.getRootParent();
		var srcmodindex = root.modelindex;
		
		var extractarray = [];
		for (x in stage.selectedNodes) {
			var selnode = stage.selectedNodes[x];
			if (selnode.nodeType == NodeType.MODEL && root!=selnode.getRootParent()) continue;
			extractarray.push(selnode);
		}
		if (root.displaymode==DisplayModes.SHOWPHYSIOMAP) {
			sender.removePhysioNodesFromExtraction(srcmodindex, root.modelindex, extractarray);
		}
		else {
			sender.removeNodesFromExtraction(srcmodindex, root.modelindex, extractarray);
		}
	});
	
	this.addExtractionNode = function(basenodeindex, newextraction) {
		var basenode = stage.getModelNodebyIndex(basenodeindex);
		var extractionnode = new ExtractedModel(stage.graph, newextraction, basenode);
		extractionnode.addBehavior(DragToMerge);
		stage.extractions[basenodeindex].modextractions.push(extractionnode);
		stage.nodes[newextraction.id] = extractionnode;
		if (droploc!=null) {
			extractionnode.setLocation(droploc[0], droploc[1]);
		}
		extractionnode.createVisualization(basenode.displaymode, false);
        stage.graph.delayfixonupdate = true;
		stage.graph.update();
		stage.selectNode(extractionnode);
	}
	
	this.setExtractionNode = function(basemodelindex, index, extraction) {
		var extractionnode = new ExtractedModel(stage.graph, extraction);
		droploc = [stage.extractions[basemodelindex].modextractions[index].xpos(), stage.extractions[basemodelindex].modextractions[index].ypos()];
		stage.extractions[basemodelindex].modextractions[index] = extractionnode;
		stage.nodes[extractionnode.id] = extractionnode;
		if (droploc!=null) {
			extractionnode.setLocation(droploc[0], droploc[1]);
		}
		extractionnode.createVisualization(DisplayModes.SHOWSUBMODELS, true);
		stage.graph.delayfixonupdate = true;
        stage.graph.update();
		stage.selectNode(extractionnode);
	}
	

	receiver.onLoadExtractions(function(extractions) {
		for (x in extractions) {
			for (y in extractions[x]) {
				stage.addExtractionNode(x, extractions[x][y]);
			}
		}
	});
	
	receiver.onNewExtraction(function(sourceindex, newextraction) {
		stage.graph.delayfixonupdate = false;
		stage.addExtractionNode(sourceindex, newextraction);
	});
	
	receiver.onModifyExtraction(function(sourceindex, index, extraction) {
		stage.setExtractionNode(sourceindex, index, extraction);
	});

	receiver.onShowModelAbstract(function (bioModelAbstract) {
		stage.hideLoader();
		window.alert(bioModelAbstract);
	});
}

//For objects that must be loaded after the rest of the stage is loaded
Stage.prototype.onInitialize = function() {
	var stage = this;

	if (stage.state.models.length > 0) {
		stage.state.models.forEach(function(model) {
			stage.addModelNode(model, [DragToMerge]);
			stage.extractions[model.modelindex] = {modextractions: []};
		});		

	}
	sender.requestExtractions();
	$('#taskModal').hide();
	this.setSavedState(true);
}

Stage.prototype.onModelSelection = function(node) {
	this.leftsidebar.updateModelPanel(node);
}


Stage.prototype.setSavedState = function (issaved) {
	Task.prototype.setSavedState.call(issaved);
	this.setSaved(this.isSaved());
	//$('#saveModel').prop('disabled', issaved);
}


function makeResultSet(searchResultSet, stage) {
	var resultSet = $(
		"<li class='searchResultSet'>" +
			"<label>" + searchResultSet.source + "</label>" +
		"</li>"
	);

    var list = document.createElement('ul');
    for(var i = 0; i < searchResultSet.results.length; i++) {
        var item = document.createElement('li');

        item.className = "searchResultSetValue";
        if (searchResultSet.source == "Example models") {
            item.appendChild(document.createTextNode(searchResultSet.results[i]));
        }
        else if (searchResultSet.source == "BioModels") {
        	item.appendChild(document.createTextNode(searchResultSet.results[i]));
            var abstractButton = document.createElement('button');
            abstractButton.innerHTML = "Abstract";
            item.appendChild(abstractButton);
		}
        else if (searchResultSet.source.includes("Nodes in Project")) {
            item.appendChild(document.createTextNode(searchResultSet.results[i][0] + " (" + searchResultSet.results[i][1] + ")"));
        }
        list.appendChild(item);

        $(item).data("source", searchResultSet.source);
        $(item).data("name", searchResultSet.results[i]);
        $(item).data("id", searchResultSet.results[i][2]);

        $(item).find('button').click(function(e) {
            e.stopPropagation();
            var source = $(this).parent().data("source");
            var name = $(this).parent().data("name");
            if (source == "BioModels") {
                stage.showLoader();
                setTimeout(function() {
                    sender.getModelAbstract(name);
				}, 20);
            }
        });

        $(item).click(function() {
            var source = $(this).data("source");
            var name = $(this).data("name");
            var id = $(this).data("id");
            if (source == "Example models") {
                sender.addModelByName(source, name);
            }
            else if (source == "BioModels") {
				sender.addModelByName(source, name);
            }
            else if (source.includes("Nodes in Project")) {
                var visibleNodes = stage.graph.getVisibleNodes();
                var node = visibleNodes.filter(function (node) {
                    return node.id === id;
                })[0];
				node.onClick();
				node.pulse();

			}

			// Hide the search box
			$(".stageSearch .searchValueContainer").hide();
		});
    }

    resultSet.append(list);
    return resultSet;
};

Stage.prototype.getTaskType = function() { return StageTasks.PROJECT; }