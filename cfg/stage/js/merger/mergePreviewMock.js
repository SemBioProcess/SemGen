/**
 * Created by graham_kim on 3/23/16.
 * Adapted from Ryan's multGraphTest.html

 */

function PreviewGraph(id) {
	var selector = "#" + id;
	var nullmodel;
	var div = $(selector);
	var graph = this;
	var svg;
	
    this.nodecharge = -120;
    this.linklength = 120;
    
    var color = d3.scale.category10();
    var svg = d3.select(selector)
    	.append("svg");

    svg.id = "svg" + id;
    this.force = d3.layout.force()
	    .charge(function (d) { return d.charge; })
		.linkDistance(function (d) { return d.length; });
    
    var links = this.force.links();
    var nodes = this.force.nodes();
    
    this.initialize = function () {
	    graph.w = div.width();
	    graph.h = div.height();
	    nullmodel = new ModelNode(graph, "null");
	    svg.attr("width", graph.w)
	       .attr("height", graph.h);
    }
    
    var path;
    var node;
    
    this.update = function(data) {
    	$(this).triggerHandler("preupdate");
    	svg.selectAll("*").remove();
    	nodes.length = 0;
    	links.length = 0;
    	
    	var smnodes = [];
    	data.childsubmodels.forEach(function(sm) {
    		var smnode = new SubmodelNode(graph, sm, nullmodel);
    		
    		smnode.x = Math.random() * graph.w;
			smnode.y = Math.random() * graph.h;
			smnodes.push(smnode);
			nodes.push(smnode);
			smnode.createChildren(sm.dependencies, function (data) {
    			var dnode = new DependencyNode(smnode.graph, data, smnode);
    			dnode.id = data.id;
    			nodes.push(dnode);
				return dnode;
			});
			
    	}); 
	
		nodes.forEach(function (n) {		
			n.isVisible = function() {return true;};
			var nodelinks = n.getLinks();
			if (nodelinks) {
				nodelinks.forEach(function(l){
					//l.id = l.id + graph.id;
					links.push(l);
				});
			}
		});
		
		// Add the links
		path = svg.selectAll("g.link")
			.data(links, function(d) { return d.id; });
	
		path.enter().append("g")
	    	.each(function (d) { d.createVisualElement(this, graph); });
	
		path.exit().remove();
	
		// Build the visibleNodes
		node = svg.selectAll("node")
	        .data(nodes, function(d) { return d.id; });
	
	    node.enter().append("g")
	        .each(function (d) { d.createVisualElement(this, graph); });
	
	    node.exit().remove();
	    
	    smnodes.forEach(function(sn) {
	    	$(sn).triggerHandler('childrenSet', [sn.children]);
	    	sn.lockhull = true;
	    });
	    
	    this.force.on("tick", this.tick);
	    
	    graph.force.size([graph.w, graph.h])
        	.start();
	    $(this).triggerHandler("postupdate");
    }
    
    this.tick = function () {
	    		path.each(function (d) {
	    			
	    			d.tickHandler(this, graph);
	    		})
	
	        	// Execute the tick handler for each node
	        	node.each(function (d) {
	        		d.tickHandler(this, graph);
	        	});
	    	};
    
    this.highlightMode = function (highlightNode) {}
    
	// Find a node by its id
	this.findVisibleNode = function(id) {
	    for (var i in nodes) {
	        if (nodes[i].id === id)
	        	return nodes[i];
	    }
	};
}