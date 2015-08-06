/**
 * Represents a mediator link for a PhysioMap in the d3 graph 
 */
 
 function MediatorLink(graph, parent, inputNode, outputLink, length, type) {
	 if(!graph) return;
	 
	 this.graph = graph;
	 this.className = "link mediator";
	 this.id = inputNode.id + "-" + outputLink.id;
	 this.source = inputNode;
	 this.target = outputLink;
	 this.length = length;
	 this.value = 1;
	 this.type = type;
	 
 }
 
 MediatorLink.prototype.createVisualElement = function (element, graph) {
	this.rootElement = d3.select(element);
	
	this.rootElement.attr("class", this.className);
	
	this.rootElement.append("svg:path")
			.attr("id", this.source.id + "-" + this.target.name)
			.attr("class", "link ")

 }
 
 MediatorLink.prototype.tickHandler = function (element , graph) {
	 // Display and update links
	var root = d3.select(element);
	root.select("path").attr("d", function(d) {
    	    var targetX = d.target.source.x + (d.target.target.x - d.target.source.x)/2,
				targetY = d.target.source.y + (d.target.target.y - d.target.source.y)/2,
				dx = targetX - d.source.x,
    	        dy = targetY - d.source.y,
    	        dr = 0,										// Lines have no arc
    	        theta = Math.atan2(dy, dx) + Math.PI * 2,
    	        d90 = Math.PI / 2,
    	        dtxs = targetX - d.target.target.r * Math.cos(theta),
    	        dtys = targetY - d.target.target.r * Math.sin(theta),
    	        arrowHeadWidth = 5;
				
    	    return "M" + d.source.x + "," + d.source.y +
    	    		"A" + dr + "," + dr + " 0 0 1," + targetX + "," + targetY +
    	    		"A" + dr + "," + dr + " 0 0 0," + d.source.x + "," + d.source.y +
    	    		"M" + dtxs + "," + dtys + "l" + (arrowHeadWidth * Math.cos(d90 - theta) - 10 * Math.cos(theta)) + "," + (-arrowHeadWidth * Math.sin(d90 - theta) - 10 * Math.sin(theta)) +
    	    		"L" + (dtxs - arrowHeadWidth * Math.cos(d90 - theta) - 10 * Math.cos(theta)) + "," + (dtys + arrowHeadWidth * Math.sin(d90 - theta) - 10 * Math.sin(theta)) +
    	    		"z";
    	});
 }