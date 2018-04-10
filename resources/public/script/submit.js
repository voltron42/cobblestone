function submitTileDoc(inputId) {
    var input = document.getElementById(inputId).value;
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            var x=window.open();
            x.document.open();
            x.document.write(JSON.parse(this.responseText));
            x.document.close();
        }
    };
    xhttp.open("POST", "/build", true);
    xhttp.send(input);
}