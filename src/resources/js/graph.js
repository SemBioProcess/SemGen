/**
 * Defines graph functionality
 * 
 * Adapted from: http://stackoverflow.com/questions/11400241/updating-links-on-a-force-directed-graph-from-dynamic-json-data
 */

function Graph() {

	// Add a node to the graph
	this.addNode = function (nodeData) {
		if(!nodeData)
			throw "Invalid node data";
		
		if(typeof nodeData.id != "string")
			throw "Node id must be a string";
		
		if(typeof nodeData.r != "number")
			throw "Node radius must be a number";
		
		if(typeof nodeData.className != "string")
			throw "Node className must be a string";
		
		// If the node already exists don't add it again
		if(findNode(nodeData.id)) {
			console.log("node already exists. Node id: " + nodeData.id);
			return;
		}
		
		nodes.push(nodeData);
		newNodes.push(nodeData);
	};
	
	// Remove a node from the graph
	this.removeNode = function (id) {
		var i = 0;
	    var node = findNode(id);
	    while (i < links.length) {
	    	if ((links[i].source == node)||(links[i].target == node))
	            links.splice(i,1);
	        else
	        	i++;
	    }
	    nodes.splice(findNodeIndex(id),1);
	    this.update();
	};

	/** 
	 * Updates the graph
	 */
	this.update = function () {
		// Add new links to the graph
		newNodes.forEach(function (nodeData) {
			// If the node doesnt have any links move on
			if(!nodeData.links)
				return;
			
			// Add each link to our array
			nodeData.links.forEach(function (targetId) {
				// Try and find the target node
				var target = findNode(targetId);
				
				// If the target doesn't exist ignore the link
				if(!target) {
					console.log("target node '" + targetId + "' does not exist. Can't build link.");
					return;
				}
				
				links.push({
					source: nodeData,
					target: target,
					value: 1,
				});
			});
		});
		
		// These are no longer new
		newNodes = [];

		// Add the links
		var path = vis.selectAll("svg > g > path")
			.data(links, function(d) { return d.source.id + "-" + d.target.id; });
		
		path.enter().append("svg:path")
			.attr("id",function(d){return d.source.id + "-" + d.target.id;})
			.attr("class", "link");
	    
		path.exit().remove();
		
		// Build the nodes
	    var node = vis.selectAll("g.node")
	        .data(nodes, function(d) { return d.id; });

	    var graph = this;
	    var nodeEnter = node.enter().append("g")
	        .each(function (d) { d.createVisualElement(this, graph); });
	    
	    node.exit().remove();
	    
	    // Define the tick function
	    this.force.on("tick", function() {
	    	// Display the links
	    	path.attr("d", function(d) {
	    	    var dx = d.target.x - d.source.x,
	    	        dy = d.target.y - d.source.y,
	    	        dr = 0,										// Lines have no arc
	    	        theta = Math.atan2(dy, dx) + Math.PI * 2,
	    	        d90 = Math.PI / 2,
	    	        dtxs = d.target.x - d.target.r * Math.cos(theta),
	    	        dtys = d.target.y - d.target.r * Math.sin(theta);
	    	    return "M" + d.source.x + "," + d.source.y +
	    	    		"A" + dr + "," + dr + " 0 0 1," + d.target.x + "," + d.target.y +
	    	    		"A" + dr + "," + dr + " 0 0 0," + d.source.x + "," + d.source.y +
	    	    		"M" + dtxs + "," + dtys + "l" + (3.5 * Math.cos(d90 - theta) - 10 * Math.cos(theta)) + "," + (-3.5 * Math.sin(d90 - theta) - 10 * Math.sin(theta)) +
	    	    		"L" + (dtxs - 3.5 * Math.cos(d90 - theta) - 10 * Math.cos(theta)) + "," + (dtys + 3.5 * Math.sin(d90 - theta) - 10 * Math.sin(theta)) +
	    	    		"z";
	    	  });
	    	
	    	// Execute the tick handler for each node
	    	node.each(function (d) {
	    		d.tickHandler(this, graph);
	    	});
	    });

	    // Restart the force layout.
	    this.force
	    	.gravity(0)
	    	.linkDistance(60)
		    .size([this.w, this.h])
		    .start();
	};
	
	// Set the graph's width and height
	this.w = $(window).width();
	this.h = $(window).height();
	
	// Find a node by its id
	var findNode = function(id) {
	    for (var i in nodes) {
	        if (nodes[i].id === id) return nodes[i];};
	};
	
	// Find a node's index
	var findNodeIndex = function(id) {
		for (var i in nodes) {
	        if (nodes[i].id == id)
	        	return i;
		};
	};
	
	// Get the stage and style it
	var vis = d3.select("#stage")
	    .append("svg:svg")
	    .attr("id", "svg")
	    .attr("pointer-events", "all")
	    .attr("width", this.w)
	    .attr("height", this.h)
	    .attr("viewBox","0 0 "+ this.w +" "+ this.h)
	    .attr("perserveAspectRatio", "xMinYMid")
	    .append('svg:g');

	this.force = d3.layout.force();
	this.color = d3.scale.category10();
	var nodes = this.force.nodes(),
		links = this.force.links();
	var newNodes = [];
	
	// Fix all nodes when ctrl + M is pressed
	d3.select("body")
		.on("keydown", function() {
			if(d3.event.keyCode == 77 /*M*/ && d3.event.ctrlKey) {
				var body = $("body").toggleClass("fixedMode");
				
				// Fix all nodes
				if(body.hasClass("fixedMode"))
				{
					nodes.forEach(function (d) {
						d.oldFixed = d.fixed;
						d.fixed = true;
					});
				}
				// Un-fix all nodes
				else
				{
					nodes.forEach(function (d) {
						d.fixed = d.oldFixed || false;
						d.oldFixed = undefined;
					});
				}
				
			}
	    });
	
	// Run it
	this.update();
}