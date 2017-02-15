/**
 * 
 */
//TODO: Save the current stage graph, clear it, and load relevant nodes of merge resolution.

ExtractorTask.prototype = new Task();
ExtractorTask.prototype.constructor = ExtractorTask;

function ExtractorTask(graph, stagestate) {
	Task.prototype.constructor.call(this, graph, stagestate);
	var extractor = this;
	
	extractor.graph.depBehaviors = [];
	extractor.graph.ghostBehaviors = [];
	extractor.extractions = [];
	
	this.extractionjs = null; //Handle for calling java functions
	extractor.taskindex = stagestate.taskindex;
	extractor.sourcemodel = null;
	
	var t = document.querySelector('#leftExtractorMenus');
	var clone = document.importNode(t.content, true);
	
	document.querySelector('#leftSidebar').appendChild(clone);

	var trash = new StageDoodad(this.graph, "trash", 0.1, 0.9, 2.0, 2.0, "glyphicon glyphicon-trash");
	this.graph.doodads.push(trash);
	
	$("#addModelButton, .stageSearch").hide();
	
	var droploc;
	
	var onExtractionAction = function(node) {
		//Don't add any extraction actions to the source model node.
		
		if (extractor.sourcemodel == node.srcnode) return;
		node.drag.push(function(selections) {
			if (trash.isOverlappedBy(node, 2.0)) {
				$("#trash").attr("background", "red");
			}
			else {
				$("#trash").attr("background", "transparent");
			}
			
			if (extractor.sourcemodel.hullContainsPoint([node.xpos(), node.ypos()])) {
				
			}
		});
		
		node.dragEnd.push(function(selections) {
			extractor.graph.shiftIsPressed = false;
			droploc = [node.xpos(), node.ypos()];
			if (extractor.sourcemodel.hullContainsPoint(droploc)) {
				return;
			}

			//Check to see if node is inside an extraction
			for (e in extractor.extractions) {
				if (extractor.extractions[e].hullContainsPoint(droploc)) {
					sender.addNodestoExtraction(extractions[e], extractarray);
					return;
				}
			}
			//If it's dropped in empty space, create a new extraction
			var name = prompt("Enter name for extraction.", ""),
				extractarray = [];
			//Don't create extraction if user cancels
			if (name==null) return;
			for (x in selections) {
				extractarray.push(selections[x].srcnode);
			}
			
			//If the node is dragged to the trash
			if (trash.isOverlappedBy(node, 2.0)) {
				sender.createExtractionExclude(extractarray, name);
				return;
			}
			
			sender.newExtraction(extractarray, name);
			
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
	
	receiver.onLoadExtractions(function(extractions) {
		for (x in extractions) {
			extractor.addExtractionNode(extractions[x]);
		}
	});
	
	receiver.onNewExtraction(function(newextraction) {
		extractor.addExtractionNode(newextraction);
	});
	
	$("#stageModel").click(function() {
		var extractstostage = [];
		for (i in extractor.extractions) {
			if (extractor.extractions[i].selected) {
				extractstostage.push(extractor.extractions[i].modelindex);
				}
			}
		sender.sendModeltoStage(extractstostage);
	});
	
	$("#minimize").click(function() {
		sender.changeTask(0);
	});
	
	$("#saveModel").click(function() {
		var extractstosave = [];
		for (i in extractor.extractions) {
			if (!extractor.extractions[i].saved && extractor.extractions[i].selected)
				extractstosave.push(extractor.extractions[i].modelindex);
		}
		sender.save(extractstosave);
	});
	
	// Quit merger
	$("#quitExtractorBtn").click(function(e) {
		if (!extractor.isSaved()) {
			e.preventDefault();
            var r = confirm("Close without saving?");
            if (r) {
            	sender.close();
            }
		}
		else {
			sender.close();
		}
	});
}

ExtractorTask.prototype.setSavedState = function (issaved) {
	Task.prototype.setSavedState.call(issaved);
	this.setSaved(this.isSaved());
	$('#saveModel').prop('disabled', issaved);
}

//Everything that needs to be called after the stage and graph are set up.
ExtractorTask.prototype.onInitialize = function() {
	var extractor = this;
	
	extractor.state.models.forEach(function(model) {	
		extractor.sourcemodel = extractor.addModelNode(model, []);
	});
	sender.requestExtractions();
}

ExtractorTask.prototype.onMinimize = function() {
	$("#activeTaskText").removeClass('blink');
	sender.minimizeTask(this.task);
}

ExtractorTask.prototype.onModelSelection = function(srcmodel, node) {
	
}

ExtractorTask.prototype.onClose = function() {

}

ExtractorTask.prototype.getTaskType = function() { return StageTasks.EXTRACTOR; }
