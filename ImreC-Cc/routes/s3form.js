'use strict';

var express = require('express');
var router = express.Router();

var s3post = require('../helpers/s3post');
var utils = require('../helpers/utils');
var logger = require('../helpers/logger');

var conf = utils.readJson('conf.json');
var confAws = utils.readJson('config.json');

router.get('/', function (req, res, next) {
    var uuid = utils.random2(8);
    var server = req.protocol + '://' + (req.get('host') || req.socket.remoteAddress);

    var s3Policy = new s3post.Policy(conf.S3.Policy);
    s3Policy.setServer(server);
    s3Policy.setFilenamePrefix(uuid);
    s3Policy.setUploadedBy(req.ip);

    var s3Form = new s3post.S3Form(s3Policy);
    var s3Fields = s3Form.getFieldsBase();
    s3Form.addS3FormFields(s3Fields, uuid + '_${filename}');
    s3Form.addS3CredentialsFields(s3Fields, confAws);
    s3Form.setField(s3Fields, 'x-amz-meta-workstatus', '0');
    s3Form.setField(s3Fields, 'x-amz-meta-uploadedby', req.ip);
    s3Form.setField(s3Fields, 'x-amz-meta-ofilename', '${filename}');

    res.render('index', {
        s3url: conf.S3.Url,
        s3fields: s3Fields
    });

    logger.log(req, 'REQ_FORM', utils.fullUrl(req))
});

module.exports = router;
