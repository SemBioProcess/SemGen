/**
 * 
 */
function parentDrag (parent) {
	$(parent).on('createVisualization', function (e, root) {
		
		var parentDrag = d3.behavior.drag()
			.on("dragstart", function (d, i) {
				// Set children to fixed if not already
				if(!d.graph.fixedMode) {
					d.children.forEach(function (child) {
						child.setFixed(true);
					});
					
				}
				//parent.graph.tick();
			})
			.on("drag", function (d, i) {
				   // Drag functionality
				d.x += d3.event.dx;
				d.y += d3.event.dy; 
				d.px += d3.event.dx;
				d.py += d3.event.dy;
		        d.children.forEach(function (node) {
		        	node.x += d3.event.dx;
		        	node.y += d3.event.dy;
		        	node.px += d3.event.dx;
		        	node.py += d3.event.dy;
		        });
		        d.graph.tick();
			})
			
			.on("dragend", function (d, i) {
				// Children no longer fixed
				if(!d.graph.fixedMode) {
					d.children.forEach(function (node) {
						node.setFixed(false);
					});
				}
				d.graph.tick();
			});
		  // Add the dragging functionality to the node
		parent.rootElement.call(parentDrag);
	});	        
}