$(window).bind("cwb-initialized", function(e) {
	var receiver = e.originalEvent.commandReceiver;
	var sender = e.originalEvent.commandSender;

	var graph = new Graph();
	var flyoutMenu = createFlyoutMenu(graph);
	
	$(".addModelButton").click(function() {
		sender.addModel();
	});
	
	// Adds a model node to the d3 graph
	receiver.onAddModel(function (modelName) {
		graph.addNode(new ModelNode(modelName, flyoutMenu));
	})
});

/**
 * Creates a flyout menu for the stage
 */
function createFlyoutMenu (graph) {
	var flyoutMenu = new FlyoutMenu();
	
	// Update the position of the flyout when the graph ticks
	graph.addTickHandler(function () {
		flyoutMenu.updatePosition();
	});
	
	var createComingSoonClickHandler = function (taskName) {
		return function () { alert("'" + taskName + "' coming soon!") };
	}
	
	// Define menu items
	var menuItems = [
		{
			text: "Merge",
			onClick: createComingSoonClickHandler("Merge"),
		},
		{
			text: "Annotate",
			onClick: createComingSoonClickHandler("Annotate"),
		},
		{
			text: "Extract",
			onClick: createComingSoonClickHandler("Extract"),
		},
		{
			text: "Visualize",
			onClick: createComingSoonClickHandler("Visualize"),
		},
	];
	
	// Add each menu item to the flyout
	menuItems.forEach(function (item) {
		flyoutMenu.addMenuItem(item);
	});
	
	// When we click outside of the flyout area hide the flyout
	$(window).click(function () {
		flyoutMenu.getRoot().hide();
	});
	
	return flyoutMenu;
}