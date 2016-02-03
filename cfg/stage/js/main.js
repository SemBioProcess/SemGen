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
	
	if(!this.graph) {
		alert("Graph initialization failed.");
		return;
	}
	
	KeyElement.getInstance().initialize(this.graph);

	this.task = new Stage(this.graph);
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


