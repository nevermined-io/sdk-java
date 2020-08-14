package io.keyko.nevermined.models;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class BalanceTest {

    @Test
    public void dropsToNevermined() {
        BigInteger drops= BigInteger.valueOf(150);
        BigDecimal nvn= Balance.dropsToNevermined(drops);

        assertEquals(0, nvn.compareTo(new BigDecimal("1.5E-16")));

    }

    @Test
    public void neverminedToDrops() {
        BigInteger nvn= BigInteger.valueOf(234);
        BigInteger drops= Balance.neverminedToDrops(nvn);

        assertEquals(0, BigInteger.valueOf(234).multiply(BigInteger.TEN.pow(18)).compareTo(drops));
    }
}