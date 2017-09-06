/**
 *
 */
function parentDrag(parent) {
	parent.dragstart.push(function (d) {
		// Set children to fixed if not already
		if (!d.showchildren) return;
		if(!d.graph.fixedMode) {
			d.applytoChildren(function (node) {
				node.fx = node.xpos();
				node.fy = node.ypos();	
			});
		}
	});
	parent.drag.push(function (d) {
        // Drag functionality
        var dx = dy = 0;
        dx = d3.event.dx;
        dy = d3.event.dy;

        d.applytoChildren(function(n) {
            n.setLocation(n.xpos() + dx, n.ypos() + dy);

        });
        d.setLocation((d.xmin+d.xmax)/2, (d.ymin+d.ymax)/2);

	});

	parent.dragend.push(function (d) {
		// Children no longer fixed
		if(!d.graph.fixedMode) {
			d.applytoChildren(function (node) {
				node.setLocation(node.x, node.y);
				if (!node.fixed) {
					node.fx = null;
					node.fy = null;	
				}
			});
		}
	});
};
