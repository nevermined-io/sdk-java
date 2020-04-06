/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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
        assertEquals(Keys.toChecksumAddress(config.getString("account.main.address")), oceanAPI.getMainAccount().address);
        assertNotNull(oceanAPI.getAssetsAPI());
        assertNotNull(oceanAPI.getAccountsAPI());
        assertNotNull(oceanAPI.getSecretStoreAPI());

    }

}
