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

var scheduleTaskIfNew = function (req, scheduleParams) {
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

            bumpProgress(req, scheduleParams, newMetadata, function () {
                addSqsMessage(req, scheduleParams);
            });

            logger.log(req, 'TASK_NEW', 'OK', scheduleParams.Key, JSON.stringify(newMetadata));
        }
    });
};

var bumpProgress = function (req, scheduleParams, metadata, callback) {
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
        if (err) {
            deleteObject(req, scheduleParams);
            logger.log(req, 'TASK_PROGRESS', 'ERROR', scheduleParams.Key, JSON.stringify(err.stack));
            console.log(err.stack);
        }
        else {
            callback();
            logger.log(req, 'TASK_PROGRESS', 'OK', scheduleParams.Key, metadata.status);
        }
    });
};

var addSqsMessage = function (req, scheduleParams) {
    var params = {
        MessageBody: scheduleParams.Key,
        QueueUrl: conf.Sqs.Url
    };

    sqs.sendMessage(params, function (err, data) {
        if (err) {
            deleteObject(req, scheduleParams);
            logger.log(req, 'TASK_SQS', 'ERROR', scheduleParams.Key, JSON.stringify(err.stack));
            console.log(err.stack);
        }
        else {
            logger.log(req, 'TASK_SQS', 'OK', scheduleParams.Key);
        }
    });
};

var deleteObject = function (req, scheduleParams) {
    var params = {
        Bucket: scheduleParams.Bucket,
        Key: scheduleParams.Key
    };

    s3.deleteObject(params, function (err, data) {
        if (err) {
            logger.log(req, 'TASK_DELETE', 'ERROR', scheduleParams.Key, JSON.stringify(err.stack));
            console.log(err.stack);
        }
        else {
            logger.log(req, 'TASK_DELETE', 'OK', scheduleParams.Key);
        }
    });
};

exports.scheduleTaskIfNew = scheduleTaskIfNew;
