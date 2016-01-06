/**
 * 
 */

function ModelPanel(model) {
	var pane = $("#modelinfo");
	
	pane.empty();
	
	if (model==null) {
		pane.append("No model selected");
		return;
	}
	
	pane.append(
			model.id +
		"<ul>" +
			"<li><a href='#' onclick='taskClicked(this);'>Annotate</a></li>" +
			"<li><a href='#' onclick='taskClicked(this);'>Extract</a></li>" +
			"<li><a href='#' onclick='taskClicked(this);'>Close</a></li>" +
			"<div class='submenuContainer'>" +
				"Visualize" +
				"<ul>" +
					"<li><a href='#' onclick='taskClicked(this);'>Submodels</a></li>" +
					"<li><a href='#' onclick='taskClicked(this);'>Dependencies</a></li>" +
					"<li><a href='#' onclick='taskClicked(this);'>PhysioMap</a></li>" +
				"</ul>" +
			"</div>" +
		"</ul>"
	);
}

