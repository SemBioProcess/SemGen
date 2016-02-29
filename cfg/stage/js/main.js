/**
 *
 */

//Global objects
var sender;
var receiver;
var main;

//Variable for holding functions awaiting a response from Java
var CallWaiting;

function main() {
	this.graph = new Graph();
	var graph = this.graph;

	if(!this.graph) {
		alert("Graph initialization failed.");
		return;
	}

	KeyElement.getInstance().initialize(this.graph);

	this.task = new Stage(this.graph);
	this.graph.setTaskNodes(this.task.nodes);
	
	//If a change task request is received from Java
	this.changeTask = function changeTask(taskname) {
		var content = document.querySelector('#mergerContent');
		content.removeChild(content.firstChild());
		
		if (taskname=="merge") {
			this.task = new MergerTask(graph);
		}
		
		this.graph.setTaskNodes(this.task.nodes);
		this.graph.update();
		$('#taskModal').modal('toggle');
		// Make ActiveTaskTray blink, and add Merger icon when Merge is in progress
		$("#activeTaskText").addClass('blink');
	}
	
	// Slide up panel for Active Task Tray
	$("#activeTaskTray").click(function() {
		$("#activeTaskPanel").slideToggle();
	});
}

$(window).bind("cwb-initialized", function(e) {
	sender = e.originalEvent.commandSender;

	receiver = e.originalEvent.commandReceiver;
	main = new main();	

	window.onresize = function () {
		main.graph.updateHeightAndWidth();
		main.graph.update();
	};

});