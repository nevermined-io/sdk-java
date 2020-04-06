/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models.service.types;

import com.oceanprotocol.squid.models.service.Service;

import java.util.Comparator;

public class DDOServiceIndexSorter implements Comparator<Service> {
    @Override
    public int compare(Service service1, Service service2) {
        if (service1.index > service2.index)
            return 1;
        return -1;
    }
}
