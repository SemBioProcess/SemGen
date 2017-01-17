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

	$("#addModelButton, .stageSearch").hide();
	$("#trash").show();
	var droploc;
	
	var onExtractionAction = function(node) {
		//Don't add any extraction actions to the source model node.
		
		if (extractor.sourcemodel == node.srcnode) return;
		node.drag.push(function(selections) {
			if (extractor.sourcemodel.hullContainsPoint([node.xpos(), node.ypos()])) {
				
			}
		});
		
		node.dragEnd.push(function(selections) {
			extractor.graph.shiftIsPressed = false;
			droploc = [node.xpos(), node.ypos()];
			if (extractor.sourcemodel.hullContainsPoint(droploc)) {
				return;
			}
			//trashcan behavior here
			//Check to see if node is inside an extraction
			for (e in extractor.extractions) {
				if (extractor.extractions[e].hullContainsPoint(droploc)) {
					
					return;
				}
			}
			//If it's dropped in empty space, create a new extraction
			var name = prompt("Enter name for extraction.", ""),
				extractarray = [];
			for (x in selections) {
				extractarray.push(selections[x].srcnode);
			}

			extractor.extractionjs.createExtraction(extractarray, name);
			
		});
	}

	this.graph.ghostBehaviors.push(onExtractionAction);
	
	receiver.onNewExtraction(function(newextraction) {
		var extractionnode = new ExtractedModel(extractor.graph, newextraction);
		extractor.extractions.push(extractionnode);
		extractor.nodes[newextraction.id] = extractionnode;
		extractionnode.setLocation(droploc[0], droploc[1]);
		extractor.graph.update();
		extractor.selectNode(extractionnode);
		
	});
	
	$("#stageModel").click(function() {
		sender.sendModeltoStage();
	});
	
	$("#minimize").click(function() {
		sender.changeTask(0);
	});
	
	$("#saveModel").click(function() {
		sender.save();
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
