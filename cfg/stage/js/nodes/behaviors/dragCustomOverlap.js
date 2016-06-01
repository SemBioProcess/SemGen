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
								if (n.submodelinput || n.nodeType != _node.nodeType) {
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
					x: _node.x,
					y: _node.y,
				};
				
				
				
			});
		    _node.drag.push(function (d) {
		        
		        // Check whether the node we're dragging is overlapping
		        // any of the other nodes. If it is update the UI.
		    	validDepNodes.forEach(function (node) {

					var leftBound = node.x - node.r - DropZoneSideLength/2;
					var rightBound = node.x + node.r + DropZoneSideLength/2;
					var upperBound = node.y + DropZoneSideLength;
					var lowerBound = node.y - node.r*2;
					if(_node.x >= leftBound &&
						_node.x <= rightBound &&
						_node.y >= lowerBound &&
						_node.y <= upperBound)
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
		        	var modelstomerge =  _node.parent.name + '.' + _node.name + "," + mergeNode.parent.name + '.' + mergeNode.name;
		        	var modelindex = _node.getRootParent().index;
		        	sender.createCustomOverlap(modelstomerge, modelindex);
		        	mergeNode = null;
		        	
		        	// Move the node back to its original location
		        	_node.px = originalLocation.x;
		        	_node.py = originalLocation.y;
		        	_node.x = originalLocation.x;
		        	_node.y = originalLocation.y;
		        	
		        }
		        
		        
		        
		    });	
};

