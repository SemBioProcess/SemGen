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
	var main = this;
	this.graph = new Graph();
	var graph = this.graph;
	
	if(!this.graph) {
		alert("Graph initialization failed.");
		return;
	}

	KeyElement.getInstance().initialize(this.graph);
	this.tasktray = new TaskTray();

	//If a change task request is received from Java
	this.changeTask = function changeTask(stagestate) {
		graph.depBehaviors = [];
		
		$('#modalContent').empty();
		
		var taskname = stagestate.tasktype;
		if (taskname=="proj") {
			this.task = new Stage(graph, stagestate);
		}
		if (taskname=="merge") {
			this.task = new MergerTask(graph, stagestate);
		}
		if (taskname=="extract") {
			this.task = new ExtractorTask(graph, stagestate);
		}
		sender.initialized(main.task);
		this.task.onInitialize();
		this.graph.setTaskNodes(this.task.nodes);
		
		this.graph.update();
		// Make ActiveTaskTray blink, and add Merger icon when Merge is in progress
		//$("#activeTaskText").addClass('blink');
		if (this.tasktray.hasIndex(this.task.taskindex)) {
			
		}
		else {
			this.tasktray.addTask(this.task.getTaskType(), this.task.taskindex);
		}
	}
	
	this.closeTask = function(taskindex) {
		this.tasktray.removeTask(taskindex);
	}
	$(".sidebar").contents().hide();
	
	// Slide horizontal for sidebars
	$(".sidebarButton").click(function() {
		var selector = $(this).data("target");
		$(selector).toggleClass('in');
		$(this).text($(this).text() == '»' ? '«' : '»');
		//If the sidebar is collapsed, hide contents
		if ($(selector).hasClass("in")) {
			$(selector).contents().show();
		}
		else {
			$(selector).contents().hide();
		}
	});
}

//The window is receiving two seperate initialization messages which is resulting in 
//two seperate svg HTML objects being created.
var initialized = false;
$(window).bind("cwb-initialized", function(e) {
	if (initialized) {
		initialized = true;
		sender = e.originalEvent.commandSender;
	
		receiver = e.originalEvent.commandReceiver;
		main = new main();	
	
		window.onresize = function () {
			main.graph.updateHeightAndWidth();
			main.graph.update();
		};
	}
	initialized = true;
});