'use strict';

var utils = require('../helpers/utils');
var conf = utils.readJson('conf.json');

var AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');
var sdb = new AWS.SimpleDB();

var ip = function (req) {
    if (req == null) return 'null';
    return req.ip;
};

var log = function (req, action) {
    var attributes = [];

    attributes.push({ Name: '0_Source', Value: '1' });
    attributes.push({ Name: '1_Date', Value: new Date().toLocaleString() });
    attributes.push({ Name: '2_IP', Value: ip(req) });
    attributes.push({ Name: '3_Action', Value: action });

    if (arguments.length > 2) {
        for (var i = 2; i < arguments.length; i++) {
            attributes.push({ Name: '4_Arg_' + (i - 2), Value: arguments[i] });
        }
    }

    var uniqueName = utils.random2(16);
    var simpleDbParams = {
        DomainName: conf.Sdb.Domain,
        ItemName: conf.Sdb.LogItemPrefix + uniqueName,
        Attributes: attributes
    };

    sdb.putAttributes(simpleDbParams, function (err, data) {
        if (err) console.log(err.stack);
    });
};

exports.log = log;