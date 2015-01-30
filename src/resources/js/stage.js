$(window).bind("cwb-initialized", function(e) {
	var receiver = e.originalEvent.commandReceiver;
	var sender = e.originalEvent.commandSender;

	$(".addModelButton").click(function() {
		sender.addModel();
	});
});