/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class BalanceTest {

    @Test
    public void dropsToOcean() {
        BigInteger drops= BigInteger.valueOf(150);
        BigDecimal ocean= Balance.dropsToOcean(drops);

        assertEquals(0, ocean.compareTo(new BigDecimal("1.5E-16")));

    }

    @Test
    public void oceanToDrops() {
        BigInteger ocean= BigInteger.valueOf(234);
        BigInteger drops= Balance.oceanToDrops(ocean);

        assertEquals(0, BigInteger.valueOf(234).multiply(BigInteger.TEN.pow(18)).compareTo(drops));
    }
}