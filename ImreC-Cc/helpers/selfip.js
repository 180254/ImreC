'use strict';

var publicIp = require('public-ip');
var myPublicIp = '?';

publicIp.v4(function (err, ip) {
    console.log('v4');
    console.log(err);
    console.log(ip);
    if (ip) myPublicIp = ip;
});

publicIp.v6(function (err, ip) {
    console.log('v6');
    console.log(err);
    console.log(ip);
    if (ip) myPublicIp = ip;
});

var ip = function () {
    return myPublicIp;
};

exports.ip = ip;