/**
 * 
 */

function ContextMenu(parent) {
	
	var owner = parent;
	//pointer to the HTML object
	var menu = null,
	    activecaller = null;
	$('#contextmenu').hide();
	
	function addItem(item) {
		return '<li class="context-menu_item">' + 
				'<a href="#" class="context-menu_link">' + 
					item.text +
				'</a>' +
			'</li>';
	}
	
	this.showMenu = function(caller) {
		if (activecaller==caller) {
			return;
		}
		activecaller = caller;
		menu = document.querySelector('#contextmenu');
		var items = caller.getContextMenu(),
			itemlist = "";
		

		for (x in items) {
			itemlist += addItem(items[x]);
		}
		
		menu.querySelector('.context-menu_items').innerHTML = itemlist;

		menu.querySelector('.context-menu').style.left = caller.x  + "px";
		menu.querySelector('.context-menu').style.top = caller.y + "px";
		$('#contextmenu').show();
	}
	
	this.hideMenu = function() {
		$('#contextmenu').hide();
		activecaller = null;
	}

}