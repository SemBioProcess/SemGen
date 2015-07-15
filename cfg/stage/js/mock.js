/**
 * If we're loading outside of SemGen create a mock sender and receiver
 */
$(window).load(function() {
	// sendNSCommand is defined when the stage is loaded in SemGen
	if(window.location.search.indexOf("testMode=false") != -1)
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
							dependencies: [
							    {
							    	name: "A",
							    	inputs: ["B", { name: "A", parents: [modelName, "Submodel_2"] }],
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
							],
						},
						{
							name: "Submodel_2",
							dependencies: [
							    {
							    	name: "A",
							    	inputs: ["B", { name: "A", parents: [modelName, "Submodel_3"] }],
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
							],
						},
						{
							name: "Submodel_3",
							dependencies: [
							    {
							    	name: "A",
							    	inputs: ["B", { name: "A", parents: [modelName, "Submodel_1"] }],
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
							],
						},
					];
					
					mockReceiver.showSubmodelNetwork(modelName, data);
				}
				else if(task == "close") {
					mockReceiver.removeModel(modelName);
				}
				else if(task == "annotate" ||
						task == "extract" ||
						task == "merge") {
					alert("Task: '" + task + "' executed");
				}
			},
			
			addModelByName: function(modelName) {
				mockReceiver.addModel(modelName);
			},
			
			search: function (searchStr) {
				searchResults = [
				     searchStr + "Search Result 1",
				     searchStr + "Search Result 2",
				     searchStr + "Search Result 3",
				];
				
				
				mockReceiver.search(searchResults);
			},
			
			merge: function (modelName1, modelName2) {
				alert("Merge: '" + modelName1 + "' and '" + modelName2 + "'");
			},
			
			log: function () {}
	};
	
	var mockReceiver = {
			onAddModel: function (handler) { this.addModel = handler; },
			
			onShowDependencyNetwork: function (handler) { this.showDependencyNetwork = handler; },
			
			onShowSubmodelNetwork: function (handler) { this.showSubmodelNetwork = handler; },
			
			onSearch: function (handler) { this.search = handler; },
			
			onRemoveModel: function (handler) { this.removeModel = handler; },
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