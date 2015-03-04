function KeyElement (visibleNodeKeys, hiddenNodeKeys) {
	
	this.initialize = function (graph) {
		$(graph).on("postupdate", function () {
			// Update keys for visible nodes
			addKeysToParent(graph, visibleNodeKeys, graph.force.nodes(), "hideNodes");
			
			// Update keys for hidden nodes
			addKeysToParent(graph, hiddenNodeKeys, graph.getHiddenNodes(), "showNodes");
		});
	};
	
	// Adds keys to the parent element based on the nodes in the nodes array
	var addKeysToParent = function (graph, parentElement, nodes, func) {
		// Clear all keys
		parentElement.empty();
		
		// Get unique keys
		var keys = {};
		nodes.forEach(function (node) {
			var info = node.getKeyInfo();
			keys[info.nodeType] = info;
		});
		
		for(nodeType in keys) {
			var keyInfo = keys[nodeType];
			
			var keyElement = document.createElement("li");
			$(keyElement).text(keyInfo.nodeType);
			keyElement.style.color = keyInfo.color;
			$(keyElement).click(function (e) {
				graph[func]($(e.target).text());
			});
			parentElement.append(keyElement);
		}
	};
}

KeyElement.instance;
KeyElement.getInstance = function () {
	return KeyElement.instance;
}

$(window).load(function () {
	KeyElement.instance = new KeyElement($(".stageMenu .key ul.visibleNodeKeys"), $(".stageMenu .key ul.hiddenNodeKeys"));
});