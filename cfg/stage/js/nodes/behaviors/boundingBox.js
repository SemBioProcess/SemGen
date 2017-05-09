/**
 * 
 */

function BoundingBox() {
	var isdragging = false,
	task = null,
	stage = $('#stage');
	
	stage.mousedown(function() {
		isdragging = false;
	})
	.mousemove(function() {
		isdragging = true;
	})
	.mouseup(function() {
		isdragging = false;
	});
	
	this.setTask = function(currenttask) {
		task = currenttask;
	}
	
}