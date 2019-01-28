var selectedNode = "Thing";
var width = 650, height = 500;
var xCenter = 0;
var yCenter = 0;
var scale = 1;
var centralizeFlag = true;
var translate;
var centralized = false;

function searchByClick(name){
	console.log("Search by click");
	document.getElementById("A1428:mainForm:searchText").value = name;
	document.getElementById("A1428:mainForm:searchButton").click();
	document.getElementById("A1428:mainForm:searchText").value = "";
}


function searchClass() {
	var newSelectedNode = document.getElementById("searchText").value;
	search(newSelectedNode);
}

function search(newSelectedNode){
	$("#" + selectedNode + " > circle")[0].style.fill = '#ccc';
	selectedNode = newSelectedNode;
	$("#" + selectedNode + " > circle")[0].style.fill = 'red';
	centralizeFlag = true;
	centralize();
	centralizeFlag = false;
}

function centralize() {
	var translate = $($("#" + selectedNode)[0]).attr('transform');
	var res = translate.replace("translate(", "").replace(")", "").split(",");
	xCenter = width / 2 - res[0];
	yCenter = height / 2 - res[1];
	var newTranslate = "translate(" + xCenter + "," + yCenter + ")"
			+ " scale("+1+")";
	$("#g-container").attr('transform', newTranslate);
	centralized = true;
}

function drawOntology(svgContainer) {
	var container = d3.select("." + svgContainer);
	container.select("svg").remove();

	// get the json from hidden object and create from it the category tree
	var d3Model = document.getElementById("A1428:hidden-form:json").value;
	// get the data
	var links = d3.csv.parse(d3Model);

	var nodes = {};

	// Compute the distinct nodes from the links.
	links.forEach(function(link) {

		link.source = nodes[link.source] || (nodes[link.source] = {
			name : link.source
		});
		link.target = nodes[link.target] || (nodes[link.target] = {
			name : link.target
		});
		link.value = +link.value;
	});

	var defaultTextSize = 12, bigTextSize = 22, normalRadius = 6, bigRadius = 16;

	var force = d3.layout.force().nodes(d3.values(nodes)).links(links).size(
			[ width, height ]).linkDistance(60).charge(-1000).on("tick", tick)
			.start();

	force.on('end', function() {
		centralizeFlag = false;
	});

	// Set the range
	var v = d3.scale.linear().range([ 0, 100 ]);

	// Scale the range of the data
	v.domain([ 0, d3.max(links, function(d) {
		return d.value;
	}) ]);

	// asign a type per value to encode opacity
	links.forEach(function(link) {
		link.type = "twofive";
	});

	var zoom = d3.behavior.zoom().scaleExtent([ 1, 8 ]).on("zoom", zoomed);

	var svg = container.append("svg").attr("width", width).attr(
			"height", height).call(zoom).on("dblclick.zoom", null).append("g")
			.attr("id", "g-container");

	// build the arrow.
	svg.append("svg:defs").selectAll("marker").data([ "end" ]) // Different
																// link/path
																// types can be
																// defined here
	.enter().append("svg:marker") // This section adds in the arrows
	.attr("id", String).attr("viewBox", "0 -5 10 10").attr("refX", 15).attr(
			"refY", -1.5).attr("markerWidth", 6).attr("markerHeight", 6).attr(
			"orient", "auto").append("svg:path").attr("d", "M0,-5L10,0L0,5");

	// add the links and the arrows
	var path = svg.append("svg:g").selectAll("path").data(force.links())
			.enter().append("svg:path").attr("class", function(d) {
				return "link " + d.type;
			}).attr("marker-end", "url(#end)");

	// define the nodes
	var node = svg.selectAll(".node").data(force.nodes()).enter().append("g")
			.attr("class", "node").on("click", click).on("dblclick", dblclick)
			.call(force.drag);

	// add the nodes
	node.append("circle").attr("r", normalRadius);

	// add the text
	node.append("text").attr("x", defaultTextSize).attr("dy", ".35em").text(
			function(d) {
				return d.name;
			});

	// add the name of the node as attribute of the node
	node.attr("id", function(d) {
		return d.name;
	});

	var text = svg.append("g").selectAll("text").data(force.nodes()).enter()
			.append("text").attr("x", 8).attr("y", ".31em").text(function(d) {
				return d.name;
			});

	// add the curvy lines
	function tick() {
		path.attr("d",
				function(d) {
					var dx = d.target.x - d.source.x, dy = d.target.y
							- d.source.y, dr = Math.sqrt(dx * dx + dy * dy);
					return "M" + d.source.x + "," + d.source.y + "A" + dr + ","
							+ dr + " 0 0,1 " + d.target.x + "," + d.target.y;
				});
		node.attr("transform", function(d) {
			return "translate(" + d.x + "," + d.y + ")";
		});
		if (centralizeFlag) {
			centralize();
		}
		$("#" + selectedNode + " > circle")[0].style.fill = 'red';
	}

	function zoomed() {
		//if (scale != d3.event.scale) {
//			console.log("zoomed");
//			scale = d3.event.scale;
//			svg.attr("transform", "translate(" + d3.event.translate + ")scale(" + scale + ")");
			if (centralized){
				d3.event.scale = 1;
				centralized = false;
			}
			
			 scale = d3.event.scale;
			 svg.attr("transform", "translate(" + (xCenter +
			 d3.event.translate[0]) + ", "+(yCenter +
			 d3.event.translate[1])+")" + " scale("+scale+")");
			 
			 //NO ZOOM
			//svg.attr("transform", "translate(" + d3.event.translate[0] + ", "
			//		+ d3.event.translate[1] + ")" + " scale(1)");
		//}
	}

	// action to take on mouse click
	function click() {
		var name = d3.select(this).select("circle").data()[0].name;
		searchByClick(name);
	}

	// action to take on mouse double click
	function dblclick() {
		if (d3.select(this).select("circle").attr("r") == bigRadius) {
			d3.select(this).select("circle").transition().duration(250).attr(
					"r", normalRadius).style("fill", "#ccc");
			d3.select(this).select("text").transition().duration(0).attr("x",
					defaultTextSize).style("stroke", "none").style("fill",
					"black").style("stroke", "none").style("font",
					"10px sans-serif");
		}
		if (d3.select(this).select("circle").attr("r") == normalRadius) {
			d3.select(this).select("circle").transition().duration(250).attr(
					"r", bigRadius).style("fill", "lightsteelblue");
			d3.select(this).select("text").transition().duration(0).attr("x",
					bigTextSize).style("fill", "steelblue").style("stroke",
					"lightsteelblue").style("stroke-width", ".5px").style(
					"font", "20px sans-serif");
		}
	}

}