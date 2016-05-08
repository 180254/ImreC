'use strict';

var express = require('express');
var router = express.Router();

var utils = require('../helpers/utils');
var conf = utils.readJson('conf.json');
var logger = require('../helpers/logger');

var AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');

var simpleDb = new AWS.SimpleDB();

router.get('/', function (req, res, next) {
    res.setHeader('Content-Type', 'application/json');

    var params = {
        SelectExpression: 'select * from ' + conf.Sdb.Domain
        + ' where bDate like "%"'
        + ' order by bDate desc'
    };

    simpleDb.select(params, function (err, data) {
        if (err) res.send(JSON.stringify(err.stack, null, ' '));
        else {
            sortAttributesInResult(data);
            res.send(JSON.stringify(data, null, ' '));
        }
    });

    logger.log(req, 'REQ_SDB', utils.fullUrl(req))
});

var sortAttributesInResult = function (data) {
    if (data.Items) {
        for (var i = 0; i < data.Items.length; i++) {
            data.Items[i].Attributes.sort(function (a, b) {
                return a.Name.localeCompare(b.Name);
            });
        }
    }
};

module.exports = router;