/**
 * 
 */

MergerTask.prototype = new Task();
MergerTask.prototype.constructor = MergerTask;

function MergerTask(graph) {
	Task.prototype.constructor.call(this, graph);
	//this.nodes = models;
	
	var merger = this;
	var nodes = this.nodes;
	
	// Preview merge resolutions
	//TODO: Save the current stage graph, clear it, and load relevant nodes of merge resolution.

	var t = document.querySelector('#mergerContent');

	var clone = document.importNode(t.content, true);
	document.querySelector('#modalContent').appendChild(clone);

	// Create three different graphs on stage to preview Merge Resolutions
//	$("#stage").append(
//		'<div class="substage" id="modelAStage"></div>' +
//		'<div class="substage" id="modelABStage">Merge preview coming soon!</div>' +
//		'<div class="substage" id="modelBStage"></div>' +
//		'<button id="backToMergeRes" type="button" class="btn btn-default" data-toggle="modal" data-target="#mergerModal">Back</button>'
//	);

	// Quit merger
	$("#quitMergerBtn").click(function() {
		// TODO: Warning dialog before quitting
		$("#activeTaskText").removeClass('blink');
		sender.minimizeTask();
	})	
	
	//$('[data-toggle="tooltip"]').tooltip();
	
	if($("#mergerIcon").length == 0	) {
		$("#activeTaskPanel").append("<a data-toggle='modal' href='#mergerModal'><img id='mergerIcon' src='../../src/semgen/icons/mergeicon2020.png' /></a>");
	}
}

MergerTask.prototype.onModelSelection = function(node) {}

MergerTask.prototype.onClose = function() {}

