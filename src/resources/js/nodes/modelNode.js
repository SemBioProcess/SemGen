/**
 * Represents a model node in the d3 graph
 */

var openPopover;

ModelNode.prototype = new Node();
ModelNode.prototype.constructor = Node;
function ModelNode (name) {
	Node.prototype.constructor.call(this, name, name, 16, 0);
	this.fixed = true;
	this.children = null;
	this.hull = null;
	
	this.addClassName("modelNode");
}

ModelNode.prototype.createVisualElement = function (element, graph) {
	Node.prototype.createVisualElement.call(this, element, graph);
	
	// Create the hull that will encapsulate child nodes
	this.hull = d3.select("svg > g")
		.append("g")
		.append("path")
		.attr("class", "hull");

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
						"<li><a href='#' onclick='comingSoonClickHandler(this);'>Submodels</a></li>" +
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
}

ModelNode.prototype.tickHandler = function (element) {
	Node.prototype.tickHandler.call(this, element);
	
	// Draw the hull around child nodes
	if(this.children) {
		// Translate the child node x and y into vertices the hull understands
		var customHull = d3.geom.hull();
		customHull.x(function(d){return d.x;});
		customHull.y(function(d){return d.y;});
		
		// Draw hull
		this.hull.datum(customHull(this.children))
			.attr("d", function(d) { return "M" + d.map(function(n){ return [n.x, n.y] }).join("L") + "Z"; });
	}
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