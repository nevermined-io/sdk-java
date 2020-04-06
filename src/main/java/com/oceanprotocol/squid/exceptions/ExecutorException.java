/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.exceptions;

/**
 * Business Exception related with Executor interactions issues
 */
public class ExecutorException extends OceanException {


    public ExecutorException(String message, Throwable e) {
        super(message, e);
    }

    public ExecutorException(String message) {
        super(message);
    }
}
