/**
 * Draws a hull around child nodes
 * @param node
 */
function Hull(node) {
	var hull;
	var children;
	
	$(node).on('createVisualization', function (e, root) {
		hull = root.append("path")
			.attr("class", "hull")
			.style("opacity", .2)
			.attr("stroke", root.style("fill"))
			.attr("fill", root.style("fill"))
			.on("click", function(d) {
				console.log("hull click");
				node.setChildren(null);
			});
	});
	
	$(node).on('childrenSet', function (e, newChildren) {
		children = newChildren;
		
		// If there are children show the hull. Otherwise, show the hull
		this.rootElement.select(".hull").style("display", children ? "inherit" : "none");
	});
	
	$(node).on('preTick', function () {
		// Draw the hull around child nodes
		if(children) {
			// 1) Convert the child positions into vertices that we'll use to create the hull
			// 2) Calculate the center of the child nodes and the top of the child nodes so 
			// 		we can position the text and hidden circle appropriately
			var vertexes = [];
			var centerX = 0;
			var minY = null;
			children.forEach(function (d) {
				// Hull position shouldn't be effected
				// by hidden models
				if(d.hidden)
					return;
				
				vertexes.push([d.x, d.y]);
				
				centerX += d.x;
				minY = minY || d.y;
				minY = d.y < minY ? d.y : minY;
			});
			
			if(!vertexes.length)
				return;
			
			// Center the node at the top of the hull
			node.x = centerX / vertexes.length;
			node.y = minY;
			
			// Draw hull
			hull.datum(d3.geom.hull(vertexes))
				.attr("d", function(d) { return "M" + d.join("L") + "Z"; })
				.attr("transform", "translate(" + -node.x + "," + -node.y + ")");
		}
	});
}