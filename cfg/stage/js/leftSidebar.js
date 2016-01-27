/**
 * 
 */
function LeftSidebar(graph) {
	this.graph = graph;
	
	this.updateModelPanel = function(model) {
		var pane = $("#modelinfo");
		
		pane.empty();
		
		if (model==null) {
			pane.append("No model selected");
			return;
		}
		
		pane.append(
			"<h5>" + model.id + "</h5>" +
			"<ul>" +
				"<li><a href='#' onclick='main.task.taskClicked(this);'>Annotate</a></li>" +
				"<li><a href='#' onclick='main.task.taskClicked(this);'>Extract</a></li>" +
				"<li><a data-toggle='modal' href='#mergerModal'>Merge</a></li>" +
				"<li><a href='#' onclick='main.task.taskClicked(this);'>Close</a></li>" +
			"</ul>" +
			"Visualize" +
			"<ul>" +
				"<li><a href='#' onclick='main.task.taskClicked(this);'>Submodels</a></li>" +
				"<li><a href='#' onclick='main.task.taskClicked(this);'>Dependencies</a></li>" +
				"<li><a href='#' onclick='main.task.taskClicked(this);'>PhysioMap</a></li>" +
			"</ul>"
			
		);
	}
	
}

