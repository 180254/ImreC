'use strict';

var express = require('express');
var router = express.Router();

var s3post = require('../helpers/s3post');
var utils = require('../helpers/utils');
var logger = require('../helpers/logger');
var selfIp = require('../helpers/selfip');

var conf = utils.readJson('conf.json');
var confAws = utils.readJson('config.json');

router.get('/', function (req, res, next) {
    var uuid = utils.random2(8);
    var server = req.protocol + '://' + (req.get('host') || req.socket.remoteAddress);

    var selfIpCopy = selfIp.ip();

    var s3Policy = new s3post.Policy(conf.S3.Policy);
    s3Policy.setServer(server);
    s3Policy.setFilenamePrefix(uuid);
    s3Policy.setUploader(req.ip);
    s3Policy.setCollector(selfIpCopy);

    var s3Form = new s3post.S3Form(s3Policy);
    var s3Fields = s3Form.getFieldsBase();
    s3Form.addS3FormFields(s3Fields, uuid + '_${filename}');
    s3Form.addS3CredentialsFields(s3Fields, confAws);
    s3Form.setField(s3Fields, 'x-amz-meta-filename', '${filename}');
    s3Form.setField(s3Fields, 'x-amz-meta-uploader', req.ip);
    s3Form.setField(s3Fields, 'x-amz-meta-collector', selfIpCopy);
    s3Form.setField(s3Fields, 'x-amz-meta-scheduler', '?');
    s3Form.setField(s3Fields, 'x-amz-meta-worker', '?');
    s3Form.setField(s3Fields, 'x-amz-meta-status', '0');

    res.render('index', {
        s3url: conf.S3.Url,
        s3fields: s3Fields
    });

    logger.log(req, 'REQ_FORM', utils.fullUrl(req))
});

module.exports = router;
