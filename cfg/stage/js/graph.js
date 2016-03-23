/**
 * Defines graph functionality
 *
 * Adapted from: http://stackoverflow.com/questions/11400241/updating-links-on-a-force-directed-graph-from-dynamic-json-data
 */


function Graph() {
	var graph = this;

	// Get the stage and style it
	var svg = d3.select("#stage")
	    .append("svg:svg")
	    .attr("id", "svg")
	    .attr("pointer-events", "all")
	    .attr("perserveAspectRatio", "xMinYMid");

	var vis = svg.append('svg:g');

	this.force = d3.layout.force()
		.gravity(0)
		.chargeDistance(250)
		.friction(0.7)
		.charge(function (d) { return d.charge; })
	    .linkDistance(function (d) { return d.length; });
	this.color = d3.scale.category10();

	this.displaymode = DisplayModes.SHOWSUBMODELS;
	this.fixedMode = false;
		//Node type visibility: model, submodel, state, rate, constitutive, entity, process, mediator, null
	this.nodesVisible = [true, true, true, true, false, true, true, true, true];

	var nodes;
	var visibleNodes = this.force.nodes();
	var links = this.force.links();
	var orphanNodes = [];

	var hiddenLinks = {};

	this.setTaskNodes = function(tasknodes) {
		nodes = tasknodes;
		graph.update();
	};

	this.getVisibleNodes = function() {
		return visibleNodes;
	}

	// Add a link to the graph
	this.addLink = function (link) {
		// If the link already exists don't add it again
		if(this.findLink(link.id)) {
			console.log("link already exists. Link id: " + link.id);
			return;
		}

		links.push(link);
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

	// Remove a link from the graph
	this.removeLink = function(id) {
		var link = this.findLink(id);

		// If we did not find the link it must be hidden
		if(!link) {
			// Remove it from the hidden list
			for(type in hiddenLinks)
				for(var i = 0; i < hiddenLinks[type].length; i++)
					if(hiddenLinks[type][i].id == id) {
						hiddenLinks[type].splice(i, 1);
						return;
					}
			return;
		}
		links.splice(findLinkIndex(id), 1);
	};

	// Remove all links
	this.removeAllLinks = function(){
	    links.splice(0,links.length);
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
		this.update();
	}

	// Hide all visibleNodes of the given type
	this.hideNodes = function (type) {
		this.nodesVisible[NodeTypeMap[type].id] = false;
		this.update();
	}



	// Hide all links of the give type
	this.hideLinks = function (type) {
		var linksToHide = [];
		links.forEach(function (link) {
			if(link.linkType == type) {
				linksToHide.push(link);
				link.hidden = true;
			}
		});

		// Remove the hidden links from the graph
		linksToHide.forEach(function (link) {
			this.removeLink(link.id);
			this.hideOrphanNodes(link);
		}, this);

		if(!hiddenLinks[type])
			hiddenLinks[type] = [];

		// Save the hidden links in case we want to add them back
		hiddenLinks[type] = hiddenLinks[type].concat(linksToHide);

		this.update();
	}

	// Hide visibleNodes without links
	this.hideOrphanNodes = function (linkToHide) {
		var nodesWithLink = [];
		var nodesFromLinkToHide = [];

		nodesFromLinkToHide.push(linkToHide.source);
		nodesFromLinkToHide.push(linkToHide.target);

		nodesFromLinkToHide.forEach(function (node) {
			// Model visibleNodes and hiddenLabel visibleNodes don't count as orphan visibleNodes
			if(node.nodeType === undefined || node.nodeType == "Model") {
				return;
			}

			for(var i = 0; i < links.length; i++) {
				if(nodesWithLink.indexOf(links[i].source.id) == -1) {
					nodesWithLink.push(links[i].source.id);
				}
				if(nodesWithLink.indexOf(links[i].target.id) == -1) {
					nodesWithLink.push(links[i].target.id);
				}
			}

			if(nodesWithLink.indexOf(node.id) == -1) {
				orphanNodes.push(node);
				this.removeNode(node.id);
			}
		}, this);
	}

	// Add orphan visibleNodes back
	this.showOrphanNodes = function () {
		orphanNodes.forEach(function (node) {
			this.addNode(node);
		}, this);
	}

	// Show all links of the given type
	this.showLinks = function (type) {
		if(!hiddenLinks[type])
			return;

		// Add all links back to the graph
		hiddenLinks[type].forEach(function (link) {
			link.hidden = false;
			this.addLink(link);
		}, this);

		// These links are no longer hidden
		delete hiddenLinks[type];

		// Add orphan visibleNodes back
		this.showOrphanNodes();

		this.update();
	}

	// Get an array of the hidden links
	this.getHiddenLinks = function () {
		var hiddenLinksArr = [];
		for(type in hiddenLinks)
			hiddenLinksArr = hiddenLinksArr.concat(hiddenLinks[type]);

		return hiddenLinksArr;
	}

	/**
	 * Updates the graph
	 */
	var path;
	var node;
	this.update = function () {
		$(this).triggerHandler("preupdate");

		bruteForceRefresh.call(this);

		// Add the links
		path = vis.selectAll("g.link")
			.data(links, function(d) { return d.id; });

		path.enter().append("g")
        	.each(function (d) { d.createVisualElement(this, graph); });

		path.exit().remove();

		// Build the visibleNodes
	    node = vis.selectAll("g.node")
	        .data(visibleNodes, function(d) { return d.id; });

	    node.enter().append("g")
	        .each(function (d) { d.createVisualElement(this, graph); });

	    node.exit().remove();

	    // Define the tick function
	    this.force.on("tick", this.tick);

	    // Restart the force layout.
	    this.force
		    .size([this.w, this.h])
		    .start();


	    $(this).triggerHandler("postupdate");

	    // This is for new visibleNodes. We need to set them to fixed if we're in fixed mode
	    // There's a delay so the forces can equalize
	    setTimeout(function () {
	    	if(this.fixedMode)
	    		visibleNodes.forEach(toggleFixedMode);
	    }, 7000);
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

		refreshing = true;
		vis.selectAll("*").remove();
		// Remove all visibleNodes from the graph
		visibleNodes.length = 0;

		// Remove all links from the graph
		this.removeAllLinks();


		var node;
		// Add the visibleNodes back
		for (var x in nodes) {
			node = nodes[x];
			node.globalApply(function(d) {
				if (d.isVisible()) {
					visibleNodes.push(d);
				}
			});
		}

		// Process links for each node
		visibleNodes.forEach(function (n) {
			var nodeLinks = n.getLinks();

			// If the node doesnt have any links move on
			if(!nodeLinks)
				return;

			nodeLinks.forEach( function (link) {
				if (!link.source.hidden && !hiddenLinks[link.linkType]) {
					links = links.concat(link);
				}
			});

		}, this);

		this.force.links(links);

		refreshing = false;
	}


	this.tick = function () {
		// Execute the tick handler for each link
		path.each(function (d) {
			d.tickHandler(this, graph);
		})

    	// Execute the tick handler for each node
    	node.each(function (d) {
    		d.tickHandler(this, graph);
    	});
	};

	// Find a node by its id
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
	    	.attr("height", this.h)
	    	.attr("viewBox","0 0 "+ this.w +" "+ this.h)
	}

	// Find a node's index
	var findNodeIndex = function(id) {
		for (var i in visibleNodes) {
	        if (visibleNodes[i].id == id)
	        	return i;
		};
	};

	// Find a link's index
	var findLinkIndex = function(id) {
		for (var i in links) {
			if (links[i].id == id)
				return i;
		}
	}

	this.setNodeCharge = function(charge) {
		if (isNaN(charge)) return;
		visibleNodes.forEach(function(node) {
			if(node.nodeType != "Model") {
				node.charge = charge;
			}
		});
		defaultcharge = charge;
		this.update();

	}

	this.toggleFixedMode = function(setfixed) {
		fixedMode = setfixed;
		visibleNodes.forEach(setFixed);
		this.update();
	}

	var setFixed = function (node) {
		node.oldFixed = node.fixed;
		node.fixed = fixedMode;
	};

	var resetFixed = function () {
		node.fixed = node.oldFixed || false;
		node.oldFixed = undefined;
		this.update();
	};

	this.toggleGravity = function(enabled) {
		if (enabled) {
			this.force.gravity(1.0);
		}
		else {
			this.force.gravity(0.0);
		}
		this.update();

	}

	this.setFriction = function(friction) {
		this.force.friction(friction);
	}

	this.updateHeightAndWidth();
	// Run it
	this.update();
}
