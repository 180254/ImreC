'use strict';

var express = require('express');
var router = express.Router();

var utils = require('../helpers/utils');
var scheduler = require('../helpers/scheduler');

router.get('/schedule', function (req, res, next) {
    var scheduleParams = {
        Bucket: req.query.bucket,
        Key: req.query.key,
        ETag: req.query.etag
    };

    res.render('task/schedule', utils.clone(scheduleParams));
    scheduler.scheduleTaskIfNew(req, scheduleParams);
});

module.exports = router;
