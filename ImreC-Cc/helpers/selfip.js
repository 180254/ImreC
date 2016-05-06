'use strict';

var eIP = require('external-ip')();
var myPublicIp = '?';

eIP(function (err, ip) {
    if (!err)
        myPublicIp = ip;
});

var ip = function () {
    return myPublicIp;
};

exports.ip = ip;