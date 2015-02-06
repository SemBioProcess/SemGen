/**
 * Flyout style menu that can position itself around elements
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