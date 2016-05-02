'use strict';

var utils = require('../helpers/utils');
var s3post = require('../helpers/s3post');
var conf = utils.readJson('conf.json');

var AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');
var s3 = new AWS.S3();
var sqs = new AWS.SQS();
var sdb = new AWS.SimpleDB();

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
            addSqsMessage(scheduleParams);
            logState(req, data.Metadata);
        }
    });
}

function addSqsMessage(scheduleParams) {
    var params = {
        MessageBody: scheduleParams.Key,
        QueueUrl: conf.SQS.Url
    };
    sqs.sendMessage(params, function (err, data) {
        if (err) console.log(err.stack);
    });
}

function bumpProgress(req, scheduleParams, metadata) {
    metadata = utils.clone(metadata);
    metadata.progress = (Number.parseInt(metadata.progress) + 1).toString();
    var s3Policy = new s3post.Policy(conf.S3.Policy);

    var copyParams = {
        Bucket: scheduleParams.Bucket,
        CopySource: scheduleParams.Bucket + '/' + scheduleParams.Key,
        Key: scheduleParams.Key,
        ACL: s3Policy.getPolicy().conditions[2].acl,
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

    sdb.putAttributes(simpleDbParams, function (err, data) {
        if (err) console.log(err.stack);
    });
}

exports.scheduleTaskIfNew = scheduleTaskIfNew;