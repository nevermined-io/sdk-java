package io.keyko.nevermined.api;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;
import org.web3j.crypto.Keys;

import static org.junit.Assert.assertNotNull;

public class NeverminedAPIIT {

    @Test
    public void buildAPIFromConfig() throws Exception {

        Config config = ConfigFactory.load();

        NeverminedAPI neverminedAPI = NeverminedAPI.getInstance(config);
        assertNotNull(neverminedAPI.getMainAccount());
        Assert.assertEquals(Keys.toChecksumAddress(config.getString("account.main.address")), neverminedAPI.getMainAccount().address);
        assertNotNull(neverminedAPI.getAssetsAPI());
        assertNotNull(neverminedAPI.getAccountsAPI());
        assertNotNull(neverminedAPI.getSecretStoreAPI());

    }

}
