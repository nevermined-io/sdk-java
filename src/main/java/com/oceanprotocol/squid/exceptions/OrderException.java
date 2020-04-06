/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.exceptions;

/**
 * Business Exception related with issues during the order process
 */
public class OrderException extends OceanException {
    public OrderException(String message, Throwable e) {
        super(message, e);
    }

    public OrderException(String message) {
        super(message);
    }
}
