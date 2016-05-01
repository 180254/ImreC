'use strict';

var fs = require('fs');

//  credits radamus @ github
// https://github.com/amgnet-weeia/awslab4
var readJson = function (fileName) {
    if (!fs.existsSync(fileName)) {
        throw new Error('unable to open file: ' + fileName);
    }

    var data = fs.readFileSync(fileName, { encoding: 'utf8' });
    return JSON.parse(data);
};

// credits: friends @ stackoverflow
// http://stackoverflow.com/posts/2117523/revisions
var uuid = function () {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
};

// credits: friends @ stackoverflow
// http://stackoverflow.com/questions/728360/most-elegant-way-to-clone-a-javascript-object
var clone = function (obj) {
    var copy;

    // Handle the 3 simple types, and null or undefined
    if (null == obj || 'object' != typeof obj) return obj;

    // Handle Date
    if (obj instanceof Date) {
        copy = new Date();
        copy.setTime(obj.getTime());
        return copy;
    }

    // Handle Array
    if (obj instanceof Array) {
        copy = [];
        for (var i = 0, len = obj.length; i < len; i++) {
            copy[i] = clone(obj[i]);
        }
        return copy;
    }

    // Handle Object
    if (obj instanceof Object) {
        copy = {};
        for (var attr in obj) {
            if (obj.hasOwnProperty(attr)) copy[attr] = clone(obj[attr]);
        }
        return copy;
    }

    throw new Error("Unable to copy obj! Its type isn't supported.");
};

exports.readJson = readJson;
exports.uuid = uuid;
exports.clone = clone;
