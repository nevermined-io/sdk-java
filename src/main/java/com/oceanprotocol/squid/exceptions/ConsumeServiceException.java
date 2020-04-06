/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.exceptions;

/**
 * Business Exception related with issues during the consume of a service
 */
public class ConsumeServiceException extends OceanException {

    public ConsumeServiceException(String message, Throwable e) {
        super(message, e);
    }

    public ConsumeServiceException(String message) {
        super(message);
    }
}
