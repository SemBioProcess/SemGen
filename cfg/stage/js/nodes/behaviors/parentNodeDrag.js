/**
 * 
 */
function parentDrag (parent) {
	$(parent).on('createVisualization', function (e, root) {
			var parentDrag = d3.behavior.drag()
				.on("dragstart", function (d, i) {
					// Set children to fixed if not already
					if(!d.graph.fixedMode && d.children) {
						d.children.forEach(function (child) {
							child.setFixed(true);
						});
						
					}
				})
				.on("drag", function (d, i) {
					   // Drag functionality
					if (d.children) {
						if (
							(d3.event.dx + d.xmin) > 10
							&& (d3.event.dx + d.xmax) < d.graph.w-10
							&& (d3.event.dy + d.ymin) > 10
							&& (d3.event.dy + d.ymax) < d.graph.h-10) {
							d.x += d3.event.dx;
							d.y += d3.event.dy; 
							d.px += d3.event.dx;
							d.py += d3.event.dy;
							
							translateChildren(d);
					        d.graph.tick();
						}
						else { 
							d.x = d.x;
							d.y = d.y;
							triggerHandler("dragend");
						}
					}
					else {
				        d.px += d3.event.dx;
				        d.py += d3.event.dy;
				        d.x += d3.event.dx;
				        d.y += d3.event.dy; 
				        d.graph.tick();
					}
				})
				
				.on("dragend", function (d, i) {
					// Children no longer fixed
					if(!d.graph.fixedMode && d.children) {
						d.children.forEach(function (node) {
							fixateChildren(node);
						});
					}
					d.graph.tick();
				});
			  // Add the dragging functionality to the node
			parent.rootElement.call(parentDrag);	
	});	    
}

function translateChildren(parent) { 
	parent.children.forEach(function (node) {
    	node.x += d3.event.dx;
    	node.y += d3.event.dy;
    	node.px += d3.event.dx;
    	node.py += d3.event.dy;
    	if (node.children) {
    		translateChildren(node);
    	}
    });
}

function fixateChildren(node) { 
	node.setFixed(false);
	if (node.children) {
		fixateChildren(node);
	}
}