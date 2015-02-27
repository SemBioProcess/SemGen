/**
 * Adds a popover to the given node
 * @param node
 */
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
		$("circle", element).click(function (e) {
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
				openPopover.modelNode = node;
			}
			
			e.stopPropagation();
		});
	});
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

// Hide the popover when the window is clicked
$(window).click(function () { hideOpenPopover(); });