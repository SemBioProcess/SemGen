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
						id: modelName + "A",
						parentModelId: modelName,
						name: "A",
						inputs: [ {sourceId: modelName + "B", parentModelId: modelName} ],
						nodeType: "state",
					},
					{
						id: modelName + "B",
						parentModelId: modelName,
						name: "B",
						nodeType: "Rate",
					},
					{
						id: modelName + "C",
						parentModelId: modelName,
						name: "C",
						inputs: [ {sourceId: modelName + "A", parentModelId: modelName} ],
						nodeType: "constitutive",
					},
					{
						id: modelName + "D",
						parentModelId: modelName,
						name: "D",
						inputs: [
							{sourceId: modelName + "A", parentModelId: modelName},
							{sourceId: modelName + "B", parentModelId: modelName},
							{sourceId: modelName + "C", parentModelId: modelName} ],
						nodeType: "State",
					},
				];
				mockReceiver.showDependencyNetwork(modelName, data);
			}
			else if (task == "submodels") {
				var data = [
					{
						id: modelName + "Submodel_1",
						parentModelId: modelName,
						name: "Submodel_1",
						dependencies: [
							{
								id: modelName + "Submodel_1" + "A",
								parentModelId: modelName + "Submodel_1",
								name: "A",
								inputs: [
									{
										sourceId: modelName + "Submodel_1" + "B",
										parentModelId: modelName + "Submodel_1"
									},
									{
										sourceId: modelName + "Submodel_2" + "A",
										parentModelId: modelName + "Submodel_2"
									},
								],
								nodeType: "state",
							},
							{
								id: modelName + "Submodel_1" + "B",
								parentModelId: modelName + "Submodel_1",
								name: "B",
								nodeType: "Rate",
							},
							{
								id: modelName + "Submodel_1" + "C",
								parentModelId: modelName + "Submodel_1",
								name: "C",
								inputs: [
									{
										sourceId: modelName + "Submodel_1" + "A",
										parentModelId: modelName + "Submodel_1"
									},
								],
								nodeType: "constitutive",
							},
							{
								id: modelName + "Submodel_1" + "D",
								parentModelId: modelName + "Submodel_1",
								name: "D",
								inputs: [
									{
										sourceId: modelName + "Submodel_1" + "A",
										parentModelId: modelName + "Submodel_1"
									},
									{
										sourceId: modelName + "Submodel_1" + "B",
										parentModelId: modelName + "Submodel_1"
									},
									{
										sourceId: modelName + "Submodel_1" + "C",
										parentModelId: modelName + "Submodel_1"
									},
								],
								nodeType: "State",
							},
						],
					},
					{
						id: modelName + "Submodel_2",
						parentModelId: modelName,
						name: "Submodel_2",
						dependencies: [
							{
								id: modelName + "Submodel_2" + "A",
								parentModelId: modelName + "Submodel_2",
								name: "A",
								inputs: [
									{
										sourceId: modelName + "Submodel_2" + "B",
										parentModelId: modelName + "Submodel_2"
									},
									{
										sourceId: modelName + "Submodel_3" + "A",
										parentModelId: modelName + "Submodel_3"
									},
								],
								nodeType: "state",
							},
							{
								id: modelName + "Submodel_2" + "B",
								parentModelId: modelName + "Submodel_2",
								name: "B",
								nodeType: "Rate",
							},
							{
								id: modelName + "Submodel_2" + "C",
								parentModelId: modelName + "Submodel_2",
								name: "C",
								inputs: [
									{
										sourceId: modelName + "Submodel_2" + "A",
										parentModelId: modelName + "Submodel_2"
									},
								],
								nodeType: "constitutive",
							},
							{
								id: modelName + "Submodel_2" + "D",
								parentModelId: modelName + "Submodel_2",
								name: "D",
								inputs: [
									{
										sourceId: modelName + "Submodel_2" + "A",
										parentModelId: modelName + "Submodel_2"
									},
									{
										sourceId: modelName + "Submodel_2" + "B",
										parentModelId: modelName + "Submodel_2"
									},
									{
										sourceId: modelName + "Submodel_2" + "C",
										parentModelId: modelName + "Submodel_2"
									},
								],
								nodeType: "State",
							},
						],
					},
					{
						id: modelName + "Submodel_3",
						parentModelId: modelName,
						name: "Submodel_3",
						dependencies: [
							{
								id: modelName + "Submodel_3" + "A",
								parentModelId: modelName + "Submodel_3",
								name: "A",
								inputs: [
									{
										sourceId: modelName + "Submodel_3" + "B",
										parentModelId: modelName + "Submodel_3"
									},
									{
										sourceId: modelName + "Submodel_1" + "A",
										parentModelId: modelName + "Submodel_1"
									},
								],
								nodeType: "state",
							},
							{
								id: modelName + "Submodel_3" + "B",
								parentModelId: modelName + "Submodel_3",
								name: "B",
								nodeType: "Rate",
							},
							{
								id: modelName + "Submodel_3" + "C",
								parentModelId: modelName + "Submodel_3",
								name: "C",
								inputs: [
									{
										sourceId: modelName + "Submodel_3" + "A",
										parentModelId: modelName + "Submodel_3"
									},
								],
								nodeType: "constitutive",
							},
							{
								id: modelName + "Submodel_3" + "D",
								parentModelId: modelName + "Submodel_3",
								name: "D",
								inputs: [
									{
										sourceId: modelName + "Submodel_3" + "A",
										parentModelId: modelName + "Submodel_3"
									},
									{
										sourceId: modelName + "Submodel_3" + "B",
										parentModelId: modelName + "Submodel_3"
									},
									{
										sourceId: modelName + "Submodel_3" + "C",
										parentModelId: modelName + "Submodel_3"
									},
								],
								nodeType: "State",
							},
						],
					},
				];

				mockReceiver.showSubmodelNetwork(modelName, data);
			}
			else if(task == "physiomap") {
				var data = [
					{
						id: modelName + "Entity 1",
						parentModelId: modelName,
						name: "Entity 1",
						nodeType: "Entity",
						inputs: [],
					},
					{
						id: modelName + "Process 1",
						parentModelId: modelName,
						name: "Process 1",
						nodeType: "Process",
						inputs: [
							{
								sourceId: modelName + "Entity 2",
								sinkId: modelName + "Process 1",
								parentModelId: modelName,
								linkType: "",
							},
							{
								sourceId: modelName + "Entity 3",
								sinkId: modelName + "Process 1",
								parentModelId: modelName,
								linkType: "",
							},
							{
								sourceId: modelName + "Mediator C",
								sinkId: modelName + "Process 1",
								parentModelId: modelName,
								linkType: "Mediator",
							},
							{
								sourceId: modelName + "Process 1",
								sinkId: modelName + "Entity 2",
								parentModelId: modelName,
								label: "",
							},
							{
								sourceId: modelName + "Process 1",
								sinkId: modelName + "Entity 3",
								parentModelId: modelName,
								linkType: "",
							},
						],
					},
					{
						id: modelName + "Entity 2",
						parentModelId: modelName,
						name: "Entity 2",
						nodeType: "Entity",
						inputs: [],
					},
					{
						id: modelName + "Process 2",
						parentModelId: modelName,
						name: "Process 2",
						nodeType: "Process",
						inputs: [
							{
								sourceId: modelName + "Entity 3",
								sinkId: modelName + "Process 2",
								parentModelId: modelName,
								linkType: "",
							},
							{
								sourceId: modelName + "Process 2",
								sinkId: modelName + "Entity 1",
								parentModelId: modelName,
								linkType: "",
							},
							{sourceId: modelName + "Process 2",
								sinkId: modelName + "Mediator C",
								parentModelId: modelName,
								linkType: "",
							},
						],
					},
					{
						id: modelName + "Entity 3",
						parentModelId: modelName,
						name: "Entity 3",
						nodeType: "Entity",
						inputs: [],
					},
					{
						id: modelName + "Process 3",
						parentModelId: modelName,
						name: "Process 3",
						nodeType: "Process",
						inputs: [
							{
								sourceId: modelName + "Entity 1",
								sinkId: modelName + "Process 3",
								parentModelId: modelName,
								linkType: "",
							},
							{
								sourceId: modelName + "Mediator B",
								sinkId: modelName + "Process 3",
								parentModelId: modelName,
								linkType: "Mediator",
							},
							{
								sourceId: modelName + "Process 3",
								sinkId: modelName + "Entity 3",
								parentModelId: modelName,
								linkType: "",
							},
						],
					},
					{
						id: modelName + "Mediator B",
						parentModelId: modelName,
						name: "Mediator B",
						nodeType: "Mediator",
						inputs: []
					},
					{
						id: modelName + "Mediator C",
						parentModelId: modelName,
						name: "Mediator C",
						nodeType: "Mediator",
						inputs: [],
					}
				];
				mockReceiver.showPhysioMapNetwork(modelName, data);
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

		addModelByName: function(source, modelName) {
			alert("Source: " + source + ", Model name: " + modelName);
			mockReceiver.addModel(modelName);
		},

		search: function (searchStr) {
			searchResults = [
				{
					source: "Source 1",
					results: [
						searchStr + "Search Result 1",
						searchStr + "Search Result 2",
						searchStr + "Search Result 3",
					]
				},
				{
					source: "Source 2",
					results: [
						searchStr + "Search Result 1",
						searchStr + "Search Result 2",
						searchStr + "Search Result 3",
					]
				},
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

		onShowPhysioMapNetwork: function (handler) { this.showPhysioMapNetwork = handler; },

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
