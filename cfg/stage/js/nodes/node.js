/**
 * Represents a node in the d3 graph
 */


function Node(graph, srcobj, parent, r, textSize, charge) {
	if(!graph)
		return;

	this.graph = graph;
	this.srcobj = srcobj;
	this.name = srcobj.name;
	this.id = srcobj.id;
	this.hash = srcobj.hash;
	this.displayName = this.name;
	this.r = r;
	this.showchildren = false;
	this.textSize = textSize;
	this.nodeType = NodeTypeArray[srcobj.typeIndex];
	this.charge = charge;
	this.defaultcharge = charge;
	this.className = "node";
	this.element;
	this.parent = parent;
	this.hidden = false;
	this.canlink = true;
	this.textlocx = 0;
	this.locked = false; //Make node immutable on stage (allow dragging and selection)
	this.selected = false;
	
	this.wasfixed = false;
	this.defaultopacity = 1.0;
	
	this.timer = null;
	this.clicks = 0;
	validateNode(this);
	this.dragstart = [];
	this.drag = [];
	this.dragend = [];
	this.ghostdragend = [];
	this.addBehavior(NodeDrag);
	
	this.x = srcobj.xpos;
	this.y = srcobj.ypos;
	
	this.xpos = function () {
		return this.x;
	}

	this.ypos = function () {
		return this.y;
	}
	
	this.isOverlappedBy = function(overlapnode, proximityfactor) {
		return (Math.sqrt(Math.pow(overlapnode.xpos()-this.xpos(), 2) + Math.pow(overlapnode.ypos()-this.ypos(), 2))+overlapnode.r*2 <= this.r*proximityfactor);

	}
	
	
	this.isHidden = function() {
		return this.srcobj.hidden;
	}

	
}

//Get this node's top level parent
Node.prototype.getRootParent = function () {
	var node = this;
	while (node.parent!=null) {
		node = node.parent;
	}
	return node;
}

Node.prototype.addClassName = function (className) {
	this.className += " " + className;
}

Node.prototype.hasClassName = function (className) {
	return this.className.indexOf(className) > -1;
}

Node.prototype.spaceBetweenTextAndNode = function() {
	return this.r * 0.2 + this.textSize;
}

Node.prototype.addBehavior = function (behavior) {
	// Behaviors are just functions that take in a node as an argument
	// To add a behavior all we need to do is call the function
	//
	// Note: I added this function to make adding a behavior easier to read
	// (e.g. this.addBehavior(SomeBehavior); )
	behavior(this);
}

Node.prototype.setLocation = function (x, y) {
	x = Math.max(this.r, Math.min(this.graph.w - this.r*2, x));
	y = Math.max(this.r, Math.min(this.graph.h - this.r + this.spaceBetweenTextAndNode(), y));

	this.x = x; this.y = y;
	
	this.srcobj.xpos = x;
	this.srcobj.ypos = y;
	if (this.fixed || this.fx !=null) {
		this.fx = x;
		this.fy = y;
	}
}

Node.prototype.createVisualElement = function (element, graph) {
	//Randomly choose a location if one has not been set
	if (this.srcobj.xpos<=10 || this.srcobj.ypos <= 10) {
		if (this.parent) {
			this.setLocation(this.parent.xpos() + Math.random()*200 - 100, this.parent.ypos() + Math.random()*200 - 100);
			
		}
		else {
			this.setLocation(
					(Math.random() * (graph.w-graph.w/3))+this.graph.w/6,
					(Math.random() * (graph.h-graph.h/2))+this.graph.h/6
				);
		}

	} 
	var node = this;
	this.rootElement = d3.select(element);
	this.rootElement.attr("class", this.className)
    	.style("fill", this.nodeType.color)
    	.attr("id", "Node;"+this.id);

	if(this.nodeType != NodeType.NULLNODE) {
	
		var circleSelection = this.rootElement.append("circle")
											.attr("r", this.r)
	
											.attr("class","nodeStrokeClass")
											.on("mouseover", function (d) {
												graph.highlightMode(d);
											})
											.on("mouseout", function () {
												graph.highlightMode(null);
											});


        circleSelection.attr("stroke", "black")
            .attr("stroke-width", 0.5);

		this.rootElement.on("click", function (node) {
			node.onClick();
		});

		//Append highlight circle
		this.rootElement.append("circle")
			.attr("class", "highlight")
			.attr("r", this.r + 4)
			.attr("stroke", "#fdc751")
			.attr("stroke-width", "4");
	
		//Create the text elements
		this.createTextElement("shadow");
		this.createTextElement("real");
		
	}

	this.rootElement.attr("transform", "translate(" + node.xpos() + "," + node.ypos() + ")");
	$(this).triggerHandler('createVisualization', [this.rootElement]);
}

