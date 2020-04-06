/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.exceptions;


/**
 * Business Exception related with Service Agreement issues
 */
public class ServiceAgreementException extends OceanException {

    private String serviceAgreementId;

    public ServiceAgreementException(String serviceAgreementId, String message, Throwable e) {

        super(message, e);
        this.serviceAgreementId = serviceAgreementId;

    }

    public ServiceAgreementException(String serviceAgreementId, String message) {

        super(message);
        this.serviceAgreementId = serviceAgreementId;

    }
}
