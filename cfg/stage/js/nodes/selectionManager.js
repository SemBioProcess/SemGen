/**
 * Manages node selection
 */
function SelectionManager () {
	var DELAY = 500,
    clicks = 0,
    timer = null;
	
	var selectionHandlers = [];
	var dblclickHandlers = [];
	
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
	
	// Register selection handlers
	this.onDoubleClick = function (handler) {
		dblclickHandlers.push(handler);
	};
	
	//Handler for sensing single and 
	var clickHandler = function (e) {
		clicks++;  //count clicks
		
	        // Execute selection handlers
	        if(clicks == 1) {
	        	timer = setTimeout(function() {
                selectionHandlers.forEach(function(handler) {
                	handler(e, e.target, e.target.__data__);
                });
    	        clicks = 0;             //after action performed, reset counter
            }, DELAY);

	        } else {
	        	dblclickHandlers.forEach(function(handler) {
        			handler(e, e.target, e.target.__data__);
        		});
	            clearTimeout(timer);    //prevent single-click action
		        clicks = 0;             //after action performed, reset counter

	        }
		        //d3.event.stopPropagation();
		
	};
}

SelectionManager.instance = null;
SelectionManager.getInstance = function () {
	return SelectionManager.instance || (SelectionManager.instance = new SelectionManager());
}