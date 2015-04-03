/**
 * Drag to merge
 * @param node
 */
var AllNodes = [];
var DropZoneSideLength = 50;
function DragToMerge(_node) {
	AllNodes.push(_node);
	
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
		
		// Save the original location so we can move the node back
		// if a merge is successful (we don't want two models occupying the same location)
		var originalLocation = {
			x: null,
			y: null,
		};
		var mergeNode = null;
		
		var nodeDrag = d3.behavior.drag()
			.on("dragstart", function (d, i) {
				// Save the original location
				originalLocation = {
					x: _node.x,
					y: _node.y,
				};
				
				// Show drop zones on all other model nodes
				AllNodes.forEach(function (node) {
					if(node != _node)
						node.rootElement.classed("dropZone", true);
				});
			})
		    .on("drag", function (d, i) {
		    	// Drag functionality
		        _node.px += d3.event.dx;
		        _node.py += d3.event.dy;
		        _node.x += d3.event.dx;
		        _node.y += d3.event.dy; 
		        _node.graph.tick(); // this is the key to make it work together with updating both px,py,x,y on d !
		        
		        // Check whether the node we're dragging is overlapping
		        // any of the other nodes. If it is update the UI.
		        AllNodes.forEach(function (node) {
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
		    })
		    .on("dragend", function (d, i) {
		    	// If the node was dropped on another node then merge the two
		        if(mergeNode) {
		        	sender.merge(_node.name, mergeNode.name);
		        	mergeNode = null;
		        	
		        	// Move the node back to its original location
		        	_node.px = originalLocation.x;
		        	_node.py = originalLocation.y;
		        	_node.x = originalLocation.x;
		        	_node.y = originalLocation.y;
		        }
		        
		        // Remove any classes we may have set
		        AllNodes.forEach(function (node) {
		        	node.rootElement.classed("dropZone", false);
					node.rootElement.classed("canDrop", false);
				});
		        
		        _node.graph.tick();
		    });
		
		// Add the dragging functionality to the node
		_node.rootElement.call(nodeDrag);
	});
}