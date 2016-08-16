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
		
		clone.id = 'smcon' + smconflicts.length;02
		clone.index = smconflicts.length;
		clone.srcobj = smconf;
		clone.resolved = false;
		
		clone.querySelector('.dupname').innerHTML = "Duplicate submodel name: " + smconf.duplicate;
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

		clone.querySelector('.dupname').innerHTML = "Duplicate codeword name: " + cwconf.duplicate;
		//var choice = clone.querySelector('.newSubmodelName');
		var input = clone.querySelector('.newName');
		
		input.value = clone.srcobj.replacement;
		input.onchange = function () {
			if (task.isNameAlreadyUsed(input.value)) {
				input.value = "";
				alert("A node with that name already exists.");
				return;
			}
			javaaccessor.setCodewordName(clone.index, true, input.value);
			clone.resolved = input.value != "";
			checkAllResolved();
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
			javaaccessor.setUnitConversion(clone.index, operatorsel.selectedIndex==0, input.value);
			clone.resolved = input.value != "";
			checkAllResolved();
		} 
		 operatorsel.onchange = function () {
				javaaccessor.setUnitConversion(clone.index, operatorsel.selectedIndex==0, input.value);
			} 
		unitconflicts.push(clone);
		document.querySelector('#modalContent #UnitConf').appendChild(clone);

	}
	
	this.refreshConflicts = function() {
		unitconflicts.length = 0;
		smconflicts.length = 0;
		cwconflicts.length = 0;

		
		var olaps = document.querySelector('#modalContent #UnitConf');
		while (olaps.firstChild) {
			olaps.removeChild(olaps.firstChild);
		}
		
		olaps = document.querySelector('#modalContent #DupSubModels');
		while (olaps.firstChild) {
			olaps.removeChild(olaps.firstChild);
		}
		
		olaps = document.querySelector('#modalContent #DupCodewords');
		while (olaps.firstChild) {
			olaps.removeChild(olaps.firstChild);
		}
		
		conflictobj.unitconflicts.forEach(function(con) {
			addUnitConflictPanel(con);
		});
		conflictobj.dupesubmodels.forEach(function(con) {
			addSubmodelConflictPanel(con);
		});
		conflictobj.dupecodewords.forEach(function(con) {
			addCodewordConflictPanel(con);
		});
		
		checkAllResolved();
	}
	
	var checkAllResolved = function() {
		var resolved = true;
		var panelchecker = function(panel) {
			resolved = panel.resolved;
		}
		
		while (resolved) {
			unitconflicts.forEach(function(panel) { panelchecker(panel); });
			smconflicts.forEach(function(panel) { panelchecker(panel); });
			cwconflicts.forEach(function(panel) { panelchecker(panel); });
			break;
		}
		
		pane.readyformerge = resolved;
		task.readyforMerge();
	}
	
	receiver.onShowConflicts(function(data) {
		conflictobj = data;
		pane.refreshConflicts();
	});
	
}