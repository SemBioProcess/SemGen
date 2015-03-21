/**
 * Controls whether dependency nodes are show in columns
 */
function Columns(node) {
	var position = Columns.columnPosition[node.nodeType];
	if(!position)
		return;
	
	// If column mode is on, listen for tick
	// If it's off, don't
	var checkColumnMode = function () {
		if($(".modes .columnView").is(':checked'))
			$(node).on('preTick', columnModeHandler);
		else
			$(node).off('preTick', columnModeHandler);
	};
	
	$(Columns).on("columnModeChanged", checkColumnMode);
	
	// When column mode is on, constrain the x position of the given node
	var parentInitialLocation;
	var columnModeHandler = function () {
		if(!parentInitialLocation) 
			parentInitialLocation = node.parent.x;
		
		node.x = Math.max(parentInitialLocation - position, Math.min(parentInitialLocation - position + Columns.columnWidth, node.x));
	};
	
	checkColumnMode();
}

// Initialize the bind
Columns.initialize = function () {
	$(".modes .columnView").bind('change', function(){        
		$(Columns).triggerHandler('columnModeChanged')
	});
}

Columns.columnWidth = 200;
Columns.columnPosition = {
	"State": -150,
	"Rate": 150,
}

// We need to wait until the page has loaded before we initialize
$(window).load(Columns.initialize);
