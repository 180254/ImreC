'use strict';

var express = require('express');
var router = express.Router();

var utils = require('../helpers/utils');
var scheduler = require('../helpers/scheduler');
var status = require('../helpers/status');
var logger = require('../helpers/logger');

router.get('/schedule', function (req, res, next) {
    var scheduleParams = {
        Bucket: req.query.bucket,
        Key: req.query.key,
        ETag: req.query.etag
    };

    res.render('task/schedule', utils.clone(scheduleParams));
    scheduler.scheduleTaskIfNew(req, scheduleParams);

    logger.log(req, 'REQ_SCHEDULE', utils.fullUrl(req))
});


router.get('/', function (req, res, next) {

    status.getStatus(req.query.id, function (renderParams) {
        res.render('task/status', renderParams)
    }, function () {
        res.status(404).render('task/notfound');
    });

    logger.log(req, 'REQ_STATUS', utils.fullUrl(req))
});

module.exports = router;
