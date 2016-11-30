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

		pane.append(
			"<span class='subheading'>" + model.id + "</span>" +
			"<ul class='menulist'>" +
				"<li><button type='button' class='panelbutton' onclick='main.task.taskClicked(this);'>Annotate</button></li>" +
				"<li><button type='button' class='panelbutton' onclick='main.task.taskClicked(this);'>Extract</button></li>" +
				"<li><button type='button' class='panelbutton' onclick='main.task.taskClicked(this);'>Export</button></li>" +
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
	refreshModelList();

}
