/**
 * 
 */
//TODO: Save the current stage graph, clear it, and load relevant nodes of merge resolution.

ExtractorTask.prototype = new Task();
ExtractorTask.prototype.constructor = ExtractorTask;

function ExtractorTask(graph, stagestate) {
	Task.prototype.constructor.call(this, graph, stagestate);
	var extractor = this;
	
	this.taskindex = stagestate.taskindex;
	
	var t = document.querySelector('#leftExtractorMenus');
	var clone = document.importNode(t.content, true);
	
	document.querySelector('#leftSidebar').appendChild(clone);

	$("#addModelButton, .stageSearch").hide();

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
		if (!merger.isSaved()) {
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
		extractor.addModelNode(model, []);
	});
}

ExtractorTask.prototype.onMinimize = function() {
	$("#activeTaskText").removeClass('blink');
	sender.minimizeTask(this.task);
}

ExtractorTask.prototype.onModelSelection = function(node) {
	
}

ExtractorTask.prototype.onClose = function() {

}

ExtractorTask.prototype.getTaskType = function() { return StageTasks.EXTRACTOR; }
