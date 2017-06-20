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
		var newOriginX, newOriginY, newDimX, newDimY;

		 var string = $(".canvas").attr("transform");
	        if(string === undefined) {
	        	newOriginX = origin.x, newOriginY = origin.y, newDimX = dim.x, newDimY = dim.y;
	        }
	        else {
		        var translate = string.substring(string.indexOf("(") + 1, string.indexOf(")")).split(","),
		            scaleStr = string.substring(string.lastIndexOf("(") + 1, string.lastIndexOf(")")),
		            dx = Number(translate[0]), dy = Number(translate[1]), scale = 1/Number(scaleStr),
		            newOriginX = (origin.x - dx)*scale, newOriginY = (origin.y - dy)*scale,
		        	newDimX = (dim.x - dx)*scale, newDimY = (dim.y - dy)*scale;
	        }
	        overlapx = (overlapnode.xpos() >= Math.min(newOriginX, newDimX)) && (overlapnode.ypos() <= Math.max(newOriginX, newDimX)),
			overlapy = (overlapnode.ypos() >= Math.min(newOriginY, newDimY)) && (overlapnode.ypos() <= Math.max(newOriginY, newDimY));
				 	
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