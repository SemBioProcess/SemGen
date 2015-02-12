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
				links.push({
					source: findNode(nodeData.id),
					target: findNode(targetId),
					value: 1,
				});
			});
		});
		
		// These are no longer new
		newNodes = [];
		
		// Build the arrows
		vis.append("svg:defs").selectAll("marker")
			.data(["end"])      // Different link/path types can be defined here
		  .enter().append("svg:marker")    // This section adds in the arrows
			.attr("id", String)
			.attr("viewBox", "0 -5 10 10")
			.attr("refX", 15)
			.attr("refY", -1.5)
			.attr("markerWidth", 6)
			.attr("markerHeight", 6)
			.attr("orient", "auto")
		  .append("svg:path")
			.attr("d", "M0,-5L10,0L0,5");

		// add the links and the arrows
		var path = vis.append("svg:g").selectAll("path")
			.data(links, function(d) { return d.source.id + "-" + d.target.id; });
		
		path.enter().append("svg:path")
			.attr("id",function(d){return d.source.id + "-" + d.target.id;})
			.attr("class", "link")
			.attr("marker-end", "url(#end)");
	    
		path.exit().remove();
		
		// Build the nodes
	    var node = vis.selectAll("g.node")
	        .data(nodes, function(d) { return d.id; });

	    var nodeEnter = node.enter().append("g")
	        .attr("class", function (d) {return d.className; })
	        .call(force.drag)
	        .style("fill", function (d) {return color(d.group);});;

	    nodeEnter.append("svg:circle")
		    .attr("r", function(d) { return d.r; })
		    .attr("id",function(d) { return "Node;"+d.id;})
		    .attr("class","nodeStrokeClass");

	    nodeEnter.append("svg:text")
		    .attr("class","text")
		    .attr("x", 20)
            .attr("y", ".31em")
		    .text( function(d) { return d.id; }) ;

	    // 1) Draw a box behind each text node
	    // 2) Initialize each node
	    node.each(function (d) {
	    	// Get the node's text element
			var textElement = $(this).find("text")[0];
			
			// Get the box defining the text element
			var textBox = textElement.getBBox();
			
			// Create the rectangle
			var rect = document.createElementNS("http://www.w3.org/2000/svg", "rect");
			rect.setAttribute("x", textBox.x);
			rect.setAttribute("y", textBox.y);
			rect.setAttribute("width", textBox.width);
			rect.setAttribute("height", textBox.height);
			rect.setAttribute("class", "textBackgroundColor");

			// Add the rectangle before the text element
			this.insertBefore(rect, textElement);
			
			// Initialize each node
	    	d.initialize(this);
	    });
	    
	    node.exit().remove();
	    
	    // Define the tick function
	    force.on("tick", function() {
	    	// Display the links
	    	path.attr("d", function(d) {
				var dx = d.target.x - d.source.x,
					dy = d.target.y - d.source.y,
					dr = 0;
					//dr = Math.sqrt(dx * dx + dy * dy); // Use this value to make the links curvy
				return "M" + 
					d.source.x + "," + 
					d.source.y + "A" + 
					dr + "," + dr + " 0 0,1 " + 
					d.target.x + "," + 
					d.target.y;
			});
	    	
	    	// Execute the tick handler for each node
	    	node.each(function (d) {
	    		d.tickHandler(this);
	    	});
	    });

	    // Restart the force layout.
	    force
		    .gravity(.05)
		    .distance(50)
		    .linkDistance( 50 )
		    .size([w, h])
		    .start();
	};
	
	// Find a node by its id
	var findNode = function(id) {
	    for (var i in nodes) {
	        if (nodes[i]["id"] === id) return nodes[i];};
	};
	
	// Set up the D3 visualisation in the specified element
	var w = 500,
	    h = 500;
	
	// Get the stage and style it
	var vis = d3.select("#stage")
	    .append("svg:svg")
	    .attr("width", w)
	    .attr("height", h)
	    .attr("id", "svg")
	    .attr("pointer-events", "all")
	    .attr("viewBox","0 0 "+ w +" "+ h)
	    .attr("perserveAspectRatio", "xMinYMid")
	    .append('svg:g');

	var force = d3.layout.force();
	var color = d3.scale.category10();
	var nodes = force.nodes(),
		links = force.links();
	var newNodes = [];
	
	// Run it
	this.update();
}