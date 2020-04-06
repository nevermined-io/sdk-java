/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.exceptions;

/**
 * Business Exception related with DID issues during the Register process
 */
public class DIDRegisterException extends OceanException {

    public DIDRegisterException(String message, Throwable e) {
        super(message, e);
    }

    public DIDRegisterException(String message) {
        super(message);
    }
}
