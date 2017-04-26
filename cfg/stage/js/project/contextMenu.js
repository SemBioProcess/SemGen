/**
 * 
 */

function ContextMenu(parent) {
	
	var owner = parent;
	//pointer to the HTML object
	var menu = null,
	    activecaller = null;
	$('#contextmenu').hide();

	function addItem(item, caller) {
		var menuitem = document.createElement("li"); 
		var button = document.createElement("button");  
		var t = document.createTextNode(item.text);       // Button Text
		button.appendChild(t); 
		button.type = "button";
		button.onclick = function() {
			$('#stage').triggerHandler(item.text, [caller]);		
		};
		menuitem.appendChild(button);
		
		return menuitem;

	}
	
	this.showMenu = function(caller) {
		if (activecaller==caller) {
			return;
		}
		activecaller = caller;
		menu = document.querySelector('#contextmenu');
		$('.context-menu_items').empty();
		
		var items = caller.getContextMenu();
		
		for (x in items) {
			menu.querySelector('.context-menu_items').appendChild(addItem(items[x], caller));
		}
		
		menu.querySelector('.context-menu').style.left = caller.x  + "px";
		menu.querySelector('.context-menu').style.top = caller.y + "px";
		$('#contextmenu').show();
	}
	
	this.hideMenu = function() {
		$('#contextmenu').hide();
		activecaller = null;
	}

}