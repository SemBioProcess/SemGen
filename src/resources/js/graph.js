/**
 * Defines graph functionality
 * 
 * Adapted from: http://stackoverflow.com/questions/11400241/updating-links-on-a-force-directed-graph-from-dynamic-json-data
 */

function Graph() {
	// Add a node to the graph
	this.addNode = function (id) {
	    nodes.push({
	    	"id": id,
	    	"fixed": true,
	    });
	    
	    update();
	};

	// set up the D3 visualisation in the specified element
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

	var nodes = force.nodes(),
	    links = force.links();

	/** 
	 * Updates the graph
	 */
	var update = function () {
		var link = vis.selectAll("line")
			.data(links);

	    link.enter().append("line")
	        .attr("class","link");	

	    link.exit().remove();
	    
	    var node = vis.selectAll("g.node")
	        .data(nodes, function(d) { return d.id; });

	    var nodeEnter = node.enter().append("g")
	        .attr("class", "node")
	        .call(force.drag);

	    nodeEnter.append("svg:circle")
		    .attr("r", 16)
		    .attr("id",function(d) { return "Node;"+d.id;})
		    .attr("class","nodeStrokeClass");

	    nodeEnter.append("svg:text")
		    .attr("class","text")
		    .attr("x", 20)
            .attr("y", ".31em")
		    .text( function(d) { return d.id; }) ;

	    node.exit().remove();
	    
	    force.on("tick", function() {

	        node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y         + ")"; });

	        link.attr("x1", function(d) { return d.source.x; })
	          .attr("y1", function(d) { return d.source.y; })
	          .attr("x2", function(d) { return d.target.x; })
	          .attr("y2", function(d) { return d.target.y; });
	    });

	    // Restart the force layout.
	    force
		    .gravity(.05)
		    .distance(50)
		    .linkDistance( 50 )
		    .size([w, h])
		    .start();
	};

	// Run it
	update();
}
