/**
 *
 */
function parentDrag(parent) {
	parent.dragstart.push(function (d) {
		// Set children to fixed if not already
		if (!d.showchildren) return;
		if(!d.graph.fixedMode) {
			d.globalApply(function (node) {
				node.oldfixed = node.fixed;
				node.fixed = true;
			});
		}
	});
	parent.drag.push(function (d) {
		   // Drag functionality
		if (d.showchildren) {
			if (
				(d3.event.dx + d.xmin) > 10
				&& (d3.event.dx + d.xmax) < d.graph.w-10
				&& (d3.event.dy + d.ymin) > 10
				&& (d3.event.dy + d.ymax) < d.graph.h-10
			){
				d.globalApply(function(d) {
				d.setLocation(d.xpos() + d3.event.dx, d.ypos() + d3.event.dy);
					d.px += d3.event.dx;
					d.py += d3.event.dy;
				});
			}
			else {
				d.setLocation(d.xpos(), d.ypos());
				$(parent).triggerHandler("dragend");
			}
		}
	});

	parent.dragend.push(function (d) {
			// Children no longer fixed
		if(!d.graph.fixedMode) {
			d.globalApply(function (node) {
				node.fixed = node.oldfixed;
			});
		}
	});
};
