String.prototype.capitalizeFirstLetter = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
}

getSymbolArray = function (obj) {
	var syms = [];
	for (x in obj) {
		syms.push(obj[x]);
	}
	return syms;
}