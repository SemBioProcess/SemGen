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
	
	document.getElementById("modalContent").innerHTML='<object type="text/html" data="merger.html" ></object>';
	// Preview merge resolutions
	//TODO: Save the current stage graph, clear it, and load relevant nodes of merge resolution.



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
		$(".substage").remove();
		$("#activeTaskText").removeClass('blink');
		$("#mergerIcon").remove();
		$("#backToMergeRes").remove();
	})	

}