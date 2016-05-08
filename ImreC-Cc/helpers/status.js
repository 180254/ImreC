'use strict';

var utils = require('../helpers/utils');
var conf = utils.readJson('conf.json');

var AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');
var s3 = new AWS.S3();

var statusDescriptions = [
    'File successfully uploaded. Resize task to be scheduled. Please wait.',
    'File successfully uploaded. Resize task scheduled. Please wait.',
    'System is resizing this file right now. Please wait.',
    'File processed. Results ready to download.'
];

var getStatus = function (bucketKey, callbackOk, callbackFail) {
    var headParams = {
        Bucket: conf.S3.Name,
        Key: bucketKey
    };

    s3.headObject(headParams, function (err, data) {
        if (err) {
            callbackFail();

        } else {
            var renderParams = {
                id: bucketKey,
                url: conf.S3.Url + '/' + bucketKey,
                scale: data.Metadata.task,
                status: statusDescriptions[Number.parseInt(data.Metadata.status)],
                isDone: data.Metadata.status === '3',
                collector: data.Metadata.collector,
                scheduler: data.Metadata.scheduler,
                worker: data.Metadata.worker
            };

            callbackOk(renderParams);
        }
    });
};

exports.getStatus = getStatus;

