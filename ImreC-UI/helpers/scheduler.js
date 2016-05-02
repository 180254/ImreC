'use strict';

var utils = require('../helpers/utils');
var conf = utils.readJson('conf.json');

var AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');
var simpleDb = new AWS.SimpleDB();
var s3 = new AWS.S3();

function scheduleTaskIfNew(req, scheduleParams) {
    if (scheduleParams.Bucket !== conf.S3.Name) {
        return;
    }

    var headParams = {
        Bucket: scheduleParams.Bucket,
        Key: scheduleParams.Key
    };

    s3.headObject(headParams, function (err, data) {
        if (err) console.log(err, err.stack);
        else if (data.Metadata.progress === '0') {
            bumpProgress(req, scheduleParams, data.Metadata);
            logState(req, data.Metadata);
            // added to sqs by event
        }
    });
}

function bumpProgress(req, scheduleParams, metadata) {
    metadata = utils.clone(metadata);
    metadata.progress = (Number.parseInt(metadata.progress) + 1).toString();

    var copyParams = {
        Bucket: scheduleParams.Bucket,
        CopySource: scheduleParams.Bucket + '/' + scheduleParams.Key,
        Key: scheduleParams.Key,
        Metadata: metadata,
        MetadataDirective: 'REPLACE'
    };

    s3.copyObject(copyParams, function (err, data) {
        if (err) console.log(err.stack);
        else     logState(req, metadata);
    });
}

function logState(req, metadata) {
    var logText = new Date().toLocaleString() +
        ' | ' + req.ip +
        ' | ' + JSON.stringify(metadata);

    var simpleDbParams = {
        DomainName: conf.SimpleDb.Domain,
        ItemName: conf.SimpleDb.LogItemName,
        Attributes: [{ Name: utils.random2(16), Value: logText }]
    };

    simpleDb.putAttributes(simpleDbParams, function (err, data) {
        if (err) console.log(err.stack);
    });
}

exports.scheduleTaskIfNew = scheduleTaskIfNew;
