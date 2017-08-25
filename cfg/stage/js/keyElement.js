
function KeyElement (visibleModDepKeys, hiddenModDepKeys, visiblePhysioKeys, hiddenPhysioKeys) {
	var addedKeys = [];
	
	this.initialize = function (graph) {
		$(graph).on("postupdate", function () {
			addedKeys = [];
			$("#stagemenu #stagekey #modelkey").empty();
			visibleModDepKeys.empty();
			hiddenModDepKeys.empty();
			visiblePhysioKeys.empty();
			hiddenPhysioKeys.empty();
			
			var activemodes = [false, false, false];
			graph.getModels().forEach(function (model) {
				activemodes[model.displaymode.id] = true;
			});
			
			addKeyToParent(graph, $("#stagemenu #stagekey #modelkey"), NodeType.MODEL, null);
			addKeyToParent(graph, $("#stagemenu #stagekey #modelkey"), NodeType.EXTRACTION, null);
			
			for (x in DisplayModes) {
				if (DisplayModes[x] == DisplayModes.SHOWPHYSIOMAP) {
					populateNodeKeys(graph, x, visiblePhysioKeys, hiddenPhysioKeys);
				}
				else {
					populateNodeKeys(graph, x, visibleModDepKeys, hiddenModDepKeys);
				}
			}
			
			for(key in LinkDisplayModes.SUBDEP.keys) {
				addLinkKeyToParent(graph, visibleModDepKeys, LinkDisplayModes.SUBDEP.keys[key]);
			}
			for(key in LinkDisplayModes.PHYSIOMAP.keys) {
				addLinkKeyToParent(graph, visiblePhysioKeys, LinkDisplayModes.PHYSIOMAP.keys[key]);
			}

		});
	}
	
	var populateNodeKeys = function(graph, x, visible, hidden) {
		DisplayModes[x].keys.forEach(function(type) {
			if (graph.nodesVisible[type.id]) {
				addKeyToParent(graph, visible, type, "hideNodes");
			}
			else {
				addKeyToParent(graph, hidden, type, "showNodes");
			}
		});
	}
	
	var addKeyToParent = function (graph, parentElement, keyInfo, func) {
			if (legendContainsKey(keyInfo)) return; 
		
			var keyElement = document.createElement("li"),
			slash = "";
			$(keyElement).text(keyInfo.nodeType);
			addedKeys.push(keyInfo.nodeType);
			keyElement.style.color = keyInfo.color;
			
			if (keyInfo==NodeType.UNSPECIFIED) {
				slash = '<line x1="18" y1="0" x2="2" y2="16" style="stroke:#000000; stroke-width:2"; />';
			}
			
			
			keyElement.innerHTML = '<svg height="16" width="200">' +
		  		'<circle transform="translate(10,8)" r="6" style="fill:' + keyInfo.color + '; stroke: #000000;" />' + slash +
		  		'<text x="54" y="14" stroke="' + keyInfo.nodeType + '">'+ keyInfo.nodeType + '</text>' +
			 '</svg>';
			
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
	var addLinkKeyToParent = function (graph, parentElement, keyInfo, func) {

		//Add all links
			var keyElement = document.createElement('ln'), 
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
}

KeyElement.instance;
KeyElement.getInstance = function () {
	return KeyElement.instance;
}

$(window).load(function () {
	KeyElement.instance = new KeyElement($("#stagemenu #stagekey #subdepkeys ul.visibleKeys"), $("#stagemenu #stagekey #subdepkeys ul.hiddenKeys"),
			$("#stagemenu #stagekey #physkeys ul.visibleKeys"), $("#stagemenu #stagekey #physkeys ul.hiddenKeys"));
});