/**
 *
 */
function LeftSidebar(graph) {
	this.graph = graph;
	
	var t = document.querySelector('#leftProjectMenus');
	var clone = document.importNode(t.content, true);
	
	document.querySelector('#leftSidebar').appendChild(clone);
	
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
				"<li><a href='#' onclick='main.task.taskClicked(this);'>Close</a></li>" +
			"</ul>" +
			"Visualize" +
			"<ul>" +
				"<li><a href='#' onclick='main.task.doModelAction(function(model) " +
					"{ model.createVisualization(0, true);});'>Submodels</a></li>" +
				"<li><a href='#' onclick='main.task.doModelAction(function(model) " +
					"{ model.createVisualization(1, true);});'>Dependencies</a></li>" +
				"<li><a href='#' onclick='main.task.doModelAction(function(model) " +
					"{ model.createVisualization(2, true);});'>PhysioMap</a></li>" +
			"</ul>"

		);
	}

}
