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

/**
 * Flyout style menu
 */
function FlyoutMenu() {
	
	// Add an item to the menu
	this.addMenuItem = function (item) {
		var listItem = document.createElement("li");
		listItem.innerHTML = item.text;
		$(listItem).click(item.onClick);
		list.appendChild(listItem);
	};
	
	// Position the flyout around the given element
	var elementToPositionAround;
	this.positionAroundElement = function (element) {
		elementToPositionAround = element;
		this.updatePosition();
		this.getRoot().show();
	};
	
	// Update the flyout's position
	this.updatePosition = function () {
		if(!elementToPositionAround)
			return;
		
		var newOffset = {};
		
		// Get the new offset if this is a circle element
		if(elementToPositionAround.tagName == "circle") {
			var r = parseInt($(elementToPositionAround).attr("r"));
			newOffset.left = $(elementToPositionAround).offset().left - this.getRoot().width() / 2 + r;
			newOffset.top = $(elementToPositionAround).offset().top + 2 * r;
		}
		else
			// The flyout is not setup to position itself around this type of element
			return;
		
		// Set the new offset and show the flyout
		this.getRoot().offset(newOffset);
	}
	
	// Get the root element
	this.getRoot = function () {
		return $(root)
	}
	
	// Create the list element
	var list = document.createElement("ul")
	
	// Create the root element
	var root = document.createElement("div");
	root.className = "flyoutMenu";
	$(root).click(function (e) {
		// Swallow events when the flyout is clicked
		e.stopPropagation();
	});
	this.getRoot().hide();
	
	// Put the elements together
	root.appendChild(list);
	document.body.appendChild(root);
}