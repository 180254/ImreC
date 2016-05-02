'use strict';

var express = require('express');
var router = express.Router();

var utils = require('../helpers/utils');
var conf = utils.readJson('conf.json');

var AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');

var simpleDb = new AWS.SimpleDB();

router.get('/', function (req, res, next) {
    res.setHeader('Content-Type', 'application/json');

    var params = {
        SelectExpression: 'select * from ' + conf.SimpleDb.Domain
    };

    simpleDb.select(params, function (err, data) {
        if (err) res.send(JSON.stringify(err.stack, null, ' '));
        else     res.send(JSON.stringify(data, null, ' '));
    });
});

module.exports = router;