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

convertArraytoString = function (array) {
	var string = "";
	array.foreach(function(d) {
		string += d + ",";
	});
	//return string with last comma removed
	return string.slice(0, -1);
}
