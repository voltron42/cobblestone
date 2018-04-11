function buildXML(node) {
    if ((typeof node) == "string") {
        return node;
    } else {
        node.attrs = node.attrs?node.attrs:{};
        node.content = node.content?node.content:[];
        var attrs = Object.entries(node.attrs).map(function(attr){
            return " " + attr[0] + "='" + attr[1] + "'";
        });
        var out = "<" + node.tag + attrs;
        if (node.content.length == 0) {
            out += "/>";
        } else {
            out += ">" + node.content.map(buildXML).join("") + "</" + node.tag + ">";
        }
        return out;
    }
}

function submitTileDoc(inputId,outputId) {
    var input = document.getElementById(inputId).value;
    var output = document.getElementById(outputId);
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == 4 && xhttp.status == 200) {
            var svgData = JSON.parse(this.responseText);
            output.innerHTML = svgData.map(buildXML).join("");
        }
    };
    xhttp.open("POST", "/build", true);
    xhttp.send(input);
}