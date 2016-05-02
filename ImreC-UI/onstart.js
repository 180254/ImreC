'use strict';

var AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');
var simpleDb = new AWS.SimpleDB();

var utils = require('./helpers/utils');
var conf = utils.readJson('conf.json');

var actionRemaining = 1;
var callbackFunc = null;

var checkStartDone = function () {
    if (--actionRemaining <= 0) {
        console.log('onStart OK.');
        callbackFunc();
    }
};

var onStart = function (callback) {
    callbackFunc = callback;

    simpleDbInit();
    // checkStartDone();
};

var simpleDbInit = function () {
    var simpleDbParams = { DomainName: conf.SimpleDb.Domain };

    simpleDb.deleteDomain(simpleDbParams, function (err, data) {
        if (err) throw new Error(err.stack);
        else {
            simpleDb.createDomain(simpleDbParams, function (err, data) {
                if (err)  throw new Error(err.stack);
                else      checkStartDone();
            });
        }
    });
};

exports.onStart = onStart;