/**
 * Created by graham_kim on 3/23/16.
 * Adapted from Ryan's multGraphTest.html

 */

function createGraph(selector, data) {
    var width = document.querySelector(selector).offsetWidth,
        height = document.querySelector(selector).offsetHeight;

    var color = d3.scale.category10();

    var force = d3.layout.force()
        .charge(-120)
        .linkDistance(30)
        .size([width, height]);

    var svg = d3.select(selector).append("svg")
        .attr("width", width)
        .attr("height", height);

    force
        .nodes(data.nodes)
        .links(data.links)
        .start();

    var link = svg.selectAll(".link")
        .data(data.links)
        .enter().append("line")
        .attr("class", "link")
        .style("stroke", "black")
        .style("stroke-width", 1)

    var node = svg.selectAll(".node")
        .data(data.nodes)
        .enter().append("circle")
        .attr("class", "node")
        .attr("r", 5)
        .style("fill", function(d) { return color(d.group); })
        .call(force.drag);

    node.append("text")
        .attr("font-size", 12)
        .attr("x", 0)
        .attr("y", -5)
        .attr("text-anchor", "middle")
        .text(function(d) { return d.name });

    force.on("tick", function() {
        link.attr("x1", function(d) { return d.source.x; })
            .attr("y1", function(d) { return d.source.y; })
            .attr("x2", function(d) { return d.target.x; })
            .attr("y2", function(d) { return d.target.y; });

        node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
    });
}