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
			"<span class='subheading'>" + model.id + "</span>" +
			"<ul class='menulist'>" +
				"<li><button type='button' class='panelbutton' onclick='main.task.taskClicked(this);'>Annotate</button></li>" +
				"<li><button type='button' class='panelbutton' onclick='main.task.taskClicked(this);'>Extract</button></li>" +
				"<li><button type='button' class='panelbutton' onclick='main.task.taskClicked(this);'>Close</button></li>" +
			"</ul>" +
			"<span class='subheading'>Visualize</span>" + 
			"<ul class='menulist'>" +
				"<li><button type='button' class='panelbutton' onclick='main.task.doModelAction(function(model) " +
					"{ model.createVisualization(0, true);});'>Submodels</button></li>" +
				"<li><button type='button' class='panelbutton' onclick='main.task.doModelAction(function(model) " +
					"{ model.createVisualization(1, true);});'>Dependencies</buttona></li>" +
				"<li><button type='button' class='panelbutton' onclick='main.task.doModelAction(function(model) " +
					"{ model.createVisualization(2, true);});'>PhysioMap</button></li>" +
			"</ul>"

		);
	}

}
