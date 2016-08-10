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
	this.conflictsj = null; //Handle for calling java functions
	
	this.semrespane;
	this.confrespane;
	
	var t = document.querySelector('#leftMergerMenus');
	var clone = document.importNode(t.content, true);
	
	document.querySelector('#leftSidebar').appendChild(clone);

	//Create the resolution pane
	
	var t = document.querySelector('#mergerContent');

	var clone = document.importNode(t.content, true);
	document.querySelector('#modalContent').appendChild(clone);

	this.readyforMerge = function() {
		var ready = confrespane.readyformerge && merger.semrespane.readyformerge;
			$('.merge').prop('disabled', !ready);

	}
	
	this.showResolutionPane = function() {
		merger.semrespane = new SemanticResolutionPane(this);
		confrespane = new ConflictResolutionPane(this);
		
		merger.semrespane.initialize(this.nodes);

	}
	
	this.isNameAlreadyUsed = function (name) {
		for (x in merger.nodes) {
			 if (merger.nodes[x].hasChildWithName(name)) {
				return true; 
			 }
		}
		return false;
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
			merger.nodes[x].showchildren = false;
			merger.nodes[x].hidden=true;
		}
		merger.addModelNode(mergedname, [DragToMerge]);
		$('#taskModal').modal("hide");

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
	$("#mergeStep2").hide();
	$("#nextBtn").click(function() {
		$("#mergeStep1").hide();
		$("#mergeStep2").slideDown();
	});

	$("#backBtn").click(function() {
		$("#mergeStep1").slideDown();
		$("#mergeStep2").hide();
	});
	

	$(".merge").prop('disabled', true)
		.click(function() {
			if (!$(".merge").prop('disabled')) {
				sender.executeMerge(merger.semrespane.pollOverlaps());
			}
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
