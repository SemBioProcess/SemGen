$(window).bind("cwb-initialized", function(e) {
	var receiver = e.originalEvent.commandReceiver;
	var sender = e.originalEvent.commandSender;

	var graph = new Graph();
	var flyoutMenu = createFlyoutMenu();
	
	$(".addModelButton").click(function() {
		sender.addModel();
	});
	
	receiver.onAddModel(function (modelName) {
		graph.addNode(modelName);
	})
});

/**
 * Creates a flyout menu for the stage
 */
function createFlyoutMenu () {
	var flyoutMenu = new FlyoutMenu();
	
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
}

/**
 * Flyout style menu
 */
function FlyoutMenu() {
	
	// Add an item to the menu
	this.addMenuItem = function (item) {
		var listItem = document.createElement("li");
		listItem.innerHTML = item.text;
		$(listItem).click(item.onClick);
		root.appendChild(listItem);
	};
	
	// Hide the flyout
	this.getRoot = function () {
		return $(root)
	}
	
	// Create the root element
	var root = document.createElement("ul");
	root.className = "flyoutMenu";
	this.getRoot().hide();
	document.body.appendChild(root);
}