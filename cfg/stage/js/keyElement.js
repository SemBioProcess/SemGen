function KeyElement (visibleNodeKeys, hiddenNodeKeys, visibleLinkKeys, hiddenLinkKeys) {
	
	this.initialize = function (graph) {
		$(graph).on("postupdate", function () {
			// Update keys for visible nodes
			addKeysToParent(graph, visibleNodeKeys, graph.force.nodes(), "hideNodes");
			
			// Update keys for hidden nodes
			addKeysToParent(graph, hiddenNodeKeys, graph.getHiddenNodes(), "showNodes");

			// Update keys for visible links
			addLinkKeysToParent(graph, visibleLinkKeys, graph.force.links(), "hideLinks");

			// Update keys for hidden links
			addLinkKeysToParent(graph, hiddenLinkKeys, graph.getHiddenLinks(), "showLinks");
		});
	};
	
	// Adds keys to the parent element based on the nodes in the nodes array
	var addKeysToParent = function (graph, parentElement, nodes, func) {
		// Clear all keys
		parentElement.empty();
		
		// Get unique keys
		var keys = {};
		nodes.forEach(function (node) {
			// Some nodes, like hidden label nodes, don't have key info
			if(!node.getKeyInfo)
				return;
			
			var info = node.getKeyInfo();
			keys[info.nodeType] = info;
		});
		
		for(nodeType in keys) {
			var keyInfo = keys[nodeType];
			
			var keyElement = document.createElement("li");
			$(keyElement).text(keyInfo.nodeType);
			keyElement.style.color = keyInfo.color;
			// Put border around "Mediator" text for consistency with node border
			if(keyInfo.nodeType == "Mediator") {
				keyElement.style.webkitTextStroke = ".7px black";
			}

			if(keyInfo.canShowHide) {
				$(keyElement).click(function (e) {
					graph[func]($(e.target).text());
				});
				
				keyElement.className += " canClick";
			}
			
			parentElement.append(keyElement);
		}
	};

	// Adds link keys to the parent element based on the link in the nodes array
	var addLinkKeysToParent = function (graph, parentElement, links, func) {
		// Clear all keys
		parentElement.empty();

		// Get unique keys
		var keys = {};
		links.forEach(function (link) {
			if(!link.getKeyInfo)
				return;

			var info = link.getKeyInfo();
			keys[info.linkType] = info;
		});

		for(linkType in keys) {
			var keyInfo = keys[linkType];

			if(keyInfo.canShowHide) {
				var keyElement = document.createElement("li");
				$(keyElement).text(keyInfo.linkType);

				$(keyElement).click(function (e) {
					graph[func]($(e.target).text());
				});

				keyElement.className += " canClick";
			}

			parentElement.append(keyElement);
		}
	};
}

KeyElement.instance;
KeyElement.getInstance = function () {
	return KeyElement.instance;
}

$(window).load(function () {
	KeyElement.instance = new KeyElement($(".stageMenu .key ul.visibleNodeKeys"), $(".stageMenu .key ul.hiddenNodeKeys"),
			$(".stageMenu .key ul.visibleLinkKeys"), $(".stageMenu .key ul.hiddenLinkKeys"));
});