/**
 * 
 */

function BoundingBox(visnodes) {
	
	var boudingbox = this;
	stage = d3.select("#svg"),
	visiblenodes = visnodes,
	origin = {x: 0, y: 0},
	dim = {x: 0, y: 0},
	box = null;
	
	var isOverlappedBy = function(overlapnode) {
		var overlapscreen = overlapnode.getScreenCoordinates(),
		 	overlapx = (overlapscreen[0] >= Math.min(origin.x, dim.x)) && (overlapscreen[0] <= Math.max(origin.x, dim.x)),
		overlapy = (overlapscreen[1] >= Math.min(origin.y, dim.y)) && (overlapscreen[1] <= Math.max(origin.y, dim.y));
		return overlapx && overlapy;
	}
	
	var stagedrag = d3.drag()
		.on("start", function() {
			origin.x = d3.event.x;
			origin.y = d3.event.y;
			box = stage.append("rect")
				.attr("id", "boundrect")
				.attr("x", origin.x)
				.attr("y", origin.y)
				.attr("width", 1)
				.attr("height", 1);
		})
		.on("drag", function() {
			dim.x = d3.event.x, dim.y = d3.event.y;
			
			if (d3.event.x < origin.x) {
				box.attr('x', d3.event.x)
			}
			if (d3.event.y < origin.y) {
				box.attr('y', d3.event.y)
			}

			box.attr("width", Math.abs(d3.event.x-origin.x))
			.attr("height", Math.abs(d3.event.y-origin.y));
			
		})
		.on("end", function() {
			var nodestoselect = [];
			
			visiblenodes.forEach(function(vn) {
				if (vn.showchildren) return;
				if (isOverlappedBy(vn)) {
					
					nodestoselect.push(vn);
				}
			});
			
			stage.select("#boundrect").remove();
			main.task.selectNodes(nodestoselect);

		});

	return stagedrag;
	
}