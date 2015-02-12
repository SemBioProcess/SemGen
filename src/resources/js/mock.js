/**
 * If we're loading outside of SemGen create a mock sender and receiver
 */
$(window).load(function() {
	// sendNSCommand is defined when the stage is loaded in SemGen
	if(typeof sendNSCommand != 'undefined')
		return;
	
	var modelNum = 0;
	var mockSender = {
			addModel: function() {
				mockReceiver.addModel("Test model " + modelNum++);
			},
			
			taskClicked: function (task, modelName) {
				if(task.toLowerCase() == "dependencies") {
					var data = [
					    {
					    	id: modelName + "A",
					    	links: [modelName + "B"],
					    	group: 0,
					    },
					    {
					    	id: modelName + "B",
					    	group: 1,
					    },
					    {
					    	id: modelName + "C",
					    	links: [modelName + "A"],
					    	group: 2,
					    },
					    {
					    	id: modelName + "D",
					    	links: [modelName + "A", modelName + "B", modelName + "C"],
					    	group: 0,
					    },
					];
					mockReceiver.showDependencies(modelName, data);
				}
			},
	};
	
	var mockReceiver = {
			onAddModel: function (handler) { this.addModel = handler; },
			
			onShowDependencies: function (handler) { this.showDependencies = handler; },
	};
	
	var event; // The custom event that will be created

	if (document.createEvent) {
		event = document.createEvent("HTMLEvents");
		event.initEvent("cwb-initialized", true, true);
	}
	else {
		event = document.createEventObject();
		event.eventType = "cwb-initialized";
	}

	event.eventName = "cwb-initialized";
	event.commandReceiver = mockReceiver;
	event.commandSender = mockSender;

	if (document.createEvent) {
		window.dispatchEvent(event);
	}
	else {
		window.fireEvent("on" + event.eventType, event);
	}
});