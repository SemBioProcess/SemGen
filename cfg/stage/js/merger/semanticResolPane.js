/**
 * 
 */
function SemanticResolutionPane(merger) {
	var pane = this;
	var task = merger;
	var resolutions = [];
	var choices = [];
	this.readyformerge = false;

	//Checks to see if requirements are met for merging.
	this.allchoicesmade = function () {
		var choices = pane.pollOverlaps();
		
		var disable = false;
		choices.forEach(function(c) {
			if (c==-1) disable = true; 
		});
		
		this.readyformerge = !disable;
		merger.readyforMerge();
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
		clone.name = overlap.dsleft;
		
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
		
		clone.querySelector('.mergeResCollapsePane').setAttribute("data-target", "#collapsePane" + clone.id);
		clone.querySelector('.mergeResCollapse').setAttribute("id", "collapsePane" + clone.id);

		clone.querySelector('.leftCollapsePanel > .equation').innerHTML = overlap.dsleft.equation;
		clone.querySelector('.rightCollapsePanel > .equation').innerHTML = overlap.dsright.equation;
		clone.querySelector('.leftCollapsePanel > .varName').innerHTML = overlap.dsleft.name;
		clone.querySelector('.rightCollapsePanel > .varName').innerHTML = overlap.dsright.name;
		clone.querySelector('.leftCollapsePanel > .annotation').innerHTML = overlap.dsleft.annotation;
		clone.querySelector('.rightCollapsePanel > .annotation').innerHTML = overlap.dsright.annotation;
		clone.querySelector('.leftCollapsePanel > .unit').innerHTML = overlap.dsleft.unit;
		clone.querySelector('.rightCollapsePanel > .unit').innerHTML = overlap.dsright.unit;
		
		clone.querySelector('input.mergeResRadio').disabled = merger.mergecomplete;
		clone.querySelector('.previewResolutionBtn').setAttribute("onclick", 'sender.requestPreview(' + clone.index + ');');

		if (overlap.similar) {
            clone.querySelector('.mappingType').innerHTML = "Similar semantic match";
		}
		else {
            clone.querySelector('.mappingType').innerHTML = "Exact semantic match";
		}

		//Custom mappings can be removed
		if (overlap.custom) {
			clone.querySelector('.removeMappingBtn').style.display = 'inherit';
			clone.querySelector('.removeMappingBtn').onclick = function() {
				choices.splice(clone.index, 1);
				sender.removeCustomOverlap(clone.index);
			};
			clone.querySelector('.removeMappingBtn').disabled = merger.mergecomplete;

			clone.querySelector('.mappingType').innerHTML = "Manual mapping";
		}

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
		
		$(".hideResolutionsBtn").click(function() {
			$('#taskModal').modal("hide");
		})
	}


	//Preview graphs
	this.leftgraph = new PreviewGraph("modelAStage");
	this.rightgraph = new PreviewGraph("modelBStage");

	var leftModelName = "";
	var rightModelName = "";

	this.initialize = function(nodes) {
		var nodearr = getSymbolArray(nodes);
		leftModelName = nodearr[0].id;
		rightModelName = nodearr[1].id;
		$(".leftModelName").append(leftModelName);
		$(".rightModelName").append(rightModelName);
		$('.addManualMap').prop("disabled", true);
		
		sender.requestOverlaps();
	};

	this.updateOverlaps = function(overlaps) {
		resolutions = [];
		$('#modalContent #overlapPanels').contents().remove();
		
		overlaps.forEach(function(d) {
			pane.addResolutionPanel(d);	
		});
		
		for (i=0; i<choices.length; i++) {
			resolutions[i].setSelection(choices[i]);
		}
		//Disable radio buttons if the merge has been performed.
		$('input.mergeResRadio').prop('disabled', merger.mergecomplete);
		pane.allchoicesmade();
		//This is done here to prevent synchronicity issues between this and the conflict pane
		sender.requestConflicts();
	}
	
	receiver.onShowOverlaps(function(data) {
		pane.updateOverlaps(data);
	});
	
	receiver.onShowPreview(function(data) {
		pane.leftgraph.initialize();
		pane.rightgraph.initialize();

		$("#fixedNodesA").removeAttr("checked");
		$("#fixedNodesB").removeAttr("checked");

		//Add indicators to left/right model choices
        data.choices[0].childsubmodels[1].id += "_left";
        data.choices[0].childsubmodels[1].id += "_right";
        data.choices[1].childsubmodels[0].id += "_left";
        data.choices[0].childsubmodels[1].id += "_right";

        pane.leftgraph.setPreviewData(data.choices[0]);
		pane.rightgraph.setPreviewData(data.choices[1]);

	});

	// Prevent clicking on radio button from toggling collapse panel
	$(document).on("click", ".radio", function(e) {
		e.stopPropagation();
	});

	// Adjust preview window size
	$('#resizeHandle').mousedown(function(e) {
		e.preventDefault();
		$(document).mousemove(function(e) {
			$('.mergePreview').css("height", e.pageY-75);
			$('.modal-body').css("height", $(window).height()-e.pageY-75);
			pane.leftgraph.initialize();
			pane.rightgraph.initialize();
			
		});
		$(document).mouseup(function() {
			$(document).unbind('mousemove');
		});
	});

	// Dynamically adjust modal body height
	$(window).on('load resize', function(){
		$('.modal-body').css("height", $(window).height()-$('.mergePreview').height()-150);
	});

	this.pollOverlaps = function() {
		choices = [];
		resolutions.forEach(function(panel) {
			choices.push(panel.poll());
		});
		return choices;
	}

	this.semanticOverlapReolvesSyntactic = function(name) {
		for (i in resolutions) {
			if (resolutions[i].name.name == name) {
				if (resolutions[i].choice == 1 || resolutions[i].choice == 0) {
					return true;
				}
				return false;
			}
		}
		return false;
	}
	
	this.createManualLink = function(dependency) {
		var link = document.createElement("a");
			link.className = "list-group-item"; 
			link.href = "#";
			link.id = dependency.id;
			link.text = dependency.name;
			link.hash = dependency.hash;
			link.linkedhash = dependency.linkedhash;
			
			if (link.linkedhash != -1) {
				link.className = "list-group-item mapped"; 
			}
			
		return link;
	}
	
	var manualleft = null, manualright = null;
	$('.addManualMap').click(function() {
		if (manualleft!=null && manualright!=null) {
        	var modelstomerge = manualleft + "," + manualright;
        	$('.merge').prop('disabled', true);
        	sender.createCustomOverlap(modelstomerge);

            // Add custom link visualization
            var manualleftnode = merger.graph.findNode(manualleft);
            var manualrightnode = merger.graph.findNode(manualright);
			var customlink = {
                id : manualleftnode.id + "-" + manualrightnode.id,
                linklevel: 3,
                linkType: manualleftnode.nodeType.id,
                input: manualrightnode,
                output: manualleftnode,
                length: 100,
                external: true
            }
            manualleftnode.srcobj.inputs.push(customlink);
            manualleftnode.graph.update();
		}
	});
	
	receiver.onShowMappingCandidates(function(deplist) {
		$('#manualMapLeftModel').contents().remove();
		$('#manualMapRightModel').contents().remove();
		var links = [];
		for (i=0; i< deplist[0].length; i++) {
			links.push(pane.createManualLink(deplist[0][i]));
		}
		for (i=0; i< links.length; i++) {
			document.querySelector('#manualMapLeftModel').appendChild(links[i]);
		}
		
		links = [];
		for (i=0; i< deplist[1].length; i++) {
			links.push(pane.createManualLink(deplist[1][i]));
		}
		for (i=0; i< links.length; i++) {
			document.querySelector('#manualMapRightModel').appendChild(links[i]);
		}
		
	    $("#manualMapLeftModel").find("a").click(function(e) {
	        e.preventDefault();

	        $that = $(this);

	        if($that.hasClass('active')) {
	        	$that.removeClass('active');
	        	manualleft = null;
	        	$('.addManualMap').prop("disabled", true);
	        	
			}
			else {
				$("#manualMapLeftModel").find('a').removeClass('active');
	            $that.addClass('active');
	            manualleft = $that.attr("id");
	            if (manualright != null) {
	            	$('.addManualMap').prop("disabled", false);
	            }
	        }
	    });

	    $("#manualMapRightModel").find("a").click(function(e) {
	        e.preventDefault();

	        $that = $(this);

	        if($that.hasClass('active')) {
	            $that.removeClass('active');
	            manualright.selectionhash = null;
	            $('.addManualMap').prop("disabled", true);
	        }
	        else {
	        	$("#manualMapRightModel").find('a').removeClass('active');
	            $that.addClass('active');
	            manualright = $that.attr("id");
	            if (manualleft != null) {
	            	$('.addManualMap').prop("disabled", false);
	            }
	        }
	    });
	});
	
	$("#fixedNodesA").bind('change', function(){
		pane.leftgraph.toggleFixedMode(this.checked);
	});
	$("#fixedNodesB").bind('change', function(){
		pane.rightgraph.toggleFixedMode(this.checked);
	});
}

