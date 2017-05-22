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
			box.attr("width", d3.event.x)
				.attr("height", d3.event.y);
		})
		.on("end", function() {
			stage.select("#boundrect").remove();
		});

	return stagedrag;
	
}