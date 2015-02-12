/**
 * Represents a model node in the d3 graph
 */

var initialized = false;
ModelNode.prototype = new Node();
ModelNode.prototype.constructor = Node;
function ModelNode (id) {
	Node.prototype.constructor.call(this, id, 16);
	this.fixed = true;
	this.popover = false;
	
	this.addClassName("modelNode");
	
	if(!initialized)
	{
		// Show a popover containing model tasks when each model node is clicked
		$("body").popover({
			selector: ".modelNode circle",
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
							"<li><a href='#' onclick='comingSoonClickHandler(this);'>Dependencies</a></li>" +
						"</ul>" +
					"</li>" +
				"</ul>",
			placement: "bottom" });

		initialized = true;
	}
}


function comingSoonClickHandler(element) {
	alert("'" + element.innerHTML + "' coming soon!");
}