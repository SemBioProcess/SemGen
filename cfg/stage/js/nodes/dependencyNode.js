/**
 * Represents a dependency node in the d3 graph
 */

DependencyNode.prototype = new Node();
DependencyNode.prototype.constructor = DependencyNode;

function DependencyNode (graph, data, parentNode) {
	// Ensure the node type is formatted properly

	Node.prototype.constructor.call(this, graph, data, parentNode, 5, 12, graph.nodecharge);
	this.submodelid = data.submodelId;
	this.submodelinput = data.issubmodelinput;
	
	this.addClassName("dependencyNode");
	//this.addBehavior(Columns);
	var _node = this;
	graph.depBehaviors.forEach(function(b) {
		_node.addBehavior(b);
	});
	this.addBehavior(HiddenLabelNodeGenerator);

	this.isOrphaned = function() {
		return data.isorphaned;
	}
}

DependencyNode.prototype.createVisualElement = function (element, graph) {
	Node.prototype.createVisualElement.call(this,element, graph);
	if (this.submodelinput) {
		this.defaultopacity = 0.6;
		this.rootElement.selectAll("circle").attr("opacity", this.defaultopacity);
	}	

}

DependencyNode.prototype.getFirstLinkableAncestor = function() {
	var outputNode = this;
	while (!outputNode.isVisible() && outputNode.canlink) {
		outputNode = outputNode.parent;
	}
	if (!outputNode.canlink) outputNode = null;
	return outputNode;
}

DependencyNode.prototype.getLinks = function (linklist) {
	
	// Build an array of links from our list of inputs
	var links = [];
	if (this.nodeType.id == NodeType.ENTITY.id) {
		return links;
	}
	var fade = false;
	var intraSubmodelLink = false;
	if (!this.graph.nodesVisible[this.nodeType.id] ) {
		//return links;
		fade = true;
	}
	var outputNode = this.getFirstLinkableAncestor();
	if (!outputNode) return links; 
	
	this.srcobj.inputs.forEach(function (link) {
		var inputNode = outputNode.graph.findNode(link.input.id);
		
		if (!inputNode.graph.nodesVisible[inputNode.nodeType.id]) {
			if (inputNode.parent == this.parent) {
				return links;
			}
			fade = true;
		}
		// Compare submodel names and check if they are from different submodels
		if (outputNode.submodelinput) {
            intraSubmodelLink = true;
        }

		inputNode = inputNode.getFirstLinkableAncestor();
		if (inputNode==null) {
			return links;
		}
		else if (inputNode.parent == outputNode) {
			return links;
		}
		if (!inputNode || inputNode==outputNode) return;
		//Check for duplicate links
			for (l in linklist) {
				var exisstinglink = linklist[l];
				if (exisstinglink.linksNodes(inputNode, outputNode)) {
					return;
				}
				else if (exisstinglink.linksNodes(outputNode, inputNode)) {
					exisstinglink.bidirectional = true;
					return;
				}
			}
		
		var length = outputNode.graph.linklength;
		links.push(new Link(outputNode.graph, link, outputNode, inputNode, length, fade, intraSubmodelLink));
	});

	return links;
}

DependencyNode.prototype.hasIntermodalLink = function() {
	for (x in this.srcobj.inputs) {
		if (this.srcobj.inputs[x].linklevel > 1) {
			return true;
		}
	}
	return false;
}

DependencyNode.prototype.removeLink = function(inputnode) {
	var srcinputs =this.srcobj.inputs;
	var loc = null;
	for (i in srcinputs) {
		if (srcinputs[i].output == inputnode.id) {
			loc = i;
			break;
		}
	}
	if (i != null) {
		srcinputs.splice(i, 1);
	}
	return false;
}

DependencyNode.prototype.isVisible = function () {
	if (this.srcobj.isorphaned && !this.graph.showorphans) return false;  
	return Node.prototype.isVisible.call(this);
}