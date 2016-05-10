/**
 * Drag to merge
 * @param node
 */

var DropZoneSideLength = 50;
function DragToMerge(_node) {
	
	// When the node visualization is created add a dropzone element
	// and listen for dragging.
	// When a model is dragging all other models will display dropzones. If the model
	// is released on a dropzone the merger will open with those models
	$(_node).on('createVisualization', function (e, root) {
		// Add a rectagle that is the drop zone
		root.append("svg:rect")
			.attr("x", -DropZoneSideLength/2)
			.attr("y", -_node.r)
		    .attr("width", DropZoneSideLength)
		    .attr("height", DropZoneSideLength)
		    .attr("class","dropZoneRect");
	});
		// Save the original location so we can move the node back
		// if a merge is successful (we don't want two models occupying the same location)
		var originalLocation = {
			x: null,
			y: null,
		};
		var models;
		var mergeNode = null;
		
		_node.dragstart.push(function (d) {
				if (_node.children) return;
				//Refresh node references
				models = _node.graph.getVisibleNodes();
				// Save the original location
				originalLocation = {
					x: _node.x,
					y: _node.y,
				};
				
				
				// Show drop zones on all other model nodes
				models.forEach(function (node) {
					if(node != _node)
						node.rootElement.classed("dropZone", true);
				});
			});
		    _node.drag.push(function (d) {
		    	if (_node.children) return;
		        
		        // Check whether the node we're dragging is overlapping
		        // any of the other nodes. If it is update the UI.
		        models.forEach(function (node) {
					if(node == _node)
						return;
					
					var leftBound = node.x - node.r - DropZoneSideLength/2;
					var rightBound = node.x + node.r + DropZoneSideLength/2;
					var upperBound = node.y + DropZoneSideLength;
					var lowerBound = node.y - node.r*2;
					if(_node.x >= leftBound &&
						_node.x <= rightBound &&
						_node.y >= lowerBound &&
						_node.y <= upperBound)
					{
						mergeNode = node;
						node.rootElement.classed("canDrop", true);
					}
					else {
						node.rootElement.classed("canDrop", false);
						if(node == mergeNode)
							mergeNode = null;
					}
				});
		    });
		    _node.dragend.push(function (d) {
		    	if (_node.children) {
		    		return;
		    	}
		    	// If the node was dropped on another node then merge the two
		        if(mergeNode) {
		        	var modelstomerge = _node.name + "," + mergeNode.name;
		        	sender.merge(modelstomerge, main.task);
		        	mergeNode = null;
		        	
		        	// Move the node back to its original location
		        	_node.px = originalLocation.x;
		        	_node.py = originalLocation.y;
		        	_node.x = originalLocation.x;
		        	_node.y = originalLocation.y;
		        }
		        
		        // Remove any classes we may have set
		        models.forEach(function (node) {
		        	node.rootElement.classed("dropZone", false);
					node.rootElement.classed("canDrop", false);
				});
		        
		    });	
};

