/**
 * Manages node selection
 */
function SelectionManager () {

	// Listen for clicks when the graph is updated
	this.initialize = function (graph) {
		$(graph).on("preupdate", function () {
			// Remove old selection handlers
			$(".node circle").off("click", clickHandler);
		});
		
		$(graph).on("postupdate", function () {
			// Add click handler to each node			
			$(".node circle").click(clickHandler);
		});
	};
	
	// Register selection handlers
	this.onSelected = function (handler) {
		selectionHandlers.push(handler);
	};
	
	var selectionHandlers = [];
	
	var clickHandler = function (e) {
		// Execute selection handlers
		selectionHandlers.forEach(function(handler) {
			handler(e, e.target, e.target.__data__);
		})
	};
}

SelectionManager.instance = null;
SelectionManager.getInstance = function () {
	return SelectionManager.instance || (SelectionManager.instance = new SelectionManager());
}