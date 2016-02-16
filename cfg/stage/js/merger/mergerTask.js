/**
 * 
 */

MergerTask.prototype = new Task();
MergerTask.prototype.constructor = MergerTask;

function MergerTask(graph, models) {
	Task.prototype.constructor.call(this, graph);
	this.nodes = models;
	
	var merger = this;
	var nodes = this.nodes;
	
}