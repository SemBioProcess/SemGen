/**
 * Represents a model node in the d3 graph
 */

var openPopover;

ModelNode.prototype = new Node();
ModelNode.prototype.constructor = Node;
function ModelNode (name) {
	Node.prototype.constructor.call(this, name, name, 16, 0, 20);
	this.fixed = true;
	this.children = null;
	this.hull = null;
	
	this.addClassName("modelNode");
}

ModelNode.prototype.createVisualElement = function (element, graph) {
	Node.prototype.createVisualElement.call(this, element, graph);
	
	// Create the hull that will encapsulate child nodes
	var modelArg = this;
	this.hull = this.rootElement.append("path")
		.attr("class", "hull")
		.style("opacity", .2)
		.attr("stroke", this.rootElement.style("fill"))
		.attr("fill", this.rootElement.style("fill"))
		.on("click", function(d) {
			console.log("hull click");
			
			// Remove child nodes from the graph
			modelArg.children.forEach(function (child) {
				graph.removeNode(child.id);
			});
			modelArg.setChildren(null);
		});

	// Define the popover html
	$("circle", element).popover({
		container: "body",
		title: "Model Tasks",
		html: true,
		content: 
			"<ul class='modelPopover'>" +
				"<li><a href='#' onclick='comingSoonClickHandler(this);'>Merge</a></li>" +
				"<li><a href='#' onclick='comingSoonClickHandler(this);'>Annotate</a></li>" +
				"<li><a href='#' onclick='comingSoonClickHandler(this);'>Extract</a></li>" +
				"<li class='submenuContainer'>" +
					"Visualize" +
					"<ul>" +
						"<li><a href='#' onclick='taskClicked(this);'>Submodels</a></li>" +
						"<li><a href='#' onclick='taskClicked(this);'>Dependencies</a></li>" +
					"</ul>" +
				"</li>" +
			"</ul>",
		placement: "bottom",
		trigger: "manual" });

	// Show the popover when the node is clicked
	$("circle", element).click({modelNode: this}, function (e) {
		var popover = $(this);
		var isOpen = false;
		
		// If there's an open popover, hide it
		if(openPopover != null) {
			isOpen = openPopover.attr("aria-describedBy") == popover.attr("aria-describedBy");
			hideOpenPopover();
		}
		
		// If the open popover is not the node that was clicked
		// and store it
		if(!isOpen) {
			$(this).popover("show");
			openPopover = $(this).popover();
			openPopover.modelNode = e.data.modelNode;
		}
		
		e.stopPropagation();
	});
	
	$(window).click(function () { hideOpenPopover(); });
}

ModelNode.prototype.setChildren = function (children) {
	this.children = children;

	// Show/Hide the correct elements depending on the model's state
	var circleDisplay = this.children ? "none" : "inherit";
	var hullDisplay = this.children ? "inherit" : "none";
	
	this.rootElement.select("circle").style("display", circleDisplay);
	this.rootElement.select(".hull").style("display", hullDisplay);
}

ModelNode.prototype.tickHandler = function (element, graph) {
	// Draw the hull around child nodes
	if(this.children) {
		// 1) Convert the child positions into vertices that we'll use to create the hull
		// 2) Calculate the center of the child nodes and the top of the child nodes so 
		// 		we can position the text and hidden circle appropriately
		var vertexes = [];
		var centerX = 0;
		var minY = null;
		this.children.forEach(function (d) {
			vertexes.push([d.x, d.y]);
			
			centerX += d.x;
			minY = minY || d.y;
			minY = d.y < minY ? d.y : minY;
		});
		
		// Position the the text and hidden circle
		this.x = centerX / this.children.length;
		this.y = minY;
		
		// Draw hull
		this.hull.datum(d3.geom.hull(vertexes))
			.attr("d", function(d) { return "M" + d.join("L") + "Z"; })
			.attr("transform", "translate(" + -this.x + "," + -this.y + ")");
	}
	
	// Draw the model node
	Node.prototype.tickHandler.call(this, element, graph);
}

function hideOpenPopover() {
	if(!openPopover)
		return;
	
	openPopover.popover("hide");
	openPopover = null;
}
	
function comingSoonClickHandler(element) {
	alert("'" + element.innerHTML + "' coming soon!");
}

function taskClicked (element) {
	var task = element.innerHTML.toLowerCase();
	sender.taskClicked(openPopover.modelNode.id, task);
}