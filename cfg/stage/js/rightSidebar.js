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
		var charge = $("#nodecharge").val();
		graph.setNodeCharge(parseInt(charge));

	});
	$("#linklength").change(function() {
		var length = $("#linklength").val();
		graph.setLinkLength(parseInt(length));

	});
	$("#chargedist").change(function() {
		var length = $("#chargedist").val();
		graph.setChargeDistance(parseInt(length));

	});
	$("#friction").change(function() {
		var friction = $("#friction").val();
		graph.setFriction(parseInt(friction));

	});
	$("#gravity").bind('change', function() {
		var gravity = this.checked;
		graph.toggleGravity(gravity);

	});
}
