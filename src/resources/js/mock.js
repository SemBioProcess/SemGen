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
			
			taskClicked: function (modelName, task) {
				if(task == "dependencies") {
					var data = [
					    {
					    	name: "A",
					    	inputs: ["B"],
					    	nodeType: "state",
					    },
					    {
					    	name: "B",
					    	nodeType: "Rate",
					    },
					    {
					    	name: "C",
					    	inputs: ["A"],
					    	nodeType: "constitutive",
					    },
					    {
					    	name: "D",
					    	inputs: ["A", "B", "C"],
					    	nodeType: "State",
					    },
					];
					mockReceiver.showDependencyNetwork(modelName, data);
				}
				else if (task == "submodels") {
					var data = [
						{
							name: "Submodel_1",
							inputs: ["Submodel_2"],
						},
						{
							name: "Submodel_2",
							inputs: ["Submodel_3"],
						},
						{
							name: "Submodel_3",
							inputs: ["Submodel_1"],
						},
					];
					
					mockReceiver.showSubmodelNetwork(modelName, data);
				}
			},
			
			submodelClicked: function(parentName, submodelName) {
				var submodelInput = submodelName == "Submodel_1" ? "Submodel_2" :
						submodelName == "Submodel_2" ? "Submodel_3" :
						"Submodel_1";

				var data = [
				    {
				    	name: "A",
				    	inputs: ["B", { name: "A", parents: [parentName, submodelInput] }],
				    	nodeType: "state",
				    },
				    {
				    	name: "B",
				    	nodeType: "Rate",
				    },
				    {
				    	name: "C",
				    	inputs: ["A"],
				    	nodeType: "constitutive",
				    },
				    {
				    	name: "D",
				    	inputs: ["A", "B", "C"],
				    	nodeType: "State",
				    },
				];
				
				mockReceiver.showSubmodelDependencyNetwork(parentName, submodelName, data);
			}
	};
	
	var mockReceiver = {
			onAddModel: function (handler) { this.addModel = handler; },
			
			onShowDependencyNetwork: function (handler) { this.showDependencyNetwork = handler; },
			
			onShowSubmodelNetwork: function (handler) { this.showSubmodelNetwork = handler; },
			
			onShowSubmodelDependencyNetwork: function (handler) { this.showSubmodelDependencyNetwork = handler; },
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