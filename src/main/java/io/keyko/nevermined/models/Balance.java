package io.keyko.nevermined.models;


import java.math.BigDecimal;
import java.math.BigInteger;

public class Balance {

    public enum Unit {
        DROP("drop", 0),
        VODKA("vodka", 0),
        NVN("nvn", 18);

        private String name;
        private BigInteger factor;

        Unit(String name, int factor) {
            this.name = name;
            this.factor = BigInteger.TEN.pow(factor);
        }
    }

    private BigInteger eth;

    private BigInteger drops;

    public Balance() {
        this.eth = BigInteger.valueOf(0);
        this.drops = BigInteger.valueOf(0);
    }

    public Balance(BigInteger eth, BigInteger drops) {
        this.eth = eth;
        this.drops = drops;
    }

    public BigInteger getEth() {
        return eth;
    }

    public BigInteger getDrops() {
        return drops;
    }

    public BigDecimal getNeverminedTokens() {
        return dropsToNevermined(this.drops);
    }


    public static BigDecimal dropsToNevermined(BigInteger drops) {
        return new BigDecimal(drops).divide(new BigDecimal(Unit.NVN.factor));
    }

    public static BigInteger neverminedToDrops(BigInteger nevermined) {
        return nevermined.multiply(Unit.NVN.factor);
    }

    @Override
    public String toString() {
        return "Balance{" +
                "eth=" + eth +
                ", drops=" + drops +
                '}';
    }
}