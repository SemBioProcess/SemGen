
function KeyElement (visibleNodeKeys, hiddenNodeKeys, visibleLinkKeys, hiddenLinkKeys) {
	var addedKeys = [];
	
	this.initialize = function (graph) {
		$(graph).on("postupdate", function () {
			addedKeys = [];
			visibleNodeKeys.empty();
			hiddenNodeKeys.empty();
			
			var activemodes = [false, false, false];
			graph.getModels().forEach(function (model) {
				activemodes[model.displaymode] = true;
			});
			
			var i = 0;
			for (x in DisplayModes) {
				if (activemodes[i]) {
					DisplayModes[x].keys.forEach(function(type) {
						if (graph.nodesVisible[type.id]) {
							addKeyToParent(graph, visibleNodeKeys, type, "hideNodes");
						}
						else {
							addKeyToParent(graph, hiddenNodeKeys, type, "showNodes");
						}
					});
				}
				i++;
			}

			// Update keys for visible links
			addLinkKeysToParent(graph, visibleLinkKeys, graph.force.force("link").links(), "hideLinks");
		});
	};
	
	var addKeyToParent = function (graph, parentElement, keyInfo, func) {
			if (legendContainsKey(keyInfo)) return; 
			var keyElement = document.createElement("li");
			$(keyElement).text(keyInfo.nodeType);
			addedKeys.push(keyInfo.nodeType);
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

	var legendContainsKey = function(key) {
		for (x in addedKeys) {
			if (addedKeys[x] == key.nodeType) return true;
		}
		return false;
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