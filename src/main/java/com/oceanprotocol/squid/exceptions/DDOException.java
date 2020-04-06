/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.exceptions;

/**
 * Business Exception related with DDOs issues
 */
public class DDOException extends OceanException {

    public DDOException(String message, Throwable e) {
        super(message, e);
    }

    public DDOException(String message) {
        super(message);
    }
}
