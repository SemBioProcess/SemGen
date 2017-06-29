/**
 * Draws a hull around child nodes
 * @param node
 */
function Hull(node) {
	var hull;
	var children = null;
	
	$(node).on('createVisualization', function (e, root) {
		hull = root.append("path")
			.attr("class", "hull")
			.style("opacity", .1)
			.attr("stroke", root.style("fill"))
			.attr("fill", root.style("fill"))
			.on("dblclick", function(d) {
                node.showchildren = false;
                node.rootElement.selectAll("text").attr("x", 0);
                if (!node.fixed) {
                    node.fx = null;
                    node.fy = null;
                }
                node.setLocation((node.xmin+node.xmax)/2, (node.ymin+node.ymax)/2);
                node.graph.update();
                main.task.selectNode(node);
            });
	});
	
	$(node).on('childrenSet', function (e, newChildren) {
		// If there are children show the hull. Otherwise, show the node
		this.rootElement.select(".hull").style("display", node.showchildren ? "inherit" : "none");
	});
	
	$(node).on('preTick', function () {
		
		// Draw the hull around child nodes
		if(node.showchildren) {
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
					// by hidden nodes
					if(!child.isVisible())
						return;

					// If the child has children analyze them as well
					if(child.showchildren) {
						analyzeChildren(getSymbolArray(child.children));
						return;
					}
					
					vertexes.push([child.xpos(), child.ypos()]);
					//Find the most extreme node positions for each axis
					minX = minX || child.xpos();
					minX = child.xpos() < minX ? child.xpos() : minX;
					
					maxX = maxX || child.xpos();
					maxX = child.xpos() > maxX ? child.xpos() : maxX;
					
					minY = minY || child.ypos();
					minY = child.ypos() < minY ? child.ypos() : minY;
					
					maxY = maxY || child.ypos();
					maxY = child.ypos() > maxY ? child.ypos() : maxY;
				});
			};
			analyzeChildren(getSymbolArray(node.children));

			//If there are zero children make a hull centered on the parent node (used for merge previews)
			if (vertexes.length ==0) {
				
				minX = node.xpos() - 6;
				maxX = node.xpos() + 6;
				minY = node.ypos() - 6;
				maxY = node.ypos() + 6;
				vertexes.push([minX, minY]);
				vertexes.push([maxX, minY]);
				vertexes.push([minX, maxY]);
				vertexes.push([maxX, maxY]);

			}
			
						//If there is only one visible child, make the hull a circle with a radius of 6
			if (vertexes.length ==1) {
				vertexes.length = 0;
				minX = minX - 6;
				maxX = maxX + 6;
				minY = minY - 6;
				maxY = maxY + 6;
				vertexes.push([minX, minY]);
				vertexes.push([maxX, minY]);
				vertexes.push([minX, maxY]);
				vertexes.push([maxX, maxY]);

			}
			//If there are only two visible children, add a vertex centered between the two nodes
			if (vertexes.length ==2) {
				vertexes.push([(maxX + minX)/2, (maxY + minY)/2]);
			}
			
			if(!vertexes.length)
				return;
			
			node.xmin = minX;
			node.xmax = maxX;
			node.ymin = minY;
			node.ymax = maxY;
			if (node.nodeType==NodeType.NULLNODE) return; 
			// Center the node at the top of the hull
			// Draw hull
			node.vertices = vertexes;
			hull.datum(d3.polygonHull(vertexes)).attr("d", function(d) { 
						return  "M" + d.join("L") + "Z";})
						.attr("transform", "translate(" + -node.xpos() + "," + -node.ypos() + ")");
			}
	});
}