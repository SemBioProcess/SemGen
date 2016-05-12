/**
 * 
 */
function SemanticResolutionPane() {
	var pane = this;
	var resolutions = [];
	// Preview merge resolutions

	var t = document.querySelector('#mergerContent');

	var clone = document.importNode(t.content, true);
	document.querySelector('#modalContent').appendChild(clone);

	//$('[data-toggle="tooltip"]').tooltip();
	
	//Create the resolution panel
	this.addResolutionPanel = function(overlap) {
		
		var dsradiobutton = function (nodeside, desc, id) {
			var nodetype = NodeTypeMap[desc.type];
			return '<label>' +
				'<div class="freetextDef">' + desc.description + '</div>' +
				//'<svg class="'+ nodeside + '" height="10" width="10">' +
				//'<circle cx="5" cy="5" r="5" fill="' + nodetype.color + '"/>' +
				//'</svg>' +
				'<input class="mergeResRadio" type="radio" name="mergeResRadio' + id + '">' +
				'</label>';
		};

		var t = document.querySelector('#overlapPanel');
		var clone = document.importNode(t.content, true);
		
		clone.id = 'res' + resolutions.length;
		clone.index = resolutions.length;
		
		clone.querySelector('#leftRes').innerHTML = dsradiobutton('leftNode', overlap.dsleft, clone.id);
		clone.querySelector('#rightRes').innerHTML = dsradiobutton('rightNode', overlap.dsright, clone.id);
		clone.querySelector('#ignoreRes').innerHTML = '<label><div class="ignoreLabel">Ignore</div><input class="mergeResRadio" type="radio" name="mergeResRadio' + clone.id + '"></label>';

		clone.querySelector('.collapsePane').setAttribute("href", "#collapsePane" + clone.id);
		clone.querySelector('.collapse').setAttribute("id", "collapsePane" + clone.id);

		clone.querySelector('.leftCollapsePanel > .equation').innerHTML = overlap.dsleft.equation;
		clone.querySelector('.rightCollapsePanel > .equation').innerHTML = overlap.dsright.equation;
		clone.querySelector('.leftCollapsePanel > .varName').innerHTML = overlap.dsleft.name;
		clone.querySelector('.rightCollapsePanel > .varName').innerHTML = overlap.dsright.name;
		clone.querySelector('.leftCollapsePanel > .compAnnotation').innerHTML = overlap.dsleft.compAnnotation;
		clone.querySelector('.rightCollapsePanel > .compAnnotation').innerHTML = overlap.dsright.compAnnotation;
		clone.querySelector('.leftCollapsePanel > .unit').innerHTML = overlap.dsleft.unit;
		clone.querySelector('.rightCollapsePanel > .unit').innerHTML = overlap.dsright.unit;
		
		clone.querySelector('.collapsePane').setAttribute("onclick", 'sender.requestPreview(' + clone.index + ');');
		
		resolutions.push(clone);
		document.querySelector('#modalContent #overlapPanels').appendChild(clone);
		
		$("#hideResolutionsBtn").click(function() {
			$('#taskModal').modal("hide");
		})
	}


	//Preview graphs
	this.leftgraph = new PreviewGraph("modelAStage");
	this.midgraph = new PreviewGraph("modelABStage");
	this.rightgraph = new PreviewGraph("modelBStage");

	this.initialize = function(nodes) {
		var nodearr = getSymbolArray(nodes);
		$(".leftModelName").append(nodearr[0].id);
		$(".rightModelName").append(nodearr[1].id);
		
		this.leftgraph.initialize();
		this.midgraph.initialize();
		this.rightgraph.initialize();

		sender.requestOverlaps();
	}
	
	receiver.onShowOverlaps(function(data) {
		data.forEach(function(d) {
			pane.addResolutionPanel(d);	
		});
	});
	
	receiver.onShowPreview(function(data) {
		pane.leftgraph.update(data.left);
		pane.midgraph.update(data.middle);
		pane.rightgraph.update(data.right);
	});

	// Adjust preview window size
	$('#resizeHandle').mousedown(function(e) {
		e.preventDefault();
		$(document).mousemove(function(e) {
			$('.mergePreview').css("height",e.pageY-95);
			$('.modal-body').css("height", $(window).height()-e.pageY-95);
		});
	});
	$(document).mouseup(function(e) {
		$(document).unbind('mousemove');
	});
}

