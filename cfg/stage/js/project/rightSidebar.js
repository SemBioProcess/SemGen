/**
 *
 */

function RightSidebar(graph) {
	this.graph = graph;

	// Fix all nodes when ctrl + M is pressed
	$("#fixedNodes").bind('change', function(){
		graph.toggleFixedMode(this.checked);
	});

	$("#repulsion").change(function() {
		var charge = -1 * $("#repulsion").val();
		graph.setNodeCharge(parseInt(charge));

		// Increase link length as repulsion increases
        var length = 0.5 * $("#repulsion").val();
        graph.setLinkLength(parseInt(length));

	});

    $("#friction").change(function() {
        var friction = $("#friction").val();
        graph.setFriction(parseFloat(friction));

    });

	// Advanced d3 parameters disabled for users

	// $("#linklength").change(function() {
	// 	var length = $("#linklength").val();
	// 	graph.setLinkLength(parseInt(length));
    //
	// });
	// $("#chargedist").change(function() {
	// 	var length = $("#chargedist").val();
	// 	graph.setChargeDistance(parseInt(length));
    //
	// });
	// $("#gravity").bind('change', function() {
	// 	var gravity = this.checked;
	// 	graph.toggleGravity(gravity);
    //
	// });
}
