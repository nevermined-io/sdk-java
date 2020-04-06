/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.exceptions;

/**
 * Business Exception related with Ethereum interactions issues
 */
public class EthereumException extends OceanException {


    public EthereumException(String message, Throwable e) {
        super(message, e);
    }

    public EthereumException(String message) {
        super(message);
    }
}
