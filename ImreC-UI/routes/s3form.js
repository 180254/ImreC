'use strict';

var express = require('express');
var router = express.Router();

var s3post = require('../helpers/s3post');
var utils = require('../helpers/utils');

var AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');

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
        s3url: conf.S3.URL,
        s3fields: s3Fields
    });
});

router.get('/submitted', function (req, res, next) {
    res.render('submitted', {
        bucket: req.query.bucket,
        key: req.query.key
    });
});

module.exports = router;
