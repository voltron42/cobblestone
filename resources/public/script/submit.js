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

function submit(route,func,inputId,outputId) {
    var input = document.getElementById(inputId).value;
    var output = document.getElementById(outputId);
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == 4 && xhttp.status == 200) {
            var data = JSON.parse(this.responseText);
            output.innerHTML = data.map(func).join("");
        }
    };
    xhttp.open("POST", route, true);
    xhttp.send(input);
}

function submitSVG(inputId,outputId) {
    submit("/svg",buildXML, inputId, outputId);
}

function submitIMG(inputId,outputId) {
    submit("/img", (function(img) {
        return buildXML({
            tag:"img",
            attrs:{
                src:("data:image/png;base64,"+img)
            }
        });
    }), inputId, outputId);
}