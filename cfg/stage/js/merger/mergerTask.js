/**
 * 
 */

MergerTask.prototype = new Task();
MergerTask.prototype.constructor = MergerTask;

function MergerTask(graph, state) {
	Task.prototype.constructor.call(this, graph, state);
	
	var merger = this;
	var nodes = this.nodes;
	var task = this;
	
	var resolutions = [];
	// Preview merge resolutions
	//TODO: Save the current stage graph, clear it, and load relevant nodes of merge resolution.

	var t = document.querySelector('#mergerContent');

	var clone = document.importNode(t.content, true);
	document.querySelector('#modalContent').appendChild(clone);

	// Quit merger
	$("#quitMergerBtn").click(function() {
		// TODO: Warning dialog before quitting
		$("#activeTaskText").removeClass('blink');
		sender.minimizeTask(task);
	})	
	
	//$('[data-toggle="tooltip"]').tooltip();
	
		// Slide up panel for Active Task Tray
	$("#activeTaskTray").click(function() {
		$("#activeTaskPanel").slideToggle();
	});
	
	//Create the resolution panel
	this.addResolutionPanel = function(overlap) {
		
		var dsradiobutton = function (nodeside, desc) {
			var nodetype = NodeTypeMap[desc.type];
			return '<label>' + 
				'<div class="modelVarName">' + desc.name + '</div>' +
				//'<div class="modelVarEquation">' + desc.equation + '</div>' +
				'<svg class="'+ nodeside + '" height="10" width="10">' +
					'<circle cx="5" cy="5" r="5" fill="' + nodetype.color + '"/>' +
				'</svg>' + 
				'<input type="radio" name="mergeResRadio">' +
			'</label>';

		}

		var t = document.querySelector('#overlapPanel');
		var clone = document.importNode(t.content, true);
		
		clone.id = "res" + resolutions.length;
		
		clone.querySelector('#leftRes').innerHTML = dsradiobutton('leftNode', overlap.dsleft);
		clone.querySelector('#rightRes').innerHTML = dsradiobutton('rightNode', overlap.dsright);
		
		resolutions.push(clone);
		
		document.querySelector('#modalContent #overlapPanels').appendChild(clone);
		
		// Preview merge resolutions
		$(".mergePreviewBtn").click(function() {
			//TODO: Save the current stage graph, clear it, and load relevant nodes of merge resolution.

			// Create three different graphs on stage to preview Merge Resolutions
			if(!$("#stage").hasClass("mergePreview")) {
				$("#stage").append(
					'<div class="mergePreview">' +
					'<div class="substage" id="modelAStage"><img src="modelA.png" style="width:200px;height:200px;"></div>' +
					'<div class="substage" id="modelABStage"><img src="modelAB.png" style="width:400px;height:400px;"></div>' +
					'<div class="substage" id="modelBStage"><img src="modelB.png" style="width:200px;height:200px;"></div>' +
					'<button id="backToMergeRes" type="button" class="btn btn-default" data-toggle="modal" data-target="#mergerModal">Back</button>' +
					'</div>'
				);
				$('#taskModal').hide();
			}
		});
	}
	
	receiver.onShowOverlaps(function(data) {
		data.forEach(function(d) {
			task.addResolutionPanel(d);	
		});
	});
}

MergerTask.prototype.onInitialize = function() {
	var nodearr = getSymbolArray(this.nodes);
	$("#ModelA").append(nodearr[0].id);
	$("#ModelB").append(nodearr[1].id);
	
	if($("#mergerIcon").length == 0	) {
		$("#activeTaskPanel").append("<a data-toggle='modal' href='#mergerModal'><img id='mergerIcon' src='../../src/semgen/icons/mergeicon2020.png' /></a>");
	}
	
	sender.requestOverlaps();
}

MergerTask.prototype.onModelSelection = function(node) {}

MergerTask.prototype.onClose = function() {}

