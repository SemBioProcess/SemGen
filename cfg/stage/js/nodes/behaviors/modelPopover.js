/**
 * Adds a popover to the given node
 * @param node
 */
var openPopover = null;
function ModelPopover(node) {
	$(node).on('createVisualization', function (e, root) {
		// Get the DOM element from the d3 selection
		var element = root[0][0];
		
		// Define the popover html
		$("circle", element).popover({
			container: "body",
			title: "Model Tasks",
			html: true,
			content: 
				"<ul class='modelPopover'>" +
					"<li><a href='#' onclick='taskClicked(this);'>Merge</a></li>" +
					"<li><a href='#' onclick='taskClicked(this);'>Annotate</a></li>" +
					"<li><a href='#' onclick='taskClicked(this);'>Extract</a></li>" +
					"<li><a href='#' onclick='taskClicked(this);'>Close</a></li>" +
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
	});
}

SelectionManager.getInstance().onSelected(function (e, element, node) {
	if(!(node instanceof ModelNode))
		return;
	
	var popover = $(element);
	var isOpen = false;
	
	// If there's an open popover, hide it
	if(openPopover != null) {
		isOpen = openPopover.attr("aria-describedBy") == popover.attr("aria-describedBy");
		hideOpenPopover();
	}
	
	// If the open popover is not the node that was clicked
	// and store it
	if(!isOpen) {
		$(element).popover("show");
		openPopover = $(element).popover();
		openPopover.modelNode = node;
	}
	
	e.stopPropagation();
});

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

// Hide the popover when the window is clicked
$(window).click(function () { hideOpenPopover(); });