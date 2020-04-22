package io.keyko.nevermind.api;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;
import org.web3j.crypto.Keys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OceanApiIT {

    @Test
    public void buildAPIFromConfig() throws Exception {

        Config config = ConfigFactory.load();

        OceanAPI oceanAPI = OceanAPI.getInstance(config);
        assertNotNull(oceanAPI.getMainAccount());
        Assert.assertEquals(Keys.toChecksumAddress(config.getString("account.main.address")), oceanAPI.getMainAccount().address);
        assertNotNull(oceanAPI.getAssetsAPI());
        assertNotNull(oceanAPI.getAccountsAPI());
        assertNotNull(oceanAPI.getSecretStoreAPI());

    }

}
