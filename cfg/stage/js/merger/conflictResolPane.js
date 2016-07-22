/**
 * 
 */

function ConflictResolutionPane(accessor) {
	var pane = this;
	var conflictobj;
	var javaaccessor = accessor;
	var unitconflicts = [];
	var smconflicts = [];
	var cwconflicts = [];

	var addSubmodelConflictPanel = function(smconf) {
		var t = document.querySelector('#dupNameResolution');
		var clone = document.importNode(t.content, true);
		
		clone.id = 'smcon' + smconflicts.length;
		clone.index = smconflicts.length;
		clone.srcobj = smconf;
		
		clone.querySelector('.dupname').innerHTML = "Duplicate submodel name: " + smconf.duplicate;
		//var choice = clone.querySelector('.newSubmodelName');
		var input = clone.querySelector('.newName');
		
		input.value = clone.srcobj.replacement;
		input.onchange = function () {
			javaaccessor.setSubmodelName(clone.index, true, input.value);
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
		
		clone.querySelector('.dupname').innerHTML = "Duplicate codeword name: " + cwconf.duplicate;
		//var choice = clone.querySelector('.newSubmodelName');
		var input = clone.querySelector('.newName');
		
		input.value = clone.srcobj.replacement;
		input.onchange = function () {
			javaaccessor.setCodewordName(clone.index, true, input.value);
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
		
		clone.querySelector('.unitA').innerHTML = unitconf.unitleft;
		clone.querySelector('.unitB').innerHTML = unitconf.unitright;
		
		var operatorsel = clone.querySelector('.multiplyChoice');
		var input = clone.querySelector('.unitConvFactor');
		input.value = clone.srcobj.conversion;
		
		operatorsel.selectedIndex = clone.srcobj.multiply ? 0 : 1;
		 input.onchange = function () {
			javaaccessor.setUnitConversion(clone.index, operatorsel.selectedIndex==0, input.value);
		} 
		 operatorsel.onchange = function () {
				javaaccessor.setUnitConversion(clone.index, operatorsel.selectedIndex==0, input.value);
			} 
		unitconflicts.push(clone);
		document.querySelector('#modalContent #UnitConf').appendChild(clone);

	}
	
	receiver.onShowConflicts(function(data) {
		conflictobj = data;
		pane.refreshConflicts();
	});
	
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
	}
}