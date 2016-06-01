/**
 * 
 */
function SemanticResolutionPane() {
	var pane = this;
	var resolutions = [];
	var choices = [];
	
	// Preview merge resolutions

	var t = document.querySelector('#mergerContent');

	var clone = document.importNode(t.content, true);
	document.querySelector('#modalContent').appendChild(clone);
	
	//Checks to see if requirements are met for merging.
	this.allchoicesmade = function () {
		var disable = false;
		var choices = pane.pollOverlaps();
		choices.forEach(function(c) {
			if (c==-1) disable = true; 
		});
		$('.merge').prop('disabled', disable);
	}
	
	//Create the resolution panel
	this.addResolutionPanel = function(overlap) {
		var dsradiobutton = function (nodeside, desc, id) {
			var nodetype = NodeTypeMap[desc.type];
			return '<label>' +
				'<div class="freetextDef">' + desc.description + '</div>' +

				'<input class="mergeResRadio" type="radio" name="mergeResRadio' + id + '">' +
				'</label>';
		};
		
		var t = document.querySelector('#overlapPanel');
		var clone = document.importNode(t.content, true);
		
		clone.id = 'res' + resolutions.length;
		clone.index = resolutions.length;

		clone.choice = -1;
		clone.radiobtns = [];
		
		var radioClick = function(val) {
			clone.choice = val;
			pane.allchoicesmade();
		}
		
		clone.radiobtns.push(clone.querySelector('.leftRes'));
		clone.querySelector('.leftRes').setAttribute("id", 'leftRes' + clone.index);
		clone.querySelector('.leftRes').innerHTML = dsradiobutton('leftNode', overlap.dsleft, clone.id);
		clone.querySelector('.leftRes').onclick = function() {radioClick(0);};
		
		clone.radiobtns.push(clone.querySelector('.rightRes'));
		clone.querySelector('.rightRes').innerHTML = dsradiobutton('rightNode', overlap.dsright, clone.id);
		clone.querySelector('.rightRes').onclick = function() {radioClick(1);};
		clone.querySelector('.rightRes').setAttribute("id", 'rightRes' + clone.index);
		
		clone.radiobtns.push(clone.querySelector('.ignoreRes'));
		clone.querySelector(".ignoreRes").innerHTML = '<label><div class="ignoreLabel">Ignore</div>' +
			'<input class="mergeResRadio" type="radio" name="mergeResRadio' + clone.id + '"></label>';
		clone.querySelector('.ignoreRes').onclick = function() {radioClick(2);};
		clone.querySelector('.ignoreRes').setAttribute("id", 'ignoreRes' + clone.index);
		
		clone.querySelector('.collapsePane').setAttribute("data-target", "#collapsePane" + clone.id);
		clone.querySelector('.collapse').setAttribute("id", "collapsePane" + clone.id);

		clone.querySelector('.leftCollapsePanel > .equation').innerHTML = overlap.dsleft.equation;
		clone.querySelector('.rightCollapsePanel > .equation').innerHTML = overlap.dsright.equation;
		clone.querySelector('.leftCollapsePanel > .varName').innerHTML = overlap.dsleft.name;
		clone.querySelector('.rightCollapsePanel > .varName').innerHTML = overlap.dsright.name;
		clone.querySelector('.leftCollapsePanel > .annotation').innerHTML = overlap.dsleft.annotation;
		clone.querySelector('.rightCollapsePanel > .annotation').innerHTML = overlap.dsright.annotation;
		clone.querySelector('.leftCollapsePanel > .unit').innerHTML = overlap.dsleft.unit;
		clone.querySelector('.rightCollapsePanel > .unit').innerHTML = overlap.dsright.unit;
		
		clone.querySelector('.previewResolutionBtn').setAttribute("onclick", 'sender.requestPreview(' + clone.index + ');');
		
		clone.setSelection = function(sel) {
			if (sel==-1) return;
			clone.choice = sel;
			document.querySelector('#' + clone.radiobtns[sel].id + ' .mergeResRadio').setAttribute('checked','true');
		}
		
		clone.poll = function() {
			return clone.choice;
		}
		resolutions.push(clone);
		document.querySelector('#modalContent #overlapPanels').appendChild(clone);
		
		$("#hideResolutionsBtn").click(function() {
			$('#taskModal').modal("hide");
		})
	}


	//Preview graphs
	this.leftgraph = new PreviewGraph("modelAStage");
	this.rightgraph = new PreviewGraph("modelBStage");

	this.initialize = function(nodes) {
		var nodearr = getSymbolArray(nodes);
		$(".leftModelName").append(nodearr[0].id);
		$(".rightModelName").append(nodearr[1].id);
		
		sender.requestOverlaps();
	};
	
	receiver.onShowOverlaps(function(data) {
		resolutions = [];
		var olaps = document.querySelector('#modalContent #overlapPanels');
		while (olaps.firstChild) {
			olaps.removeChild(olaps.firstChild);
		}
		
		data.forEach(function(d) {
			pane.addResolutionPanel(d);	
		});
		
		for (i=0; i<choices.length; i++) {
			resolutions[i].setSelection(choices[i]);
		}
	});
	
	receiver.onShowPreview(function(data) {
		pane.leftgraph.initialize();
		pane.rightgraph.initialize();

		pane.leftgraph.update(data.left);
		pane.rightgraph.update(data.right);
	});

	// Prevent clicking on radio button from toggling collapse panel
	$(document).on("click", ".radio", function(e) {
		e.stopPropagation();
	});

	// Adjust preview window size
	$('#resizeHandle').mousedown(function(e) {
		e.preventDefault();
		$(document).mousemove(function(e) {
			$('.mergePreview').css("height", e.pageY-95);
			$('.modal-body').css("height", $(window).height()-e.pageY-95);
			pane.leftgraph.initialize();
			pane.rightgraph.initialize();
			
		});
		$(document).mouseup(function() {
			$(document).unbind('mousemove');
		});
	});
	
	this.pollOverlaps = function() {
		choices = [];
		resolutions.forEach(function(panel) {
			choices.push(panel.poll());
		});
		return choices;
	}
}

