/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

var wsUri = "ws://" + document.location.host + document.location.pathname + "stockUpdate";
var websocket = new WebSocket(wsUri);
var output = document.getElementById("output");

websocket.onmessage = function(evt) { onMessage(evt) };
websocket.onerror = function(evt) { onError(evt) };
websocket.onopen = function(evt) { onOpen(evt) };

function onOpen() {
    writeToScreen("Connected to " + wsUri);
}

function onMessage(evt) {
    //alert("received: " + evt.data);
    console.log("received: " + evt.data);
    if (typeof evt.data == "string") {
        displayStockInfo(evt.data);
    } else {
        writeToScreen('<span style="color: red;">ERROR: </span> Unsupported ent.data type, ' + evt.data);
    } 
}

function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

function displayStockInfo(stockData)
{
    output.innerHTML += stockData + "<br>";
}


function writeToScreen(message) {
    errorMessage.innerHTML += message + "<br>";
}
