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
		var scaledX, scaledY;

		 var string = $(".canvas").attr("transform");
	        if(string === undefined) {
	        	scaledX = this.xpos(), scaledY = this.ypos();
	        }
	        else {
		        var translate = string.substring(string.indexOf("(") + 1, string.indexOf(")")).split(","),
		            scaleStr = string.substring(string.lastIndexOf("(") + 1, string.lastIndexOf(")")),
		            dx = Number(translate[0]), dy = Number(translate[1]), scale = 1/Number(scaleStr),
		            scaledX = (this.xpos() - dx)*scale, scaledY = (this.ypos() - dy)*scale;
	        }
	        
		return (Math.sqrt(Math.pow(overlapnode.xpos()-scaledX, 2) + Math.pow(overlapnode.ypos()-scaledY, 2))+overlapnode.r*2 <= this.width*proximityfactor);
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