/**
 * 
 */
//TODO: Save the current stage graph, clear it, and load relevant nodes of merge resolution.

MergerTask.prototype = new Task();
MergerTask.prototype.constructor = MergerTask;

function MergerTask(graph, stagestate) {
	Task.prototype.constructor.call(this, graph, stagestate);
	graph.depBehaviors.push(CreateCustomOverlap);
	var merger = this;
	
	this.semrespane;
	
	var t = document.querySelector('#leftMergerMenus');
	var clone = document.importNode(t.content, true);
	
	document.querySelector('#leftSidebar').appendChild(clone);

	this.showResolutionPane = function() {
		merger.semrespane = new SemanticResolutionPane();
		merger.semrespane.initialize(this.nodes);
	}
	
	$("#addModelButton").hide();
	$(".stageSearch").hide();
	
	$("#resolPanels").click(function() {
		$('#taskModal').modal("show");
		sender.requestOverlaps();
	});
	
	// Quit merger
	$("#quitMergerBtn").click(function() {

	});

	receiver.onMergeCompleted(function(mergedname) {
		for (x in merger.nodes) {
			merger.nodes[x].setChildren(null, null);
			merger.nodes[x].hidden=true;
		}
		merger.addModelNode(mergedname, [DragToMerge]);
		$('#taskModal').modal("hide");

	});
	
	receiver.onReceiveReply(function (reply) {
		CallWaiting(reply);
	});

	receiver.onReceiveReply(function (reply) {
		CallWaiting(reply);
	});
}

MergerTask.prototype.onInitialize = function() {
	var merger = this;
	
	merger.state.models.forEach(function(model) {
		merger.addModelNode(model, []);
	});

	if($("#mergerIcon").length == 0	) {
		$("#activeTaskPanel").append("<a data-toggle='modal' href='#taskModal'><img id='mergerIcon' src='../../src/semgen/icons/mergeicon2020.png' /></a>");
	}
	merger.showResolutionPane();
	$(".merge").prop('disabled', true)
		.click(function() {
			sender.executeMerge(merger.semrespane.pollOverlaps());
		});
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
