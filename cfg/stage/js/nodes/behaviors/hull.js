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
			.style("opacity", .1)
			.attr("stroke", root.style("fill"))
			.attr("fill", root.style("fill"))
			.on("dblclick", function(d) {
				node.setChildren(null, null);
				
				node.rootElement.selectAll("text").attr("x", 0);
			});
	});
	
	$(node).on('childrenSet', function (e, newChildren) {
		if (newChildren) {
			children = getSymbolArray(newChildren);
		}
		else {
			children = null;
		}
		
		// If there are children show the hull. Otherwise, show the node
		this.rootElement.select(".hull").style("display", children ? "inherit" : "none");
	});
	
	$(node).on('preTick', function () {
		if (node.id==null) return; 
		// Draw the hull around child nodes
		if(children) {
			// 1) Convert the child positions into vertices that we'll use to create the hull
			// 2) Calculate the center of the child nodes and the top of the child nodes so 
			// 		we can position the text and parent node appropriately
			var vertexes = [];

			var minX = null;
			var maxX = null;
			var minY = null;
			var maxY = null;

			// Recursively analyze all descendants
			var analyzeChildren = function (childrenArr) {
				childrenArr.forEach(function (child) {
					// Hull position shouldn't be effected
					// by hidden models
					if(!child.isVisible())
						return;

					// If the child has children analyze them as well
					if(child.children)
						analyzeChildren(getSymbolArray(child.children));
					
					vertexes.push([child.x, child.y]);
					//Find the most extreme node positions for each axis
					minX = minX || child.x;
					minX = child.x < minX ? child.x : minX;
					
					maxX = maxX || child.x;
					maxX = child.x > maxX ? child.x : maxX;
					
					minY = minY || child.y;
					minY = child.y < minY ? child.y : minY;
					
					maxY = maxY || child.y;
					maxY = child.y > maxY ? child.y : maxY;
				});
			};
			analyzeChildren(children);
			
						//If there is only one visible child, make the hull a circle with a radius of 6
			if (minX == maxX) {
				
				minX = minX - 6;
				maxX = maxX + 6;
				minY = minY - 6;
				maxY = maxY + 6;
				vertexes.push([minX, minY]);
				vertexes.push([maxX, minY]);
				vertexes.push([minX, maxY]);
				vertexes.push([maxX, maxY]);

			}
			
			if(!vertexes.length)
				return;
			
			node.xmin = minX;
			node.xmax = maxX;
			node.ymin = minY;
			node.ymax = maxY;
			
			// Center the node at the top of the hull
			// Draw hull
			hull.datum(d3.geom.hull(vertexes))
				.attr("d", function(d) { return "M" + d.join("L") + "Z"; })
				.attr("transform", "translate(" + -node.x + "," + -node.y + ")");
		}
	});
}