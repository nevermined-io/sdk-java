/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.exceptions;

/**
 * Base Class to implement a hierarchy of Functional Ocean's Exceptions
 */
public abstract class OceanException extends Exception {

    public OceanException(String message, Throwable e) {
        super(message, e);
    }

    public OceanException(String message) {
        super(message);
    }
}
