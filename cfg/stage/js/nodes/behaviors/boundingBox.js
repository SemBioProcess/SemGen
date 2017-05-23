/**
 * 
 */

function BoundingBox() {
	
	var stage = d3.select("#svg"),
	clickloc = {x: 0, y: 0},
	box = null;
	
	var stagedrag = d3.drag()
		.on("start", function() {
			clickloc.x = d3.event.x;
			clickloc.y = d3.event.y;
			box = stage.append("rect")
				.attr("id", "boundrect")
				.attr("x", clickloc.x)
				.attr("y", clickloc.y)
				.attr("width", 1)
				.attr("height", 1);
		})
		.on("drag", function() {

			if (d3.event.x < clickloc.x) {
				box.attr('x', d3.event.x)
			}
			if (d3.event.y < clickloc.y) {
				box.attr('y', d3.event.y)
			}


			box.attr("width", Math.abs(d3.event.x-clickloc.x))
			.attr("height", Math.abs(d3.event.y-clickloc.y));
			
		})
		.on("end", function() {
			stage.select("#boundrect").remove();
		});

	return stagedrag;
	
}