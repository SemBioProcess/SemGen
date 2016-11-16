/**
 * Represents a link in the d3 graph
 */
function Link(graph, srclink, output, input, length) {
	if(!graph)
		return;

	this.graph = graph;
	this.srclink = srclink;
	this.id = srclink.id;
	this.source = input;
	this.target = output;
	this.length = length;
	this.bidirectional = false;
	//this.value = 1;
	this.hidden = false;
	this.userCanHide = false;
	this.linkType = NodeTypeArray[srclink.linkType];

	this.arrowHeadWidth = (this.linkType == NodeType.MEDIATOR) ? 0 : 3;
	
	this.getLinkLevel = function() {
		return this.srclink.linklevel;
	}
	
	this.linksNodes = function(innode, outnode) {
		return this.source==innode && this.target == outnode;
	}
	
	this.draw = function (source, target) {
		var arrowHeadWidth = this.arrowHeadWidth;
		var dx = target.xpos() - source.xpos(),
	    dy = target.ypos() - source.ypos(),
	    dr = 0;
	    theta = Math.atan2(dy, dx) + Math.PI * 2,
	    d90 = Math.PI / 2,
	    dtxs = target.xpos() - target.r * Math.cos(theta),
	    dtys = target.ypos() - target.r * Math.sin(theta),
	    cth = 10*Math.cos(theta),
	    sth = 10*Math.sin(theta),
	    c90th = Math.cos(d90 - theta),
	    s90th = Math.sin(d90 - theta);
	       
		return "M" + source.xpos() + "," + source.ypos() +
				"A" + dr + "," + dr + " 0 0 1," + target.xpos() + "," + target.ypos() +
				"A" + dr + "," + dr + " 0 0 0," + source.xpos() + "," + source.ypos() +
				"M" + dtxs + "," + dtys + "l" + (arrowHeadWidth * c90th - cth) + "," + (-arrowHeadWidth * s90th - sth) +
				"L" + (dtxs - arrowHeadWidth * c90th - cth) + "," + (dtys + arrowHeadWidth * s90th - sth) +
				"z";
	}

}

//  Link.prototype.addBehavior = function (behavior) {
// 	// Behaviors are just functions that take in a link as an argument
// 	// To add a behavior all we need to do is call the function
// 	//
// 	// Note: I added this function to make adding a behavior easier to read
// 	// (e.g. this.addBehavior(SomeBehavior); )
// 	behavior(this);
// }

Link.prototype.createVisualElement = function (element, graph) {
	this.rootElement = d3.select(element);

	this.rootElement
			.attr("id", this.id)
			.attr("class", this.className);
			//.attr("class", "link")
	this.rootElement.append("svg:path")
			.attr("class", "link");
	
	//Check if link is intra-submodel
	if (this.srclink.linklevel != 0) {
		this.rootElement.select("path")
			.attr("stroke-dasharray", 3, 6);
	}
	//Intermodel link
	if (this.srclink.linklevel == 2) {
		this.rootElement.select("path")
			.attr("class", "intermodel")
			.attr("stroke-width", "3")
			.attr("stroke", "Cyan");
	}

	//Custom link
	if (this.srclink.linklevel == 3) {
		this.rootElement.select("path")
			.attr("class", "custom")
			.attr("stroke-width", "3")
			.attr("stroke", "Lime");
	}
	
	if (this.linkType == NodeType.MEDIATOR) {
		this.rootElement.select("path")
			.attr("stroke-dasharray", 3, 6)
			.attr("stroke-width", 2);
	}

}

Link.prototype.tickHandler = function (element, graph) {
	var link = this;
	// Display and update links
	//var root = d3.select(element);
	

	this.rootElement.select("path").attr("d", link.draw(this.source, this.target));
	
//	if (link.bidirectional) {
//
//		
//	}.attr("x1", function(d) { return d.source.x; })
//	    .attr("y1", function(d) { return d.source.y; })
//	    .attr("x2", function(d) { return d.target.x; })
//	    .attr("y2", function(d) { return d.target.y; })
	// Display and update the link labels
	//var text = root.selectAll("text");
	//text.attr("x", function(d) { return source.xpos() + (d.target.xpos() - d.source.xpos())/2; });
	//text.attr("y", function(d) { return source.ypos() + (d.target.ypos() - d.source.ypos())/2; });

}

Link.prototype.getKeyInfo = function () {
	return {
		linkType: this.linkType,
		canShowHide: this.userCanHide,
	};
}