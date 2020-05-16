package io.keyko.nevermined.external;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.nevermined.models.gateway.Status;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class GatewayServiceIT {

    private static final String gatewayUrl = "http://localhost:8030";
    private static final Config config = ConfigFactory.load();

    @BeforeClass
    public static void setUp() throws Exception {

    }

    @Test
    public void getStatus() throws IOException {
        final Status gatewayStatus = GatewayService.getStatus(gatewayUrl);

        assertTrue(gatewayStatus.contracts.size() > 10);
        assertEquals(config.getString("provider.address").toLowerCase(), gatewayStatus.providerAddress.toLowerCase());
        assertTrue(gatewayStatus.ecdsaPublicKey.startsWith("0x"));

    }
}