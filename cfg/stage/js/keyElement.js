
function KeyElement (visibleNodeKeys, hiddenNodeKeys, visibleLinkKeys, hiddenLinkKeys) {
	var addedKeys = [];
	
	this.initialize = function (graph) {
		$(graph).on("postupdate", function () {
			addedKeys = [];
			visibleNodeKeys.empty();
			hiddenNodeKeys.empty();
			
			var activemodes = [false, false, false];
			graph.getModels().forEach(function (model) {
				activemodes[model.displaymode.id] = true;
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
			addLinkKeysToParent(graph, visibleLinkKeys, "hideLinks");
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
	var addLinkKeysToParent = function (graph, parentElement, func) {
		// Clear all keys
		parentElement.empty();

		//Add all links
		for(linkType in LinkLevelsArray) {
			var keyInfo = LinkLevelsArray[linkType],
				keyElement = document.createElement('ln'), 
				dasharray = "";
			
			if (keyInfo == LinkLevels.MEDIATOR ) {
				
				dasharray = 'stroke-dasharray: 3, 6;';
			}
			
			keyElement.innerHTML = '<svg height="16" width="200">' +
			  		'<line x1="0" y1="8" x2="50" y2="8" style="stroke:' + keyInfo.color + ';stroke-width:' + keyInfo.linewidth+ '; ' + dasharray+'" />' +
			  		'<text x="54" y="14" fill="' + keyInfo.color + '">'+ keyInfo.text + '</text>' +
				 '</svg>';
			
			
			
			parentElement.append(keyElement);			

			//$(keyElement).text(keyInfo.text);
			//keyElement.style.color = keyInfo.color;
			if(keyInfo.canShowHide) {
				$(keyElement).click(function (e) {
					graph[func]($(e.target).text());
				});

				keyElement.className += " canClick";
			}
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