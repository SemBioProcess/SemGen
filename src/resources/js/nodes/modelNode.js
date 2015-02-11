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
					"<li onclick='comingSoonClickHandler(this);'>Merge</li>" +
					"<li onclick='comingSoonClickHandler(this);'>Annotate</li>" +
					"<li onclick='comingSoonClickHandler(this);'>Extract</li>" +
					"<li onclick='comingSoonClickHandler(this);'>Visualize</li>" +
				"</ul>",
			placement: "bottom" });
	}
}


function comingSoonClickHandler(element) {
	alert("'" + element.innerHTML + "' coming soon!");
}