Node.prototype.getLinks = function () {
	return [];
}

Node.prototype.tickHandler = function (element, graph) {
	$(this).triggerHandler('preTick');
	var node = this;
	//Keep child nodes centered on parent
	var forcey = 0;
	var forcex = 0;

	if (this.parent && this.graph.active) {
			var k = .0001;
			forcey = (this.parent.ypos() - this.ypos()) * k;
			forcex = (this.parent.xpos() - this.xpos()) * k;

	}
	this.setLocation(
			this.xpos()+forcex, this.ypos()+forcey
	)
	
	var root = d3.select(element);
	//Keep the text above hull when an parent node is opened.
	if ((this.showchildren) && (this.nodeType != NodeType.NULLNODE)) {
		this.rootElement.selectAll("text").attr("y", -node.spaceBetweenTextAndNode())
		this.rootElement.selectAll("text").attr("x", -(node.xpos() - (this.xmax + this.xmin)/2.0));
	}
	
	this.rootElement.attr("transform", "translate(" + node.xpos() + "," + node.ypos() + ")");
	$(this).triggerHandler('postTick');
}

Node.prototype.createTextElement = function (className) {
	var node = this;
	this.rootElement.append("svg:text")
		.attr("font-size", this.textSize + "px")
	    .attr("x", 0)
	    .attr("y", -node.spaceBetweenTextAndNode())
	    .text(this.displayName)
	    .attr("class", className)
	    .attr("text-anchor", "middle");
}


Node.prototype.highlight = function () {
	if (this.rootElement) 
		this.rootElement.classed("selected", this.rootElement.select("circle").style("display")!="none");
}

Node.prototype.removeHighlight = function () {
	if (this.rootElement) 
		this.rootElement.classed("selected", false);
}

Node.prototype.onClick = function () {
	this.clicks++;

	if(this.clicks == 1) {

		node = this;
		this.timer = setTimeout(function() {
			node.clicks = 0;             //after action performed, reset counter
	        main.task.selectNode(node);
	    }, 500);

	}
    else {
        clearTimeout(this.timer);    //prevent single-click action
        this.clicks = 0;             //after action performed, reset counter
        if (!this.locked) {
        	this.onDoubleClick();
        }
    }
    d3.event.stopPropagation();

}

Node.prototype.isVisible = function () {
	var parent = this.parent;
	var show = true;
	while (parent && show) {
		show = parent.showchildren;
		parent = parent.parent;
	} 
	return (show && !this.hidden && this.graph.nodesVisible[this.nodeType.id]);
}

Node.prototype.onDoubleClick = function () {}

Node.prototype.globalApply = function (funct) {
	funct(this);
}

//Traverse children until condition is met or all children are traversed
Node.prototype.globalApplyUntilTrue = function (funct) {
	return funct(this);
}


Node.prototype.applytoChildren = function(funct) {}


Node.prototype.multiDrag = function() {
	return main.task.selectedNodes;
}

function validateNode(nodeData) {
	if(!nodeData)
		throw "Invalid node data";

	if(typeof nodeData.id != "string")
		throw "Node id must be a string";

	if(typeof nodeData.r != "number")
		throw "Node radius must be a number";

	if(typeof nodeData.charge != "number")
		throw "Charge must be a number";

	if(typeof nodeData.createVisualElement != "function")
		throw "Node createVisualElement is not defined";

	if(typeof nodeData.tickHandler != "function")
		throw "Node tickHandler is not defined";

};

Node.prototype.createGhost = function() {
	return new GhostNode(this);
}