/**
 * 
 */

function RightSidebar(graph) {
	this.graph = graph;
	
	// Fix all nodes when ctrl + M is pressed
	$("#modes #fixedNodes").bind('change', function(){        
		//Columns.columnModeOn = this.checked;
		
		graph.toggleFixedMode(this.checked);
	});
	
	$("#nodecharge").change(function() {
		var charge = $("#nodecharge").val() + ".0";
		//graph.setNodeCharge(charge);
		
	});
	$("#friction").change(function() {
		var friction = $("#friction").val();
		//graph.setFriction(friction);
		
	});
	$("#gravity").bind('change', function() {
		var gravity = $("#gravity").checked();
		graph.toggleGravity(gravity);
		
	});
}