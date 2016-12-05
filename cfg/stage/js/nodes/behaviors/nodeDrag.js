/**
 * 
 */

function NodeDrag(_node) {
	// When the node visualization is created add a dropzone element
	// and listen for dragging.
	// When a model is dragging all other models will display dropzones. If the model
	// is released on a dropzone the merger will open with those models

	
	$(_node).on('createVisualization', function (e, root) {
		var nodeDrag = d3.drag()
			.subject(_node)
			.on("start", function (d, i) {
				_node.graph.pause();
				_node.rootElement.selectAll("circle").attr("r", _node.r*2);
				_node.fx = _node.xpos();
				_node.fy = _node.ypos();	
				_node.dragstart.forEach(function(behavior) {
					behavior(_node);
				});
				
				_node.graph.tick();
			})
		    .on("drag", function (d, i) {
		    	//if (!_node.showchildren) {
		    		var posx =  d3.event.x;
		    		var posy = d3.event.y;

		    		if (posx > _node.graph.w-15 ) {
		    			posx = _node.graph.w-15;
		    		}
		    		else if (posx < 15 ) {
		    			posx = 15;
		    		}
		    		if (posy > _node.graph.h-30 ) {
		    			posy = _node.graph.h-30;
		    		}
		    		else if (posy < 15 ) {
		    			posy = 15;
		    		}
		    		_node.setLocation(posx, posy);
		    	//}
		    	
		    	_node.drag.forEach(function(behavior) {
		    		behavior(_node);
				});
				_node.graph.tick();
		    	
		    })
		    .on("end", function (d, i) {
		    	_node.rootElement.selectAll("circle").attr("r", _node.r);
		    	_node.setLocation(_node.fx, _node.fy);
		    	_node.dragend.forEach(function(behavior) {
		    		behavior(_node);
				});
		    	if (!_node.fixed) {
		    		_node.fx = null;
					_node.fy = null;	
		    	}

		    	if (!_node.graph.fixedMode) {
		    		_node.graph.resume();
		    	}
		    });
		
		// Add the dragging functionality to the node
		_node.rootElement.call(nodeDrag);
	});

};