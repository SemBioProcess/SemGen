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
		graph.doodads = [];
		
		$('#modalContent').empty();
		$("#addModel").off("click");
		$("#addModelButton").off("click");
		
		var taskname = stagestate.tasktype;
		if (taskname=="proj") {
			this.task = new Stage(graph, stagestate);
		}
		if (taskname=="merge") {
			this.task = new MergerTask(graph, stagestate);
		}
		sender.initialized(main.task);
		this.task.onInitialize();
		graph.setTaskNodes(this.task.nodes);
		
		this.graph.update();
		// Make ActiveTaskTray blink, and add Merger icon when Merge is in progress
		//$("#activeTaskText").addClass('blink');
		if (this.tasktray.hasIndex(this.task.taskindex)) {
			this.tasktray.setActiveTask(this.task.taskindex);
			this.tasktray.refresh();
		}
		else {
			var tasktype = this.task.getTaskType(), tasknodes = this.task.nodes;
			
			this.tasktray.addTask(tasktype, this.task.taskindex, tasknodes);
		}
	}
	
	this.closeTask = function(taskindex) {
		this.tasktray.removeTask(taskindex);
		if (this.tasktray.activetaskindex == taskindex) {
			this.tasktray.setActiveTask(0);
		}
		
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


$(window).load(function() {
	// sendNSCommand is defined when the stage is loaded in SemGen
	if(window.location.search.indexOf("testMode=false") != -1)
		return;

	var event; // The custom event that will be created

	if (document.createEvent) {
		event = document.createEvent("HTMLEvents");
		event.initEvent("cwb-initialized", true, true);
	}
	else {
		event = document.createEventObject();
		event.eventType = "cwb-initialized";
	}

	event.eventName = "cwb-initialized";
	
	if (document.createEvent) {
		window.dispatchEvent(event);
	}
	else {
		window.fireEvent("on" + event.eventType, event);
	}
});



