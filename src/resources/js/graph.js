/**
 * Defines graph functionality
 * 
 * Adapted from: http://stackoverflow.com/questions/11400241/updating-links-on-a-force-directed-graph-from-dynamic-json-data
 */

function Graph() {
	var graph = this;
	
	// Add a node to the graph
	this.addNode = function (nodeData) {
		if(!nodeData)
			throw "Invalid node data";
		
		if(typeof nodeData.id != "string")
			throw "Node id must be a string";
		
		if(typeof nodeData.r != "number")
			throw "Node radius must be a number";
		
		if(typeof nodeData.charge != "number")
			throw "Charge must be a number";
		
		if(typeof nodeData.getLinks != "function")
			throw "Node getLinks is not defined";
		
		if(typeof nodeData.createVisualElement != "function")
			throw "Node createVisualElement is not defined";
		
		if(typeof nodeData.tickHandler != "function")
			throw "Node tickHandler is not defined";
		
		// If the node already exists don't add it again
		if(this.findNode(nodeData.id)) {
			console.log("node already exists. Node id: " + nodeData.id);
			return;
		}
		
		nodes.push(nodeData);
		$(this).triggerHandler("nodeAdded", [nodeData]);
	};
	
	// Remove a node from the graph
	this.removeNode = function (id) {
		var i = 0;
	    var node = this.findNode(id);
	    
	    // If we did not find the node it must be hidden
	    if(!node) {
	    	// Remove if from the hidden list
	    	for(type in hiddenNodes)
	    		for(var i = 0; i < hiddenNodes[type].length; i++)
	    			if(hiddenNodes[type][i].id == id) {
	    				hiddenNodes[type].splice(i, 1);
	    				return;
	    			}
	    	
	    	return;
	    }
	    
	    while (i < links.length) {
	    	if ((links[i].source == node)||(links[i].target == node))
	            links.splice(i,1);
	        else
	        	i++;
	    }
	    nodes.splice(findNodeIndex(id),1);
	    $(this).triggerHandler("nodeRemoved", [node]);
	};
	
	// Remove all nodes
	this.removeAllNodes = function(){
	    nodes.splice(0,nodes.length);
	};
	
	// Remove all links
	this.removeAllLinks = function(){
	    links.splice(0,links.length);
	};
	
	// Check to see if there's at least 1 node of the given type
	this.hasNodeOfType = function (type) {
		for(var index = 0; index < nodes.length; index++) {
			if(nodes[index].nodeType == type)
				return true;
		}
		
		return false;
	};
	
	// Hide all nodes of the given type
	this.hideNodes = function (type) {
		var nodesToHide = [];
		nodes.forEach(function (node) {
			if(node.nodeType == type) {
				nodesToHide.push(node);
				node.hidden = true;
			}
		});
		
		// Remove the hidden nodes from the graph
		nodesToHide.forEach(function (node) {
			this.removeNode(node.id);
		}, this);
		
		if(!hiddenNodes[type])
			hiddenNodes[type] = [];
		
		// Save the hidden nodes in case we want to add them back
		hiddenNodes[type] = hiddenNodes[type].concat(nodesToHide);
		this.update();
	}
	
	// Show all nodes of the given type
	this.showNodes = function (type) {
		if(!hiddenNodes[type])
			return;
		
		// Add all nodes back to the graph
		hiddenNodes[type].forEach(function (node) {
			node.hidden = false;
			this.addNode(node);
		}, this);
		
		// These nodes are no longer hidden
		delete hiddenNodes[type];
		
		this.update();
	}
	
	// Get an array of the hidden nodes
	this.getHiddenNodes = function () {
		var hiddenNodesArr = [];
		for(type in hiddenNodes)
			hiddenNodesArr = hiddenNodesArr.concat(hiddenNodes[type]);
		
		return hiddenNodesArr;
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
		path = vis.selectAll("svg > g > path")
			.data(links, function(d) { return d.source.id + "-" + d.target.id; });
		
		path.enter().append("svg:path")
			.attr("id",function(d){return d.source.id + "-" + d.target.id;})
			.attr("class", function(d) { return "link " + d.type; });
	    
		path.exit().remove();
		
		// Build the nodes
	    node = vis.selectAll("g.node")
	        .data(nodes, function(d) { return d.id; });

	    var nodeEnter = node.enter().append("g")
	        .each(function (d) { d.createVisualElement(this, graph); });
	    
	    node.exit().remove();
	    
	    // Define the tick function
	    this.force.on("tick", this.tick);
	    
	    // Restart the force layout.
	    this.force
	    	.charge(function (d) { return d.charge; })
	    	.linkDistance(function (d) { return d.length; })
		    .size([this.w, this.h])
		    .start();
	    
	    $(this).triggerHandler("postupdate");
	    
	    // This is for new nodes. We need to set them to fixed if we're in fixed mode
	    // There's a delay so the forces can equalize
	    setTimeout(function () {
	    	if(fixedMode)
	    		nodes.forEach(setFixed);
	    }, 7000);
	};
	
	this.tick = function () {
    	// Display the links
    	path.attr("d", function(d) {
    	    var dx = d.target.x - d.source.x,
    	        dy = d.target.y - d.source.y,
    	        dr = 0,										// Lines have no arc
    	        theta = Math.atan2(dy, dx) + Math.PI * 2,
    	        d90 = Math.PI / 2,
    	        dtxs = d.target.x - d.target.r * Math.cos(theta),
    	        dtys = d.target.y - d.target.r * Math.sin(theta),
    	        arrowHeadWidth = 5;
    	    return "M" + d.source.x + "," + d.source.y +
    	    		"A" + dr + "," + dr + " 0 0 1," + d.target.x + "," + d.target.y +
    	    		"A" + dr + "," + dr + " 0 0 0," + d.source.x + "," + d.source.y +
    	    		"M" + dtxs + "," + dtys + "l" + (arrowHeadWidth * Math.cos(d90 - theta) - 10 * Math.cos(theta)) + "," + (-arrowHeadWidth * Math.sin(d90 - theta) - 10 * Math.sin(theta)) +
    	    		"L" + (dtxs - arrowHeadWidth * Math.cos(d90 - theta) - 10 * Math.cos(theta)) + "," + (dtys + arrowHeadWidth * Math.sin(d90 - theta) - 10 * Math.sin(theta)) +
    	    		"z";
    	  });
    	  
    	// Execute the tick handler for each node
    	node.each(function (d) {
    		d.tickHandler(this, graph);
    	});
	};
	
	// Find a node by its id
	this.findNode = function(id) {
	    for (var i in nodes) {
	        if (nodes[i].id === id)
	        	return nodes[i];
	    }
	};
	
	// Highlight a node, its links, and the nodes that its linked to
	this.highlightMode = function (highlightNode) {
		// Remove any existing dim assignments
		vis.selectAll(".node, path.link").each(function (d) {
			d3.select(this).classed("dim", false);
		});
		
		// If no node was passed in there's nothing to dim
		if(!highlightNode)
			return;
		
		// Get all the nodes that need to be highlighted
		// and dim all the links that need to be dimmed
		var nodesToHighlight = {};
		nodesToHighlight[highlightNode.index] = 1;
		vis.selectAll("path.link").each(function (d) {
			if(d.source == highlightNode || d.target == highlightNode) {
				nodesToHighlight[d.source.index] = 1;
				nodesToHighlight[d.target.index] = 1;
				return;
			}
			
			// Dim the link since it's not connected to the node we care about
			d3.select(this).classed("dim", true);
		});
		
		// Dim all the nodes that need to be dimmed
		vis.selectAll(".node").each(function (d) {
			if(!nodesToHighlight[d.index])
				d3.select(this).classed("dim", true);
		});
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
		
		// Remove all nodes from the graph
		var allNodes = nodes.slice(0);
		this.removeAllNodes();
		
		// Remove all links from the graph
		this.removeAllLinks();
		
		// Redraw
		this.update();
		
		// Add the nodes back
		allNodes.forEach(function (node) {
			this.addNode(node);
		}, this);
		
		// Process links for each node
		nodes.forEach(function (node) {
			var nodeLinks = node.getLinks();
			
			// If the node doesnt have any links move on
			if(!nodeLinks)
				return;
			
			// Add the node's links to our master list
			links = links.concat(nodeLinks);
		}, this);
		
		this.force.links(links);
		
		refreshing = false;
	}
	
	// Find a node's index
	var findNodeIndex = function(id) {
		for (var i in nodes) {
	        if (nodes[i].id == id)
	        	return i;
		};
	};
	
	// Get the stage and style it
	var svg = d3.select("#stage")
	    .append("svg:svg")
	    .attr("id", "svg")
	    .attr("pointer-events", "all")
	    .attr("perserveAspectRatio", "xMinYMid");
	
	var vis = svg.append('svg:g');
	
	// Set the graph's width and height
	this.updateHeightAndWidth = function () {
		this.w = $(window).width();
		this.h = $(window).height();
		svg.attr("width", this.w)
	    	.attr("height", this.h)
	    	.attr("viewBox","0 0 "+ this.w +" "+ this.h)
	}
	this.updateHeightAndWidth();

	this.force = d3.layout.force();
	this.color = d3.scale.category10();
	var nodes = this.force.nodes(),
		links = this.force.links();
	var hiddenNodes = {};
	var fixedMode = false;
	
	var setFixed = function (node) {
		node.oldFixed = node.fixed;
		node.fixed = true;
	};
	
	var resetFixed = function (node) {
		node.fixed = node.oldFixed || false;
		node.oldFixed = undefined;
	};
	
	// Fix all nodes when ctrl + M is pressed
	$(".modes .fixedNodes").bind('change', function(){        
		Columns.columnModeOn = this.checked;
		if(this.checked)
		{
			nodes.forEach(setFixed);
			fixedMode = true;
		}
		// Un-fix all nodes
		else
		{
			nodes.forEach(resetFixed);
			fixedMode = false;
		}
	});
	
	// Run it
	this.update();
	
	window.onresize = function () {
		graph.updateHeightAndWidth();
		graph.update();
	};
}
