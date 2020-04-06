/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.exceptions;

/**
 * Business Exception related with LockReward Fulfill issues
 */
public class LockRewardFulfillException extends OceanException {

    public LockRewardFulfillException(String message, Throwable e) {
        super(message, e);
    }

    public LockRewardFulfillException(String message) {
        super(message);
    }
}
