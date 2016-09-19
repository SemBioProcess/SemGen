/**
 * Drag to create a custome overlap in the merger
 * @param node
 */


function CreateCustomOverlap(_node) {
	var DropZoneSideLength = 50;

		// Save the original location so we can move the node back
		// if a merge is successful (we don't want two models occupying the same location)
		var originalLocation = {
			x: null,
			y: null,
		};
		var validDepNodes = [];
		var invalidDepNodes = [];
		var mergeNode = null;
		
		_node.dragstart.push(function (d) {
			//if (_node.hasIntermodalLink()) return; 
			
				//Refresh node references
			validDepNodes = [];
			invalidDepNodes = [];
			var models = [];
			//Get the other model
				_node.graph.getModels().forEach(function(model) {
					if (model!=_node.getRootParent()) {
						models.push(model);
					}
				});
				//Get all dependency nodes from the other model and store them in the correct container
				//Set dependecy nodes with in inputs to 10% opacity.
				models.forEach(function(model) {
					model.applytoChildren(function(n) {
						if (n.hasClassName("dependencyNode")) {
							if (n.rootElement) {
								if (n.submodelinput || n.nodeType != _node.nodeType || n.hasIntermodalLink()) {
									invalidDepNodes.push(n); 
									n.rootElement.selectAll("circle").attr("opacity", "0.1");
								}
								else {
									validDepNodes.push(n);
								}
							}
						}
					});
				});
				
				
				// Save the original location
				originalLocation = {
					x: _node.xpos(),
					y: _node.ypos(),
				};

			});
		    _node.drag.push(function (d) {
		    	//if (_node.hasIntermodalLink()) return; 
		        // Check whether the node we're dragging is overlapping
		        // any of the other nodes. If it is update the UI.
		    	validDepNodes.forEach(function (node) {

					var leftBound = node.xpos() - node.r - DropZoneSideLength/2;
					var rightBound = node.xpos() + node.r + DropZoneSideLength/2;
					var upperBound = node.ypos() + DropZoneSideLength;
					var lowerBound = node.ypos() - node.r*2;
					if(_node.xpos() >= leftBound &&
						_node.xpos() <= rightBound &&
						_node.ypos() >= lowerBound &&
						_node.ypos() <= upperBound)
					{
						mergeNode = node;
						node.rootElement.selectAll("circle").attr("r", node.r*2);
					}
					else {
						node.rootElement.selectAll("circle").attr("r", node.r);
						if(node == mergeNode) {							
							mergeNode = null;
						}
					}
				});
		    });
		    _node.dragend.push(function (d) {
		    	//if (_node.hasIntermodalLink()) return; 
		    	// If the node was dropped on another node then merge the two
		    	
		    	// Remove any classes we may have set
		        validDepNodes.forEach(function (node) {
		        	node.rootElement.selectAll("circle").attr("r", _node.r);
				});
		        invalidDepNodes.forEach(function (node) {
		        	if (node.rootElement) {
		        		node.rootElement.selectAll("circle").attr("opacity", node.defaultopacity);
		        	}
				});
		        if(mergeNode) {
		        	var modelstomerge;
		        	if (_node.getRootParent().modelindex==0) {
		        		modelstomerge =  _node.id + "," + mergeNode.id;
		        	}
		        	else {
		        		modelstomerge =  mergeNode.id + "," + _node.id;
		        	}
		        	$('.merge').prop('disabled', 'true');
		        	sender.createCustomOverlap(modelstomerge);
		        	
		        	var customlink = {
		        			id : _node.id + "-" + mergeNode.id,
		        			linklevel: 3,
		        			linkType: _node.nodeType.id,
		        			input: mergeNode,
		        			output: _node,
		        			length: 100,
		        			external: true,
		        	
		        	}
		        	_node.srcobj.inputs.push(customlink);
		        	
		        	mergeNode = null;
		        	
		        	// Move the node back to its original location
		        	_node.px = originalLocation.x;
		        	_node.py = originalLocation.y;
		        	_node.setLocation(originalLocation.x, originalLocation.y);
		        	
		        	_node.graph.update();
		        }

		    });	
};

