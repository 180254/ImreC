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

    attributes.push({ Name: 'aSource', Value: '1' });
    attributes.push({ Name: 'bDate', Value: currentDateFormatted() });
    attributes.push({ Name: 'cIP', Value: ip(req) });
    attributes.push({ Name: 'dAction', Value: action });

    if (arguments.length > 2) {
        for (var i = 2; i < arguments.length; i++) {
            attributes.push({ Name: 'eArg_' + (i - 2), Value: arguments[i] });
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

function currentDateFormatted() {
    var date = new Date();

    return date.getFullYear()
        + '-'
        + utils.pad(date.getMonth(), 2)
        + '-'
        + utils.pad(date.getDate(), 2)
        + ' '
        + utils.pad(date.getHours(), 2)
        + ':'
        + utils.pad(date.getMinutes(), 2)
        + ':'
        + utils.pad(date.getSeconds(), 2);
}

exports.log = log;