/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package io.keyko.ocean.exceptions;

/**
 * Business Exception related with Encrypt/Decrypt issues
 */
public class EncryptionException extends OceanException {

    public EncryptionException(String message, Throwable e) {
        super(message, e);
    }

    public EncryptionException(String message) {
        super(message);
    }
}
