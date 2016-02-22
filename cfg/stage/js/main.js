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
	
	receiver.onChangeTask(function(taskname) {
		if (taskname=="merger") {
			main.task = new mergerTask(graph, null);
		}
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


