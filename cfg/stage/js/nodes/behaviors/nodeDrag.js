/**
 * 
 */

function NodeDrag(_node) {
	// When the node visualization is created add a dropzone element
	// and listen for dragging.
	// When a model is dragging all other models will display dropzones. If the model
	// is released on a dropzone the merger will open with those models

	
	$(_node).on('createVisualization', function (e, root) {
		var nodeDrag = d3.behavior.drag()
			.on("dragstart", function (d, i) {
				
				_node.dragstart.forEach(function(behavior) {
					behavior(_node);
				});
				
				_node.graph.tick();
			})
		    .on("drag", function (d, i) {
		    	_node.drag.forEach(function(behavior) {
		    		behavior(_node);
				});
		    	_node.graph.tick();
		    })
		    .on("dragend", function (d, i) {
		    	_node.dragend.forEach(function(behavior) {
		    		behavior(_node);
				});
		        _node.graph.tick();
		    });
		
		// Add the dragging functionality to the node
		_node.rootElement.call(nodeDrag);
	});

	
};