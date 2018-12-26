/**
 * Defines graph functionality
 *
 * Adapted from: http://stackoverflow.com/questions/11400241/updating-links-on-a-force-directed-graph-from-dynamic-json-data
 */


function Graph() {
	var graph = this;
	this.w = $(window).width();
	this.h = $(window).height();
	//Ensure the stage has a minimum size.
	if ((this.w < 1280) || (this.h < 1024)) {
		this.worldsize = [[-1280*5, -1024*5], [1280*5, 1024*5]];
	}
	else {
		this.worldsize = [[-this.w*5, -this.h*5], [this.w*5, this.h*5]];
	}

	var visibleNodes = [];
	this.doodads = [];
	this.defaultcursor = 'move';
	$('#stage').css('cursor', graph.defaultcursor);

	var svg = d3.select("#stage")
	    .append("svg")
	    .attr("id", "svg")
	    .attr("pointer-events", "all")
	    .attr("perserveAspectRatio", "xMinYMid");

	var vis = svg.append('g').attr("class", "canvas");

	var centerx = new StageDoodad(this, "trash", 0.0, 0.0, 2.0, 2.0, "label", "X");
	this.doodads.push(centerx);

	this.drag = NodeDrag();

    this.force = d3.forceSimulation()
        .alphaMin(0.07)
        .velocityDecay(0.2)
        .force("charge", d3.forceManyBody()
            .strength(function(d) {return d.charge;})
            .distanceMax(400)
            .theta(0.6))
        .force("link", d3.forceLink()
            .id(function(d) { return d.id; })
            .distance(function (d)
            { return d.length; })
            .strength(0.1)
        );


	var links = this.force.force("link").links();

	this.nodecharge = defaultcharge;
	this.linklength = defaultlinklength;
	this.color = d3.scaleOrdinal(d3.schemeCategory10);

	this.fixedMode = false;
	this.delayfixonupdate = true; //Overrides the graph's default behavior to run for a short period on update even when fixedMode is active.
	//Node type visibility: model, submodel, state, rate, force, constitutive, entity, process, mediator, null, extraction, unspecified
	this.nodesVisible = [true, true, true, true, true, true, true, true, true, true, true, true];
	this.showorphans = false;

	this.depBehaviors = [];
	this.ghostBehaviors = [];

	var nodes;
	this.contextMenu = new ContextMenu(this);

	this.setTaskNodes = function(tasknodes) {
		nodes = tasknodes;
		graph.update();
	};

	$('#stage').click(function() {
		graph.contextMenu.hideMenu();
	});

	svg.call(d3.zoom()
			.scaleExtent([0.1, 10])
			.translateExtent(graph.worldsize)
			.on("zoom", function zoomed() {
					vis.attr("transform", d3.event.transform);
	        }))
	        .on("dblclick.zoom", null);

	$('#toggleMoveStageButton').addClass('on');

	$('#toggleMoveStageButton').click(function() {
		$('#toggleMoveStageButton').addClass('on');
		$('#toggleSelectButton').removeClass('on');

		graph.defaultcursor = 'move';
		$('#stage').css('cursor', 'move');
		svg.on('mousedown.drag', null);
		svg.call(d3.zoom()
				.scaleExtent([0.1, 10])
				.translateExtent(graph.worldsize)
				.on("zoom", function zoomed() {
						vis.attr("transform", d3.event.transform);
		        }))
		        .on("dblclick.zoom", null);

	});

	$('#toggleSelectButton').click(function() {
		$('#toggleSelectButton').addClass('on');
		$('#toggleMoveStageButton').removeClass('on');

		$('#stage').css('cursor', 'auto');
		graph.defaultcursor = 'auto';
        d3.selectAll('.node').on('mousedown.drag', null);
        svg.call(BoundingBox(visibleNodes));
        svg.call(d3.zoom()
            .scaleExtent([0.1, 10])
            .translateExtent(graph.worldsize)
            .on("zoom", function zoomed() {
                vis.attr("transform", d3.event.transform);
            }))
            .on("dblclick.zoom", null)
            .on("mousedown.zoom", null)
            .on("touchstart.zoom", null)
            .on("touchmove.zoom", null)
            .on("touchend.zoom", null);
	});



	this.getVisibleNodes = function() {
		return visibleNodes;
	}

	this.getModels = function() {
		return getSymbolArray(nodes);
	}

	// Remove a node from the graph
	this.removeNode = function (node) {
		var i = 0;

	    while (i < links.length) {
	    	if ((links[i].source == node)||(links[i].target == node))
	            links.splice(i,1);
	        else
	        	i++;
	    }
	    graph.update();
	    $(this).triggerHandler("nodeRemoved", [node]);
	};

	// Remove all links
	this.removeAllLinks = function(){
	    links.length = 0;
	};

	// Check to see if there's at least 1 node of the given type
	this.hasNodeOfType = function (type) {
		for(var index = 0; index < visibleNodes.length; index++) {
			if(visibleNodes[index].nodeType == type)
				return true;
		}

		return false;
	};

	// Hide all visibleNodes of the given type
	this.showNodes = function (type) {
		this.nodesVisible[NodeTypeMap[type].id] = true;
        graph.delayfixonupdate = false;
        this.update();
	}

	// Hide all visibleNodes of the given type
	this.hideNodes = function (type) {
		this.nodesVisible[NodeTypeMap[type].id] = false;
        graph.delayfixonupdate = false;
		this.update();
	}

	var ghost;
	this.createGhostNodes = function(nodes) {
		var ghosts = [];
		for (n in nodes) {
			ghosts.push(nodes[n].createGhost());
		}
		// Build the ghost nodes
	    ghost = vis.selectAll(".ghost")
	        .data(ghosts, function(d) { return d.id; });

	    ghost.enter().append("g")
	        .each(function (d) { d.createVisualElement(this, graph); });

		return ghosts;
	}

	/**
	 * Updates the graph
	 */
	var path;
	var node;
	this.update = function (nodesToUpdate) {
		$(this).triggerHandler("preupdate");

		bruteForceRefresh.call(this);

		for (x in this.doodads) {
			this.doodads[x].createVisualElement();
		}
		// Build the ghost nodes
	    ghost = vis.selectAll(".ghost")
	        .data([], function(d) { return d.id; });

		// Add the links
		path = vis.selectAll(".link")
			.data(links, function(d) { return d.id; });

		path.enter().append("g")
        	.each(function (d) { d.createVisualElement(this, graph); });

		path.exit().remove();

		// Build the visibleNodes
	    node = vis.selectAll(".node")
	        .data(visibleNodes, function(d) { return d.id; });

	    node.enter().append("g")
	        .each(function (d) { d.createVisualElement(this, graph); });

        node.exit().remove();


        if(nodesToUpdate != null) {
            // Define the tick function
            this.force
                .nodes(nodesToUpdate)
                .on("tick", this.tick)
                .alpha(0)
                .restart();
        }

        else {
            this.force
                .nodes(visibleNodes)
                .on("tick", this.tick)
                .alpha(1)
                .restart();
        }


        $(this).triggerHandler("postupdate");
        if (graph.fixedMode) {
            var delay = 5000;
            if (!graph.delayfixonupdate) {
                delay = 10;
            }
            setTimeout(function() {
                graph.pause();
            }, delay);
        }
        graph.delayfixonupdate = true;

        d3.selection.prototype.moveToFront = function() {
            return this.each(function(){
                this.parentNode.appendChild(this);
            });
        };
        d3.selection.prototype.moveToBack = function() {
            return this.each(function() {
                var firstChild = this.parentNode.firstChild;
                if (firstChild) {
                    this.parentNode.insertBefore(this, firstChild);
                }
            });
        };

		vis.selectAll('.dependencyNode').moveToFront();
        vis.selectAll('.submodelNode').moveToBack();
        vis.selectAll('.hullOpen').moveToBack();
        vis.selectAll('.modelNode').moveToBack();

    };

	// Brute force redraw
	// Motivation:
	//	The z-index in SVG relies on the order of elements in html.
	//	The way things are added using D3, we don't have much control
	//	over ordering when we're adding and removing element dynamically.
	//	So to get control back we remove everything and redraw everything from scratch
	var refreshing = false;
	var bruteForceRefresh = function () {
		if(refreshing)
			return;

		$('#doodads').empty();
		refreshing = true;
		//Remove all graph objects
		vis.selectAll("*").remove();
		// Remove all visibleNodes from the graph
		visibleNodes.length = 0;

		// Remove all links from the graph
		this.removeAllLinks();

		// Add the visibleNodes back
		for (var x in nodes) {
			nodes[x].globalApplyUntilTrue(function(d) {
				if (d.isVisible()) {
					visibleNodes.push(d);
				}
				if (!d.canlink && !d.showchildren) return true;
				var nodelinks = d.getLinks(links);
				if (nodelinks) {
					nodelinks.forEach(function(l) {
						links.push(l);
					});
				}
				else {
					d.charge = d.defaultcharge/3;
				}
				return false;
			});
		}

		svg.append('text')
		   .attr("y", 0)
		   .attr("x", 0)
		   .attr('text-anchor', 'middle')
		   .attr("class", "myLabel")
		   .text("Hello");

		refreshing = false;
	}


	this.tick = function () {
        graph.hoverPause();

        // Execute the tick handler for each link
		path.enter().each(function (d) {
			d.tickHandler(this, graph);
		})

    	// Execute the tick handler for each node
    	node.enter().each(function (d) {
    		d.tickHandler(this, graph);
    	});

    	ghost.enter().each(function (d) {
    		d.tickHandler(this, graph);
    	});
	};


	this.clearTemporaryObjects = function() {
		vis.selectAll(".ghost").remove();
	    ghost = vis.selectAll(".ghost")
	    	.data([], function(d) { return d.id; });
	}

	// Find a node by its id
	this.findNode = function(id) {
		var nodewithid = null;
		for (var x in nodes) {
			nodes[x].globalApplyUntilTrue(function(n) {
				if (n.id == id) {
					nodewithid = n;
					return true;
				}
				return false;
			});
		}
	   return nodewithid;
	};

	// Find a visible node by its id
	this.findVisibleNode = function(id) {
	    for (var i in visibleNodes) {
	        if (visibleNodes[i].id === id)
	        	return visibleNodes[i];
	    }
	};

	// Find a link by its id
	this.findLink = function(id) {
		for (var i in links) {
			if (links[i].id === id)
				return links[i];
		}
	}

	// Highlight a node, its links, link labels, and the visibleNodes that its linked to
	this.highlightMode = function (highlightNode) {
		// Remove any existing dim assignments
		vis.selectAll(".node, .link").each(function (d) {
			d3.select(this).classed("dim", false);

			// Replace label with display name
			d3.select(this).selectAll("text")
				.text(function(d) {
					return d.displayName;
				})
		});

		// If no node was passed in there's nothing to dim
		if(!highlightNode)
			return;

		// Get all the visibleNodes that need to be highlighted
		// and dim all the links and labels that need to be dimmed
		var nodesToHighlight = {};
		nodesToHighlight[highlightNode.index] = 1;
		vis.selectAll(".link").each(function (d) {
			if(d.source == highlightNode || d.target == highlightNode) {
				nodesToHighlight[d.source.index] = 1;
				nodesToHighlight[d.target.index] = 1;

				// Display full name for highlighted links
				d3.select(this).selectAll("text")
					.text(function(d) {
						return d.name;
					})

				return;
			}

			// Dim the link since it's not connected to the node we care about
			d3.select(this).classed("dim", true);
		});

		// Dim all the visibleNodes that need to be dimmed
		vis.selectAll(".node").each(function (d) {
			if(!nodesToHighlight[d.index])
				d3.select(this).classed("dim", true);
			else
				// Display full name for highlighted visibleNodes
				d3.select(this).selectAll("text")
					.text(function(d) {
						return d.name;
					})
		});
	};

	// Set the graph's width and height
	this.updateHeightAndWidth = function () {
		this.w = $(window).width();
		this.h = $(window).height();
		svg.attr("width", this.w)
	    	.attr("height", this.h);

		vis.attr("width", this.w)
    	.attr("height", this.h);

	}

	this.setNodeCharge = function(charge) {
		if (isNaN(charge)) return;
		this.force.stop();
		this.nodecharge = charge;
		for (n in nodes) {
			nodes[n].applytoChildren(function(d) {
				d.charge = charge;
				d.defaultcharge = charge;
			});
		};

		this.update();

	}

	this.setLinkLength = function(length) {
		if (isNaN(length)) return;
		graph.linklength = length;
		for (l in links) {
			links[l].length = length;
		};

		this.update();

	}

	this.setChargeDistance = function(dist) {
		if (isNaN(dist)) return;
		this.force.force("charge").distanceMax(dist);

		this.update();

	}

	this.toggleFixedMode = function(setfixed) {
		this.fixedMode = setfixed;

		if (setfixed) {
			this.pause();
		}
		else {
		    this.resume();
		    this.tick();
		}

	}

	this.toggleGravity = function(enabled) {
		if (enabled) {
			// Find the new center of graph based on zoom/pan transform
            var string = $(".canvas").attr("transform");
            if(string === undefined) {
            	// Use default center if zoom/pan is not applied
                this.force.force("center", d3.forceCenter(this.w/2, this.h/2));
            }
            else {
                var translate = string.substring(string.indexOf("(") + 1, string.indexOf(")")).split(",");
                var scaleStr = string.substring(string.lastIndexOf("(") + 1, string.lastIndexOf(")"));
                var dx = Number(translate[0]), dy = Number(translate[1]), scale = 1/Number(scaleStr);
                var newCenterX = (this.w/2 - dx)*scale;
                var newCenterY = (this.h/2 - dy)*scale;
                this.force.force("center", d3.forceCenter(newCenterX, newCenterY));
            }
		}
		else {
			this.force.force("center", null);
		}
	}

	this.setFriction = function(friction) {
		this.force.velocityDecay(friction);
	}

	//Get the coordinates for the center of the graph
	this.getCenter = function() {
		return [this.w/2, this.h/2];
	}

    this.hoverPause = function() {
        // Pauses graph when hovering over a node
            if(!graph.fixedMode) {
            $(".node > *:not(.hull)").hover(function () {
                graph.pause();
            }, function () {
                if (!graph.fixedMode && $(".node > *:not(.hull):hover").length == 0) {
                	graph.force.restart();
                }
            });
		} else {
            $(".node > *:not(.hull)").off('mouseenter mouseleave');
		}
    }

    this.paused = false;

	this.pause = function() {
		this.force.stop();
		graph.paused = true;
	}
	this.resume = function() {
		this.force
    	.alpha(1)
    	.restart();
		graph.paused = false;
	}



	this.isMac = navigator.userAgent.indexOf('Mac OS X') != -1;
	this.cntrlIsPressed = false;
	this.shiftIsPressed = false;
	this.cntrlIsPressedOnMac = false;

	//Bind keyboard events
	$(document).keyup(function(event){
		if(graph.isMac) {
            if (event.which == "91" || event.which == "93") { //left command and right command
                graph.cntrlIsPressed = false;
            }
            if (event.which == "17") { //control
                graph.cntrlIsPressedOnMac = false;
            }
        }
		else {
            if (event.which == "17") { //control
                graph.cntrlIsPressed = false;
            }
        }
        if (event.which == "16") { //shift
            graph.shiftIsPressed = false;
            $('#stage').css('cursor', graph.defaultcursor);
        }

        if (event.which == "32") { //space
            if (!$(event.target).closest(".searchString").length) {
                graph.fixedMode = !graph.fixedMode;
                $("#fixedNodes").attr('checked', graph.fixedMode);
                if (graph.fixedMode)
                    graph.pause();
                else graph.resume();
            }
        }
    });


	$(document).keydown(function(event){
        if(graph.isMac) {
            if(event.which == "91" || event.which == "93") { //left command and right command
                graph.cntrlIsPressed = true;
            }
            if(event.which=="17") { //control
                graph.cntrlIsPressedOnMac = true;
            }
        }
        else {
            if(event.which=="17") //control
                graph.cntrlIsPressed = true;
        }
        if(event.which=="16") { //shift
			graph.shiftIsPressed = true;
			$('#stage').css('cursor', 'crosshair');
        }
	});

	this.updateHeightAndWidth();
	// Run it
	this.update();
}
