/**
 * Created by graham_kim on 3/23/16.
 * Adapted from Ryan's multGraphTest.html

 */

function PreviewGraph(id) {
	var selector = "#" + id;
	var previewmodel;
	var div = $(selector);
	var graph = this;
	var svg;
	var fixedMode = false;
    this.nodecharge = -280;
    this.linklength =420;
	this.depBehaviors = [];
	this.nodesVisible = [true, true, true, true, true, true, true, true, true];

    var color = d3.scaleOrdinal(d3.schemeCategory10);
    var svg = d3.select(selector)
    	.append("svg");

    svg.id = "svg" + id;
    this.force = d3.forceSimulation()
	.velocityDecay(0.6)
	.force("charge", d3.forceManyBody().strength(function(d) {return d.charge;}))
	.force("link", d3.forceLink()
		.distance(function (d) 
				{ return d.length; }
		));
    

    var links = this.force.force("link").links();//.links();
    var nodes = [];
    
    this.initialize = function () {
	    graph.w = div.width();
	    graph.h = div.height();
	    svg.attr("width", graph.w)
	       .attr("height", graph.h);
    }
    
    this.setPreviewData = function(data) {
    	previewmodel = new ModelNode(graph, data);
    	previewmodel.nodeType = NodeType.NULLNODE;
    	previewmodel.showchildren = true;
    	previewmodel.createVisualization(DisplayModes.SHOWSUBMODELS.id, true);
    	this.update();
    }
    var path;
    var node;
    
    this.update = function() {
    	$(this).triggerHandler("preupdate");
    	svg.selectAll("*").remove();
    	nodes.length = 0;
    	links.length = 0;


    	previewmodel.globalApply(function(node) {
    		if (node.nodeType == NodeType.SUBMODEL) {
    			 node.nodecharge = -400;
    			node.textSize = 12;
    			node.showchildren = true;
    			node.fixed = fixedMode;
    		}
    		nodes.push(node);
    	}); 
	
		nodes.forEach(function (n) {	
			n.locked = true;
			n.isVisible = function() {return true;};
			var nodelinks = n.getLinks(links);
			if (nodelinks) {
				nodelinks.forEach(function(l){
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
	    	    
	    this.force
    	.nodes(nodes)
    	
    	.on("tick", this.tick)
    	.alphaTarget(1)
    	.restart();
	    $(this).triggerHandler("postupdate");
    }
    
    this.tick = function () {
	    		path.enter().each(function (d) {
	    			
	    			d.tickHandler(this, graph);
	    		})
	
	        	// Execute the tick handler for each node
	        	node.enter().each(function (d) {
	        		d.tickHandler(this, graph);
	        	});
	    	};
    
    this.highlightMode = function (highlightNode) {}
	
	// Find a node by its id
	this.findNode = function(id) {
		for (i in nodes) {
			if (nodes[i].id == id) {
				return nodes[i];
			}
		}
		return null;
	};

	this.toggleFixedMode = function(setfixed) {

		fixedMode = setfixed;
		for (n in nodes) {
			var d = nodes[n];
			if (setfixed) {
				d.wasfixed = d.fixed;
			}
			d.fixed = setfixed || d.wasfixed;
		}
	};
	
}