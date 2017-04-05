/**
 * 
 */
//TODO: Save the current stage graph, clear it, and load relevant nodes of merge resolution.

ExtractorTask.prototype = new Task();
ExtractorTask.prototype.constructor = ExtractorTask;

function ExtractorTask(graph, stagestate) {
	Task.prototype.constructor.call(this, graph, stagestate);
	var extractor = this;

	this.extractionjs = null; //Handle for calling java functions

	
	$("#addModelButton, .stageSearch").hide();
	

	
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
