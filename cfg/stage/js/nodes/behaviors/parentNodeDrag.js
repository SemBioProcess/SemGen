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
			var dx = 0;
			var dy = 0;
			if (
				(d3.event.dx + d.xmin) > 10
				&& (d3.event.dx + d.xmax) < d.graph.w-10) {
					dx = d3.event.dx;
			}
			
			if (
				(d3.event.dy + d.ymin) > 10
				&& (d3.event.dy + d.ymax) < d.graph.h-10) {
					dy = d3.event.dy;
			}
			d.globalApply(function(n) {
				n.setLocation(n.xpos() + dx, n.ypos() + dy);
				n.px += dx;
				n.py += dy;
			});
		
	
				//d.setLocation(d.xpos(), d.ypos());
			//	$(parent).triggerHandler("dragend");
		}
		else {
			if (
					(d3.event.dx + d.xmin) > 10
					&& (d3.event.dx + d.xmax) < d.graph.w-10
					&& (d3.event.dy + d.ymin) > 10
					&& (d3.event.dy + d.ymax) < d.graph.h-10
				){
					d.applytoChildren(function(d) {
						d.setLocation(d.xpos() + d3.event.dx, d.ypos() + d3.event.dy);
						d.px += d3.event.dx;
						d.py += d3.event.dy;
					});
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
