/**
 * Represents a model node in the d3 graph
 */

var openPopover;

ModelNode.prototype = new Node();
ModelNode.prototype.constructor = Node;
function ModelNode (id) {
	Node.prototype.constructor.call(this, id, 16);
	this.fixed = true;
	
	this.addClassName("modelNode");
}

ModelNode.prototype.initialize = function (element) {
	if(Node.prototype.initialize.call(this, element))
		return;
	
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
		
		// If there's an open popover, hide it
		if(openPopover != null)
			openPopover.popover("hide");
		
		// If the open popover is for the node that was clicked, clear the open popover 
		if(openPopover != null && openPopover.attr("aria-describedBy") == popover.attr("aria-describedBy"))
			openPopover = null;
		// Otherwise show the popover for the node that was clicked
		// and store it
		else {
			$(this).popover("show");
			openPopover = $(this).popover();
			openPopover.modelNode = e.data.modelNode;
		}
	});
}

function comingSoonClickHandler(element) {
	alert("'" + element.innerHTML + "' coming soon!");
}

function taskClicked (element) {
	var task = element.innerHTML;
	sender.taskClicked(task, openPopover.modelNode.id);
}