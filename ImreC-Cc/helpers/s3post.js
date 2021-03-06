'use strict';

/**
 * base credits radamus @ github
 * https://github.com/amgnet-weeia/awslab4
 */

var moment = require('moment');
var ciphers = require('./ciphers');
var utils = require('./utils');

var ACCESS_KEY_FIELD_NAME = 'AWSAccessKeyId';
var POLICY_FIELD_NAME = 'policy';
var SIGNATURE_FIELD_NAME = 'signature';

var Policy = function (policyData) {
    this.policy = utils.clone(policyData);
    this.policy.expiration = moment().add(policyData.expiration).toJSON();
};

Policy.prototype.getPolicy = function () {
    return this.policy;
};

Policy.prototype.setServer = function (server) {
    this.policy.conditions[3].success_action_redirect =
        server + this.policy.conditions[3].success_action_redirect
};

Policy.prototype.setFilenamePrefix = function (prefix) {
    this.policy.conditions[0][2] =
        prefix + this.policy.conditions[0][2];
};

Policy.prototype.setUploader = function (uploader) {
    this.policy.conditions[6][2] = uploader;
};

Policy.prototype.setCollector = function (collector) {
    this.policy.conditions[8][2] = collector;
};

Policy.prototype.generateEncodedPolicyDocument = function () {
    return ciphers.encode(this.policy, 'base64');
};

Policy.prototype.generateSignature = function (secretAccessKey) {
    return ciphers.hmac('sha1', secretAccessKey, this.generateEncodedPolicyDocument(), 'base64');
};

var S3Form = function (policy) {
    if (policy instanceof Policy)
        this.policyObj = policy;
    else {
        throw new Error('policyObj instanceof Policy');
    }
};

S3Form.prototype.getFieldsBase = function () {
    return [];
};

S3Form.prototype.field = function (name, value) {
    return { name: name, value: value };
};

//
S3Form.prototype.addS3FormFields = function (fields, filename) {
    filename = typeof filename !== 'undefined' ? filename : '${filename}';

    var conditions = this.policyObj.getPolicy().conditions;
    var self = this;

    conditions.forEach(function (elem) {
        if (Array.isArray(elem)) {
            if (elem[1] === '$key') {
                fields.push(self.field('key', elem[2] + filename));
            }

        } else {
            var key = Object.keys(elem)[0];
            var value = elem[key];
            if (key !== 'bucket') {
                fields.push(self.field(key, value));
            }
        }
    });
};

S3Form.prototype.addS3CredentialsFields = function (fields, awsConfig) {
    fields.push(this.field(ACCESS_KEY_FIELD_NAME, awsConfig.accessKeyId));
    fields.push(this.field(POLICY_FIELD_NAME, this.policyObj.generateEncodedPolicyDocument()));
    fields.push(this.field(SIGNATURE_FIELD_NAME, this.policyObj.generateSignature(awsConfig.secretAccessKey)));
    return fields;
};

S3Form.prototype.setField = function (fields, name, value) {
    for (var i = 0, len = fields.length; i < len; i++) {
        if (fields[i].name === name) {
            fields[i].value = value;
            return;
        }
    }

    fields.push(this.field(name, value));
};

exports.Policy = Policy;
exports.S3Form = S3Form;