'use strict';

var utils = require('../helpers/utils');
var conf = utils.readJson('conf.json');

var AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');
var sdb = new AWS.SimpleDB();

var ip = function (req) {
    if (req == null) return null;
    return req.ip;
};

var log = function (req, action, message) {
    var logText =
        '(0)' +
        new Date().toLocaleString() +
        ' | ' + (ip(req) || '?') +
        ' | ' + action +
        ' | ' + message;

    var simpleDbParams = {
        DomainName: conf.SimpleDb.Domain,
        ItemName: conf.SimpleDb.LogItemName,
        Attributes: [{ Name: utils.random2(16), Value: logText }]
    };

    sdb.putAttributes(simpleDbParams, function (err, data) {
        if (err) console.log(err.stack);
    });
};

exports.log = log;