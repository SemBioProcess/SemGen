/**
 * 
 */
//TODO: Save the current stage graph, clear it, and load relevant nodes of merge resolution.

MergerTask.prototype = new Task();
MergerTask.prototype.constructor = MergerTask;

function MergerTask(graph, state) {
	Task.prototype.constructor.call(this, graph, state);
	
	var merger = this;
	
	// Quit merger
	$("#quitMergerBtn").click(function() {

	})	

	this.showResolutionPane = function() {
		var semrespane = new SemanticResolutionPane();
		semrespane.initialize(this.nodes);
	}
	
}

MergerTask.prototype.onInitialize = function() {
	if($("#mergerIcon").length == 0	) {
		$("#activeTaskPanel").append("<a data-toggle='modal' href='#taskModal'><img id='mergerIcon' src='../../src/semgen/icons/mergeicon2020.png' /></a>");
	}
	this.showResolutionPane();
}

MergerTask.prototype.onMinimize = function() {
	$("#activeTaskText").removeClass('blink');
	sender.minimizeTask(this.task);
}

MergerTask.prototype.onModelSelection = function(node) {
	
	
}

MergerTask.prototype.onClose = function() {
	$("#activeTaskText").removeClass('blink');
	$("#mergerIcon").remove();
	sender.minimizeTask(this.task);
}
