/**
 * 
 */

function StageDoodad(graph, id, proportionalx, proportionaly, proportionalwidth, proportionalheight, graphic, text) {
	this.graph = graph;
	this.id = id;
	this.className = "doodad";
	this.x;
	this.y;
	this.width = 30 * proportionalwidth;
	this.height = 30 * proportionalheight;
	this.graphic = graphic;
	this.text = text;
	this.propx = proportionalx;
	this.propy = proportionaly;
	
	this.defaultopacity = 1.0;
	
	this.xpos = function () {
		return this.x;
	}

	this.ypos = function () {
		return this.y;
	}
	this.isOverlappedBy = function(overlapnode, proximityfactor) {
		return (Math.sqrt(Math.pow(overlapnode.xpos()-this.xpos(), 2) + Math.pow(overlapnode.ypos()-this.ypos(), 2))+overlapnode.r*2 <= this.width*proximityfactor);
	}
	this.setLocation(proportionalx, proportionaly);

	this.updatePosition = function() {
		this.setLocation(this.propx, this.propy);
	}
}

StageDoodad.prototype.setLocation = function (proportionalx, proportionaly) {
	this.x = Math.round(this.graph.w * proportionalx);
	this.y = Math.round(this.graph.h * proportionaly);
}

StageDoodad.prototype.createVisualElement = function () {
	var doodads = $("#doodads");
	this.updatePosition();
	doodads.append('<span id="' + this.id + '" class="' + this.graphic + '" style="position:fixed; top:' + 
				this.ypos() + 'px; left:' + this.xpos() + 'px; width:' + this.width + 'px; height:' + this.height + 'px;">' + this.text + '</span>');	
}