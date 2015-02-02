$(window).bind("cwb-initialized", function(e) {
	var receiver = e.originalEvent.commandReceiver;
	var sender = e.originalEvent.commandSender;

	var graph = new Graph();
	
	$(".addModelButton").click(function() {
		sender.addModel();
	});
	
	receiver.onAddModel(function (modelName) {
		graph.addNode(modelName);
	})
});