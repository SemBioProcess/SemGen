
function KeyElement (visibleNodeKeys, hiddenNodeKeys, visibleLinkKeys, hiddenLinkKeys) {
	
	this.initialize = function (graph) {
		$(graph).on("postupdate", function () {
			visibleNodeKeys.empty();
			hiddenNodeKeys.empty();
			
			addKeyToParent(graph, visibleNodeKeys, modelKey, null);
			
			if (graph.vismode==ShowSubmodels) {
				showSubmodelKeys(graph,visibleNodeKeys, hiddenNodeKeys );
			}
			else if (graph.vismode==ShowDependencies) {
				showDependencyKeys(graph,visibleNodeKeys, hiddenNodeKeys );
			}
			else {
				showPhysioMapKeys(graph,visibleNodeKeys, hiddenNodeKeys );
			}
			
			// Update keys for visible links
			addLinkKeysToParent(graph, visibleLinkKeys, graph.force.links(), "hideLinks");

			// Update keys for hidden links
			addLinkKeysToParent(graph, hiddenLinkKeys, graph.getHiddenLinks(), "showLinks");
		});
	};
	
	
	
	var showSubmodelKeys = function(graph, visibleNodeKeys, hiddenNodeKeys) {
		addKeyToParent(graph, visibleNodeKeys, submodelKey, null);
		
		showDependencyKeys(graph, visibleNodeKeys, hiddenNodeKeys);
	}
	
	var showDependencyKeys = function(graph, visibleNodeKeys, hiddenNodeKeys) {
		var i = 0;
		var key;
		graph.activedeptypes.forEach(function (shown) {
			if (shown) {
				addKeyToParent(graph, visibleNodeKeys, depenKey(i), "showNodes");
			}
			else {
				addKeyToParent(graph, hiddenNodeKeys, depenKey(i), "hideNodes");
			}
			i++;
		}) 		
		
	}
	
	var showPhysioMapKeys = function(graph, visibleNodeKeys, hiddenNodeKeys) {
		var i = 0;
		graph.activephysmaptypes.forEach(function (shown) {
			if (shown) {
				addKeyToParent(graph, visibleNodeKeys, physmapKey(i), "showNodes");
			}
			else {
				addKeyToParent(graph, hiddenNodeKeys, physmapKey[i], "hideNodes");
			}
			i++;
		}) 	
	}
	
	var addKeyToParent = function (graph, parentElement, keyInfo, func) {
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
	KeyElement.instance = new KeyElement($("#stagemenu #stagekey ul.visibleNodeKeys"), $("#stagemenu #stagekey ul.hiddenNodeKeys"),
			$("#stagemenu #stagekey ul.visibleLinkKeys"), $("#stagemenu #stagekey ul.hiddenLinkKeys"));
});