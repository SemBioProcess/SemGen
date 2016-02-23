/**
 * 
 */

MergerTask.prototype = new Task();
MergerTask.prototype.constructor = MergerTask;

function MergerTask(graph) {
	Task.prototype.constructor.call(this, graph);
	//this.nodes = models;
	
	var merger = this;
	var nodes = this.nodes;
	
	receiver.onTest(function (msg) {
		alert("It works!");
	});
	sender.consoleOut("Hi");
}