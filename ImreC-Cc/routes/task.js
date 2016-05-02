'use strict';

var express = require('express');
var router = express.Router();

var utils = require('../helpers/utils');
var scheduler = require('../helpers/scheduler');
var status = require('../helpers/status');


router.get('/schedule', function (req, res, next) {
    var scheduleParams = {
        Bucket: req.query.bucket,
        Key: req.query.key,
        ETag: req.query.etag
    };

    res.render('task/schedule', utils.clone(scheduleParams));
    scheduler.scheduleTaskIfNew(req, scheduleParams);
});


router.get('/', function (req, res, next) {

    status.getStatus(req.query.id, function (renderParams) {
        res.render('task/status', renderParams)
    }, function () {
        res.status(404).render('task/notfound');
    });
});

module.exports = router;
