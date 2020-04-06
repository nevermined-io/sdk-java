/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models;

import java.io.IOException;

public interface FromJsonToModel {

    static AbstractModel convertToModel(String json) throws IOException {
        throw new UnsupportedOperationException();
    }

    ;

}
