var version_object = {
  "name": "nodetest",
  "version": "0.0.1"
};

var http = require("http");
var mongodb = require("mongodb");
var fs = require("fs");

var server = new http.Server();


server.listen(process.env.VCAP_APP_PORT || 3000);

server.on("close", function() {
  console.log("closing server");
});

server.on("request", function (request, response){
  var body;
  console.log("request received");
  console.log("request method: " + request.method);
  console.log("request url:" + request.url);
  if (request.method == "GET") {
    switch(request.url) {
    case "/version":
      body = JSON.stringify(version_object);
      response.writeHead(200, {
        "Content-Length": body.length,
        "Content-Type": "text/plain"});
      response.write(body);
      response.end();
      break;
    case "/clclient":
      response.writeHead(200);
      body = fs.createReadStream("clclient-0.0.1-standalone.jar");
      body.pipe(response);
      response.end();
    default:
      response.statusCode = 404;
      response.end();
    }
  }
});

process.on("SIGINT", function () {
  console.log("SIGINT received");
  process.exit();
});

process.on("exit", function() {
  server.close();
});

console.log("Hello");
