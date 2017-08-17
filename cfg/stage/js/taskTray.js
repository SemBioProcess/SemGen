/**
 * 
 */

	//Structure for open task icons
function OpenTask(type, index, modelnodes) {
		var taskicon = this;
		var tasktype = type;
		var taskindex = index;
		this.currenttask = false;

		this.showTaskIcon = function() {
			var tasktab = document.createElement("span");
			tasktab.className = "taskchooser";
			
			
			var icon = document.createElement("input");
			icon.type = "image";
			icon.id = 'task' + taskindex;
			icon.src = type.thumb;
			

			if (this.currenttask) {
				tasktab.style.borderColor = 'GoldenRod';
			}
			
			tasktab.onclick = function() {
				if (!taskicon.currenttask) {
					sender.changeTask(taskindex);
				}
			};
			
			tasktab.appendChild(icon);
			if (type.type== StageTasks.MERGER.type) {
				var modelnames = [];
				for (i in modelnodes) {
					modelnames.push(modelnodes[i].name);
				}

				var namelabel = document.createElement("ul");
				$(namelabel).append("<li>" + modelnames[0] + "</li> ");
				$(namelabel).append("<li>" + modelnames[1] + "</li> ");
				tasktab.appendChild(namelabel);
			}
			
			$("#activeTaskPanel").append(tasktab);
		}
		
		this.indexMatches = function(indextocheck) {
			return indextocheck == index;
		}
	}

function TaskTray() {
	var opentasks = [];
	var activetaskindex = 0;
	
	// Slide up panel for Active Task Tray
	$("#activeTaskTray").click(function() {
		$("#activeTaskPanel").slideToggle();
	});

	this.refresh = function() {
		$("#activeTaskPanel").contents().remove();
		opentasks.forEach(function(task) {
			if (task != null) {
				task.showTaskIcon();
			}
		});
	}
	
	this.addTask = function(type, index, models) {		
		opentasks.push(new OpenTask(type, index, models));
		this.setActiveTask(index);
		this.refresh();
		
	}
	
	this.getIconwithIndex = function(index) {
		for (x in opentasks) {
			if (opentasks[x].indexMatches(index)) {
				return opentasks[x];
			}
		}
	}
	
	this.removeTask = function(index) {
		opentasks[index] = null;
		this.refresh();
	}
	
	this.hasIndex = function(index) {
		for (x in opentasks) {
			if (opentasks[x] != null) {
				if (opentasks[x].indexMatches(index)) {
					return true;
				}
			}

		}
		return false;
	}
	
	this.setActiveTask = function(index) {
		activetaskindex = index;
		for (x in opentasks) {
			opentasks[x].currenttask = false;
		}
		opentasks[index].currenttask = true;
	}
	
}
