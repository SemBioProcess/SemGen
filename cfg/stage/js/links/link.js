/**
 * Represents a link in the d3 graph
 */
function Link(graph, srclink, output, input, length) {
	if(!graph)
		return;

	this.graph = graph;
	this.srclink = srclink;
	this.id = srclink.id;
	//this.displayName = limitWords(this.name, 5);
	this.className = "link";
	this.source = input;
	this.target = output;
	this.length = length;
	this.value = 1;
	//this.linkType = srclink.linkType;
	this.hidden = false;
	this.userCanHide = false;

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

	this.rootElement.attr("class", this.className);

	this.rootElement.append("svg:path")
			.attr("id", this.id)
			.attr("class", "link " + this.linkType);

	// Create the text elements
	this.createTextElement("shadow");
	this.createTextElement("real");

}

Link.prototype.tickHandler = function (element, graph) {

	// Display and update links
	var root = d3.select(element);
	root.select("path").attr("d", function(d) {
    	    var dx = d.target.xpos() - d.source.xpos(),
    	        dy = d.target.ypos() - d.source.ypos(),
    	        dr = 0;
    	        theta = Math.atan2(dy, dx) + Math.PI * 2,
    	        d90 = Math.PI / 2,
    	        dtxs = d.target.xpos() - d.target.r * Math.cos(theta),
    	        dtys = d.target.ypos() - d.target.r * Math.sin(theta),
    	        arrowHeadWidth = 2;

    	    return "M" + d.source.xpos() + "," + d.source.ypos() +
    	    		"A" + dr + "," + dr + " 0 0 1," + d.target.xpos() + "," + d.target.ypos() +
    	    		"A" + dr + "," + dr + " 0 0 0," + d.source.xpos() + "," + d.source.ypos() +
    	    		"M" + dtxs + "," + dtys + "l" + (arrowHeadWidth * Math.cos(d90 - theta) - 10 * Math.cos(theta)) + "," + (-arrowHeadWidth * Math.sin(d90 - theta) - 10 * Math.sin(theta)) +
    	    		"L" + (dtxs - arrowHeadWidth * Math.cos(d90 - theta) - 10 * Math.cos(theta)) + "," + (dtys + arrowHeadWidth * Math.sin(d90 - theta) - 10 * Math.sin(theta)) +
    	    		"z";
    	});

	// Display and update the link labels
	var text = root.selectAll("text");
	text.attr("x", function(d) { return d.source.xpos() + (d.target.xpos() - d.source.xpos())/2; });
	text.attr("y", function(d) { return d.source.ypos() + (d.target.ypos() - d.source.ypos())/2; });

}

Link.prototype.getKeyInfo = function () {
	return {
		linkType: this.linkType,
		canShowHide: this.userCanHide,
	};
}

Link.prototype.createTextElement = function (className) {
	this.rootElement.append("svg:text")
			.attr("class", "linkLabel")
			.attr("font-size", "14px")
			.attr("font-style", "italic")
			.attr("fill", "green")
			.attr("class", className)
			.attr("text-anchor", "middle")
			.text(this.displayName);
}

// Limit displayName to 5 words
var limitWords = function (text, wordLimit) {
	if(text == null) return;
	var finalText = "";
	var text2 = text.replace(/\s+/g, ' ');
	var text3 = text2.split(' ');
	var numberOfWords = text3.length;
	var i=0;
	if(numberOfWords > wordLimit)
	{
		for(i=0; i< wordLimit; i++)
			finalText = finalText+" "+ text3[i];

		return finalText+"...";
	}
	return text;
}