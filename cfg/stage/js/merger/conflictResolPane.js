/**
 * 
 */

function ConflictResolutionPane(merger) {
	var pane = this;
	var task = merger;
	var javaaccessor = task.conflictsj;
		
	this.readyformerge = false;
	var conflictobj;
	
	var unitconflicts = [];
	var smconflicts = [];
	var cwconflicts = [];

	var addSubmodelConflictPanel = function(smconf) {
		var t = document.querySelector('#dupNameResolution');
		var clone = document.importNode(t.content, true);
		
		clone.id = 'smcon' + smconflicts.length;
		clone.index = smconflicts.length;
		clone.srcobj = smconf;
		
		clone.resolved = false;

		clone.querySelector('.dupname').innerHTML = smconf.duplicate;
		var input = clone.querySelector('.newName');
		
		input.value = clone.srcobj.replacement;
		input.onchange = function () {
			if (task.isNameAlreadyUsed(input.value)) {
				input.value = "";
				alert("A node with that name already exists.");
				return;
			}
			javaaccessor.setSubmodelName(clone.index, true, input.value);
			clone.resolved = input.value != "";
			checkAllResolved();
			//clone.querySelector('.glyphicon').style.visibility = "hidden";
		} 
		
		smconflicts.push(clone);
		document.querySelector('#modalContent #DupSubModels').appendChild(clone);

	}
	
	var addCodewordConflictPanel = function(cwconf) {
		var t = document.querySelector('#dupNameResolution');
		var clone = document.importNode(t.content, true);
		
		clone.id = 'cwcon' + cwconflicts.length;
		clone.index = cwconflicts.length;
		clone.srcobj = cwconf;
		clone.resolved = false;

		clone.disable = merger.mergecomplete;
		
		clone.querySelector('.dupname').innerHTML = cwconf.duplicate;

		var input = clone.querySelector('.newName');
		
		input.value = clone.srcobj.replacement;
		input.onchange = function () {
			if (task.isNameAlreadyUsed(input.value)) {
				input.value = "";
				alert("A node with that name already exists.");
				return;
			}
			clone.resolved = input.value != "";
			if (input.value != "") {
				javaaccessor.setCodewordName(clone.index, true, input.value);
			}
			
			checkAllResolved();
		//	clone.querySelector('.glyphicon').style.visibility = "hidden";
		} 
		
		
		cwconflicts.push(clone);
		document.querySelector('#modalContent #DupCodewords').appendChild(clone);

	}
	
	var addUnitConflictPanel = function(unitconf) {
		var t = document.querySelector('#unitConversionResolution');
		var clone = document.importNode(t.content, true);
		
		clone.id = 'ucon' + unitconflicts.length;
		clone.index = unitconflicts.length;
		clone.srcobj = unitconf;
		clone.resolved = true;

		clone.querySelector('.unitA').innerHTML = unitconf.unitleft;
		clone.querySelector('.unitB').innerHTML = unitconf.unitright;
		
		var operatorsel = clone.querySelector('.multiplyChoice');
		var input = clone.querySelector('.unitConvFactor');
		input.value = clone.srcobj.conversion;
		
		operatorsel.selectedIndex = clone.srcobj.multiply ? 0 : 1;
		input.onchange = function () {
			if (input.value != "") {
				javaaccessor.setUnitConversion(clone.index, operatorsel.selectedIndex==0, input.value);
			}
			clone.resolved = input.value != "";
			checkAllResolved();
		//	clone.querySelector('.glyphicon').style.visibility = "hidden";
		} 
		
		operatorsel.onchange = function () {
			javaaccessor.setUnitConversion(clone.index, operatorsel.selectedIndex==0, input.value);
		} 
	 
		unitconflicts.push(clone);
		document.querySelector('#modalContent #UnitConf').appendChild(clone);

	}
	
	var modifyAllPanels = function (action) {
		unitconflicts.forEach(function(panel) {action(panel);});
		smconflicts.forEach(function(panel) {action(panel);});
		cwconflicts.forEach(function(panel) {action(panel);});
	}
	
	this.refreshConflicts = function() {
		unitconflicts.length = 0;
		smconflicts.length = 0;
		cwconflicts.length = 0;

		$("#DupSubModels, #DupCodewords, #UnitConf").contents(":not(.mergeStep2Heading)").remove();
		
		conflictobj.unitconflicts.forEach(function(con) {
			addUnitConflictPanel(con);
		});
		conflictobj.dupesubmodels.forEach(function(con) {
			addSubmodelConflictPanel(con);
		});
		conflictobj.dupecodewords.forEach(function(con) {
			addCodewordConflictPanel(con);
		});

		if(unitconflicts.length == 0)
			$("#UnitConf").hide();
		if(smconflicts.length == 0)
			$("#DupSubModels").hide();
		if(cwconflicts.length == 0)
			$("#DupCodewords").hide();
		
		if (!this.hasConflicts()) {
			$("#nextBtn").hide();
		}
		
		checkAllResolved();
		$('input.newName').prop('disabled', merger.mergecomplete);
		$('input.multiplyChoice').prop('disabled', merger.mergecomplete);
		$('input.unitConvFactor').prop('disabled', merger.mergecomplete);
	}
	
	var checkAllResolved = function() {
		var resolved = true;
		var panelchecker = function(panel) {
			if (!resolved) return;
			resolved = panel.resolved;
		}
		
		while (resolved) {
			modifyAllPanels(function(panel) { panelchecker(panel); });
			break;
		}
		
		pane.readyformerge = resolved;
		task.readyforMerge();
	}
	
	this.afterMerge = function() {
		modifyAllPanels(function(panel) { panel.disable(); });
	}	
	
	this.hasConflicts = function() {
		return unitconflicts.length >0 || smconflicts.length >0 || cwconflicts.length >0;
	}
	
	receiver.onShowConflicts(function(data) {
		conflictobj = data;
		pane.refreshConflicts();
	});
	

	
}