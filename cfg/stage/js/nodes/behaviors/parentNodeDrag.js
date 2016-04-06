/**
 *
 */
function parentDrag (parent) {
	$(parent).on('createVisualization', function (e, root) {
			var parentDrag = d3.behavior.drag()
				.on("dragstart", function (d, i) {
					// Set children to fixed if not already
					if(!d.graph.fixedMode) {
						d.globalApply(function (node) {
							node.oldfixed = node.fixed;
							node.fixed = true;
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
							&& (d3.event.dy + d.ymax) < d.graph.h-10
						){
							d.globalApply(function(d) {
							d.x += d3.event.dx;
								d.y += d3.event.dy;
								d.px += d3.event.dx;
								d.py += d3.event.dy;
							});
						    d.graph.tick();
						}
						else {
							d.x = d.x;
							d.y = d.y;
							$(parent).triggerHandler("dragend");
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
				if(!d.graph.fixedMode) {
					d.globalApply(function (node) {
						node.fixed = node.oldfixed;
					});
					d.graph.tick();
				}
				});
			  // Add the dragging functionality to the node
			parent.rootElement.call(parentDrag);
	});
}
