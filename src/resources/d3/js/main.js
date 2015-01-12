$(window).load(function load() {
	javaCommandReciever.onLoadGraph(function (data) {
		// Listen for clicks on the graph type buttons
		$(".graphTypes>li").click(showGraph);

		var color = d3.scale.category10();
		
		// Build the legend
		var legend = $(".legend")[0];
		var groupsWithColors = {};
		for(group in graph.groups)
		{
			var className = "legendItem" + group;
			legend.innerHTML +=
				"<li class='" + className + "'>" +
					graph.groups[group]	+																// Group name
					"<style> ." + className + ":before { color: " + color(group) + "; } </style>" +		// Group color
				"</li>";
		}
		
		// Simulate a click on the default graph button so the default graph shows
		$(".defaultGraphButton").click();
	});
});

function showGraph() {
	// Deselect the currently selected button
	$(".selected").removeClass("selected");
	
	// Ensure all frames are hidden
	$(".graphFrame").hide();
	
	var selectedButton = $(this);
	
	// Select the button that was clicked
	selectedButton.addClass("selected");
	
	// Show the frame associated with this button
	$("." + selectedButton.attr("data-graph-className")).fadeIn();
}