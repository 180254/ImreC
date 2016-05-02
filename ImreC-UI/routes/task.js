'use strict';

var express = require('express');
var router = express.Router();

var utils = require('../helpers/utils');
var conf = utils.readJson('conf.json');

var AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');
var simpleDb = new AWS.SimpleDB();
var s3 = new AWS.S3();

router.get('/schedule', function (req, res, next) {
    var objectParams = {
        Bucket: req.query.bucket,
        Key: req.query.key
    };

    res.render('task/schedule', utils.clone(objectParams));
    scheduleTask(objectParams, req);
});

function scheduleTask(objectParams, req) {
    s3.getObject(objectParams, function (err, data) {
        if (err) console.log(err.stack);
        else {

            var logText = new Date().toLocaleString() +
                ' | ' + req.ip +
                ' | ' + JSON.stringify(data.Metadata);

            var simpleDbParams = {
                DomainName: conf.SimpleDb.Domain,
                ItemName: conf.SimpleDb.LogItemName,
                Attributes: [{ Name: utils.random2(16), Value: logText }]
            };

            simpleDb.putAttributes(simpleDbParams, function (err, data) {
                if (err) console.log(err.stack);
            });
        }
    });
};

module.exports = router;
