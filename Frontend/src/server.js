/*
 * Server.js - serves our AngularJS App, gets Kibana over proxy and calls Middleware or serves static files
 */
var express = require('express'),
    fs = require('fs'),
    https = require('https'),
    http = require('http'),
    app = express(),
    lessMiddleware = require('less-middleware'),
    bodyParser = require('body-parser'),
    cookie = require('cookie'),
    request = require('request'),
    config=require('./config'),
    httpProxy = require('http-proxy');


/*
 *  Config
 */
config.port = 80;
config.dir = 'src';
config.cookieAuth = 'AUTH-TOKEN';
config.cookieUser = 'c_user';


/*
 *  Kibana Proxy
 */
var proxy = httpProxy.createProxyServer();
app.all(['/app/kibana', '/app/monitoring', '/app/timelion', '/app/timelion/*', '/app/graph', '/plugins/*', '/bundles/*', '/ui/*', '/es_admin/*', '/elasticsearch/*', '/api/*'], function(req, res) {
    if(req.url=='/app/kibana'){
        request(
            {
                uri: config.KIBANA_URL,
                method: 'GET',
                timeout: 180000
            },
            function (error, response, body) {

                if(error || response.statusCode != 200){
                    res.writeHead(500, {'Content-Type': 'text/html'});
                    if(response){
                        var statusCode = response.statusCode;
                    }else{
                        statusCode = 404; // If there is no any response, then is error 404
                    }
                    res.end('<div style="font-family:Sans-serif;color:#ff3738;font-size:14px;">Failed to connect to Kibana. Response code from '+config.KIBANA_URL+': <span style="font-style:italic;">Error ' + statusCode + '</span>.</div>');

                }else{
                    proxy.web(req, res, {target: config.KIBANA_URL});
                }
            }
        );
    }else{
        proxy.web(req, res, {target: config.KIBANA_URL});
    }
});


/*
 *  REST service
 */
app.use(bodyParser.urlencoded({extended : true}));
app.all(['/rest', '/rest/:method', '/rest/:method/:submethod', '/rest/:method/:submethod/:subsubmethod', '/rest/:method/:submethod/:param/:value'], function (req, res) {
    if(req.params.method){
        // Script on MW which needs to be executed
        var source=config.JAVA_URL+req.params.method+(req.params.submethod?'/'+req.params.submethod:'')+(req.params.subsubmethod?'/'+req.params.subsubmethod:'');

        switch (req.params.method){

            // Method request is different for login
            case "login":
                request(
                    {
                        method: 'POST',
                        uri: source,
                        form: req.body,
                        timeout: 10000
                    },
                    function (error, response, body) {
                        if( error ){
                            // Set status code for not found
                            res.send('{ "status": 404, "errorMessage": "[Node.js] Error: Requested resource cannot be executed." }');
                        }else if( response.statusCode != 200){
                            var errorMessage = response.statusMessage;

                            if( response.statusCode == 401 ) errorMessage = JSON.parse(response.body).errorMessage;

                            // Fetch status code and message
                            res.send('{ "status": ' + response.statusCode + ', "errorMessage": "[Node.js] ' + errorMessage + '" }');

                        }else{
                            // Check and set cookie
                            if(response.headers['set-cookie']){
                                // First clear old cookies
                                res.clearCookie(config.cookieAuth);
                                res.clearCookie(config.cookieUser);

                                // Set new cookies
                                res.append('Set-Cookie', response.headers['set-cookie']);
                            }
                            res.send(response.body);
                        }
                    }
                );
                break;

            // Default request
            default:

                // Special case for downloading reports from MW
                if(req.params.method == 'reports' && req.params.submethod == 'getReportByName'){
                    // Set body from GET method instead from POST
                    req.body[req.params.param] = req.params.value;

                    var callback = function(decompressedData, response, body){
                        // Final action - merge chunks to final data
                        var data = Buffer.concat(decompressedData);
                        // And then output data :)
                        res.writeHead(200, {
                            'Content-Type': 'application/pdf',
                            'Content-Disposition': response.headers['content-disposition'],
                            'Content-Length': data.length
                        });
                        res.end(data);
                    }

                }else{
                    var callback = function(decompressedData, response, body){
                        res.send(body);
                    }
                }

                // Set return data type
                res.type('json');

                // Some cookies are existing, needs to be checked if are correct
                if(req.headers.cookie){
                    var decompressedData = []; // Userd for storing chunks
                    request(
                        {
                            method: 'POST',
                            uri: source,
                            headers: {
                                'Cookie': req.headers.cookie
                            },
                            json: true,
                            body: req.body,
                            timeout: 180000
                        },
                        function (error, response, body) {
                            if( error ){
                                // Set status code for not found
                                res.send('{ "status": 404, "errorMessage": "[Node.js] Error: Requested resource cannot be executed." }');
                            }else if( response.statusCode != 200){
                                // Fetch status code and message
                                res.send('{ "status": ' + response.statusCode + ', "errorMessage": "[Node.js] ' + response.statusMessage + '" }');
                            }else{
                                // Special case for logout
                                if(req.params.method == 'logout'){
                                    // Clear cookies
                                    res.clearCookie(config.cookieAuth);
                                    res.clearCookie(config.cookieUser);
                                }

                                callback(decompressedData, response, body);
                            }
                        }
                    )
                    .on('data', function(chunk){
                        // Fetch and store one chunk
                        decompressedData.push(chunk);
                    });

                // There is no any existing cookies
                }else{
                    res.send('{ "status": 2, "errorMessage": "[Node.js] Please Sign in first." }');
                }
                break;
        }
    }
});


/*
 *  Source files, Less compiler and Node.js modules
 */
app.use(lessMiddleware( config.dir + '/'));
app.use(express.static( config.dir + '/'));
app.use('/node_modules', express.static('node_modules/'));


/*
 * Forward non-existing directories or files to Angular2 app
 */
app.get('*', function(req, res){
    res.sendFile('/index.html', { root: config.dir });
});


/*
 *  Start listening :)
 */
// HTTP server
http.createServer(app).listen(config.port);
// HTTPS server
/*
https.createServer({
    key: fs.readFileSync(config.dir + '/key.pem'),
    cert: fs.readFileSync(config.dir + '/cert.pem')
}, app).listen(443);
*/