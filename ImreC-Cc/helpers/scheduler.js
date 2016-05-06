'use strict';

var logger = require('../helpers/logger');
var utils = require('../helpers/utils');
var s3post = require('../helpers/s3post');
var selfIp = require('../helpers/selfip');

var conf = utils.readJson('conf.json');

var AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');
var s3 = new AWS.S3();
var sqs = new AWS.SQS();

function scheduleTaskIfNew(req, scheduleParams) {
    if (scheduleParams.Bucket !== conf.S3.Name) {
        return;
    }

    var headParams = {
        Bucket: scheduleParams.Bucket,
        Key: scheduleParams.Key
    };

    s3.headObject(headParams, function (err, data) {
        if (!err && data.Metadata.status === '0') {
            var newMetadata = data.Metadata;
            newMetadata.scheduler = selfIp.ip();

            addSqsMessage(req, scheduleParams, function () {
                bumpProgress(req, scheduleParams, newMetadata);
            });

            logger.log(req, 'TASK_NEW', scheduleParams.Key, JSON.stringify(newMetadata));
        }
    });
}

function addSqsMessage(req, scheduleParams, callback) {
    var params = {
        MessageBody: scheduleParams.Key,
        QueueUrl: conf.Sqs.Url
    };
    sqs.sendMessage(params, function (err, data) {
        if (err) console.log(err.stack);
        else {
            callback();
            logger.log(req, 'TASK_SQS', scheduleParams.Key);
        }
    });
}

function bumpProgress(req, scheduleParams, metadata) {
    metadata = utils.clone(metadata);
    metadata.status = (Number.parseInt(metadata.status) + 1).toString();
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
        else     logger.log(req, 'TASK_STATUS=1', scheduleParams.Key);
    });
}

exports.scheduleTaskIfNew = scheduleTaskIfNew;
