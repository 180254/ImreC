'use strict';

var express = require('express');
var router = express.Router();

var s3post = require('../helpers/s3post');
var utils = require('../helpers/utils');

var AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');
var simpleDb = new AWS.SimpleDB();
var s3 = new AWS.S3();

var conf = utils.readJson('conf.json');
var confAws = utils.readJson('config.json');

router.get('/', function (req, res, next) {

    var uuid = utils.uuid();
    var server = req.protocol + '://' + req.get('host') || req.socket.remoteAddress;

    var s3Policy = new s3post.Policy(utils.clone(conf.S3.Policy));
    s3Policy.setServer(server);

    var s3Form = new s3post.S3Form(s3Policy);
    var s3Fields = s3Form.getFieldsBase();
    s3Form.addS3FormFields(s3Fields, uuid);
    s3Form.addS3CredentialsFields(s3Fields, confAws);
    s3Form.addCustomField(s3Fields, 'x-amz-meta-uploaded-by', req.ip);
    s3Form.addCustomField(s3Fields, 'x-amz-meta-filename', '${filename}');

    res.render('index', {
        s3url: conf.S3.Url,
        s3fields: s3Fields
    });
});

router.get('/submitted', function (req, res, next) {
    var objectParams = {
        Bucket: req.query.bucket,
        Key: req.query.key
    };

    res.render('submitted', utils.clone(objectParams));

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
});

module.exports = router;
