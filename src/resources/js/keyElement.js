function KeyElement (keyList) {
	
	this.initialize = function (graph) {
		$(graph).on("postupdate", function () {
			// Clear all keys
			keyList.empty();
			
			// Get unique keys
			var uniqueKeys = {};
			graph.force.nodes().forEach(function (node) {
				if(!node.isVisible())
					return;
				
				var info = node.getKeyInfo();
				uniqueKeys[info.nodeType] = info;
			});
			
			for(nodeType in uniqueKeys) {
				var keyInfo = uniqueKeys[nodeType];
				
				var keyElement = document.createElement("li");
				$(keyElement).text(keyInfo.nodeType);
				keyElement.style.color = keyInfo.color;
				keyList.append(keyElement);
			}
		});
	};
}

KeyElement.instance;
KeyElement.getInstance = function () {
	return KeyElement.instance;
}

$(window).load(function () {
	KeyElement.instance = new KeyElement($(".stageMenu .key ul"));
});