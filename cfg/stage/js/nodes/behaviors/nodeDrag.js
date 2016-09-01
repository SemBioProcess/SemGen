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
				_node.graph.force.stop();
				_node.rootElement.selectAll("circle").attr("r", _node.r*2);
				_node.dragstart.forEach(function(behavior) {
					behavior(_node);
				});
				
				_node.graph.tick();
			})
		    .on("drag", function (d, i) {
		    	if (!_node.showchildren) {
		    		var posx =  _node.xpos() + d3.event.dx;
		    		var posy = _node.ypos() + d3.event.dy;
		    		_node.px += d3.event.dx;
			        _node.py += d3.event.dy;
		    		if (posx > _node.graph.w-15 ) {
		    			posx = _node.graph.w-15;
		    			_node.px = posx;
		    		}
		    		else if (posx < 15 ) {
		    			posx = 15;
		    			_node.px = posx;
		    		}
		    		if (posy > _node.graph.h-30 ) {
		    			posy = _node.graph.h-30;
		    			_node.py = posy;
		    		}
		    		else if (posy < 15 ) {
		    			posy = 15;
		    			_node.py = posy;
		    		}


			        _node.setLocation(posx, posy); 
		    	}
		    	
		    	_node.drag.forEach(function(behavior) {
		    		behavior(_node);
				});
				_node.graph.tick();
		    	
		    })
		    .on("dragend", function (d, i) {
		    	_node.rootElement.selectAll("circle").attr("r", _node.r);
		    	_node.dragend.forEach(function(behavior) {
		    		behavior(_node);
				});
		    	_node.graph.force.start();
		        _node.graph.tick();
		        
		    });
		
		// Add the dragging functionality to the node
		_node.rootElement.call(nodeDrag);
	});

	
};