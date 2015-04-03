/**
 * Hidden nodes attached to labels to prevent labels and other nodes from overlapping
 */
var ShowHiddenNodes = false;
function HiddenLabelNode(anchorNode, percentageFromLeftOfLabel, charge) {
	this.id = percentageFromLeftOfLabel + "HiddenLabelNodeFor" + anchorNode.id;
	this.r = 5;
	this.charge = charge;
	
	// This node does not have any links
	this.getLinks = function () {
		return null;
	}
	
	// Create a visual element if we're showing the hidden node
	this.createVisualElement = function (element, graph) {
		if(!ShowHiddenNodes)
			return;

		this.circle = d3.select(element)
			.append("svg:circle")
		    .attr("r", thisArg.r)
		    .attr("fill","red");
	}
	
	this.tickHandler = function (element, graph) {		
		var root = d3.select(element);
		root.attr("transform", "translate(" + this.x + "," + this.y + ")");
	}
	
	// When the anchor node is created save its visual element
	var anchorTextElement;
	var createVisualizationHandler = function (root) {
		anchorTextElement = anchorNode.rootElement.select("text.real")[0][0];
	};

	// When the anchor node moves update the position of this node
	var thisArg = this;
	var postTickHandler = function () {
		var textElementBBox = anchorTextElement.getBBox();
		
		thisArg.x = anchorNode.x - textElementBBox.width / 2 + textElementBBox.width * percentageFromLeftOfLabel;
		thisArg.y = anchorNode.y + textElementBBox.y + textElementBBox.height / 2;
	}
	
	// Attach handlers when the anchor node is added to the graph
	$(anchorNode.graph).on("nodeAdded", function (e, node) {
		if(anchorNode != node)
			return;
		
		$(anchorNode).on("createVisualization", createVisualizationHandler);
		$(anchorNode).on("postTick", postTickHandler);
	});
	
	// Remove handlers when the anchor node is removed from the graph
	$(anchorNode.graph).on("nodeRemoved", function (e, node) {
		if(anchorNode != node)
			return;
		
		$(anchorNode).off("createVisualization", createVisualizationHandler);
		$(anchorNode).off("postTick", postTickHandler);
	});
}