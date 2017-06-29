/**
 *
 */
function LeftSidebar(graph) {
	this.graph = graph;
	var loadedmodels = [];

	var t = document.querySelector('#leftProjectMenus');
	var clone = document.importNode(t.content, true);
	
	document.querySelector('#leftSidebar').appendChild(clone);
	
	var refreshModelList = function() {
		var pane = $("#projectmodels");
		pane.empty();
		if (loadedmodels.length==0) {
			pane.append("No models loaded");
			return;
		}
		loadedmodels.forEach(function(m) {
			pane.append("<li>" + m.modelname + "</li> ");
		});
	}
	
	this.addModeltoList = function(node) {
		var loadedmodel = { 
				modelname: node.name,
				modelid: node.id,
			}; 
		
		loadedmodels.push(loadedmodel);
		refreshModelList();		
	}

	this.removeModelfromList = function(id) {
		var target = null;
		for (x in loadedmodels) {
			if (loadedmodels[x].id == id) {
				target = x;
			}
		}
		loadedmodels.splice(x, 1);
		refreshModelList();		
	}
	
	this.updateModelPanel = function(model) {
		var pane = $("#modelinfo");

		pane.empty();

		if (model==null) {
			pane.append("No model selected");
			return;
		}

        $("#rightSidebarModelName").text(model.id);

		pane.append(
			"<ul class='menulist'>" +
				"<li><button type='button' class='panelbutton' data-toggle='tooltip' title='Annotate model' onclick='main.task.taskClicked(this);'>Annotate</button></li>" +
				"<li><button type='button' class='panelbutton' data-toggle='tooltip' title='Export model to a different format' onclick='main.task.taskClicked(this);'>Export</button></li>" +
				"<li><button type='button' class='panelbutton' data-toggle='tooltip' title='Close current model' onclick='main.task.taskClicked(this);'>Close</button></li>" +
			"</ul>" +
			"<span class='subheading'>Visualize</span>" + 
			"<ul class='menulist'>" +
				"<li><button type='button' class='panelbutton' data-toggle='tooltip' title='Visualize submodel network' onclick='main.task.doModelAction(function(model) " +
					"{ model.createVisualization(0, true);});'>Submodels</button></li>" +
				"<li><button type='button' class='panelbutton' data-toggle='tooltip' title='Visualize mathematical dependencies' onclick='main.task.doModelAction(function(model) " +
					"{ model.createVisualization(1, true);});'>Dependencies</button></li>" +
				"<li><button type='button' class='panelbutton' data-toggle='tooltip' title='Visualize PhysioMap network' onclick='main.task.doModelAction(function(model) " +
					"{ model.createVisualization(2, true);});'>PhysioMap</button></li>" +
			"</ul>"

		);
	}
	refreshModelList();

}
