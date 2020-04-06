/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package io.keyko.ocean.models;

import io.keyko.ocean.models.asset.AssetMetadata;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class OrderTest {

    @Test
    public void toStringTest() {
        Order order= new Order("id", new AssetMetadata(), 10);
        assertTrue(order.toString().contains("id='id'"));
    }
}