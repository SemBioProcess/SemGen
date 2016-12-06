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
				
				_node.multiDrag().forEach(function(n) {
					n.rootElement.selectAll("circle").attr("r", _node.r*2);
					n.fx = _node.xpos();
					n.fy = _node.ypos();	
				});
				//Execute any drag behaviors unique to the node type
				_node.dragstart.forEach(function(behavior) {
					behavior(_node);
				});
				
				_node.graph.tick();
			})
		    .on("drag", function (d, i) {
		    	var dx = d3.event.x - _node.xpos(),
		    		dy = d3.event.y - _node.ypos();
		    	
		    	_node.multiDrag().forEach(function(n) {
		    		var posx = n.xpos()+dx,
		    		    posy = n.ypos()+dy;

		    		if (posx > n.graph.w-15 ) {
		    			posx = n.graph.w-15;
		    		}
		    		else if (posx < 15 ) {
		    			posx = 15;
		    		}
		    		if (posy > n.graph.h-30 ) {
		    			posy = n.graph.h-30;
		    		}
		    		else if (posy < 15 ) {
		    			posy = 15;
		    		}
		    		n.setLocation(posx, posy);
		    	});
		    	//Execute any drag behaviors unique to the node type
		    	_node.drag.forEach(function(behavior) {
		    		behavior(_node);
				});
				_node.graph.tick();
		    	
		    })
		    .on("end", function (d, i) {
		    	_node.multiDrag().forEach(function(n) {
			    	n.rootElement.selectAll("circle").attr("r", n.r);
			    	n.setLocation(n.fx, n.fy);
		    	});
		    	//Execute any drag behaviors unique to the node type	
		    	_node.dragend.forEach(function(behavior) {
		    		behavior(_node);
				});
		    	
		    	_node.multiDrag().forEach(function(n) {
			    	if (!n.fixed) {
			    		n.fx = null;
						n.fy = null;	
			    	}
		    	});

		    	if (!_node.graph.fixedMode) {
		    		_node.graph.resume();
		    	}
		    });
		
		// Add the dragging functionality to the node
		_node.rootElement.call(nodeDrag);
	});

};