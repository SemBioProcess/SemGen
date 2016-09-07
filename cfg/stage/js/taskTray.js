/**
 * 
 */

	//Structure for open task icons
function OpenTask(type, index) {
		var taskicon = this;
		var type;
		var taskindex = index;
		this.currenttask = false;
		
		this.showTaskIcon = function() {
			var icon = document.createElement("input");
			icon.type = "image";
			icon.id = 'task' + taskindex;
			icon.src = type.thumb;
			
			icon.onclick = function() {
				if (!taskicon.currenttask) {
					currenttask = true;
					sender.changeTask(taskindex);
					
				}
			};
			$("#activeTaskPanel").append(icon);
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
	
	this.addTask = function(type, index) {
		opentasks.push(new OpenTask(type, index));
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
	
}
