function updateSvgSize() {
    svg.attr("width", $(window).width()).attr("height", $(window).height());
}
window.onresize = updateSvgSize;