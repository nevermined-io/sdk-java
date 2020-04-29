package io.keyko.nevermined.models;

import io.keyko.nevermined.models.asset.AssetMetadata;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class OrderTest {

    @Test
    public void toStringTest() {
        Order order= new Order("id", new AssetMetadata(), 10);
        assertTrue(order.toString().contains("id='id'"));
    }
}