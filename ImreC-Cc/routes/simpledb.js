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
        SelectExpression: 'select * from ' + conf.SimpleDb.Domain
    };

    simpleDb.select(params, function (err, data) {
        if (err) res.send(JSON.stringify(err.stack, null, ' '));
        else     res.send(JSON.stringify(data, null, ' '));
    });

    logger.log(req, 'REQ_SDB_CHECK_LOG', utils.fullUrl(req))
});

module.exports = router;