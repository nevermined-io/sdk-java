/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package io.keyko.ocean.api;

import io.keyko.ocean.models.Account;
import io.keyko.ocean.models.Balance;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.*;

public class AccountsApiIT {

    private static OceanAPI oceanAPI;

    @BeforeClass
    public static void setUp() throws Exception {


        Config config = ConfigFactory.load();
        oceanAPI = OceanAPI.getInstance(config);

        assertNotNull(oceanAPI.getAccountsAPI());
        assertNotNull(oceanAPI.getMainAccount());

    }

    @Test
    public void list() throws Exception {

        List<Account> accounts = oceanAPI.getAccountsAPI().list();
        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());
    }

    @Test
    public void balance() throws Exception {

        Balance balance = oceanAPI.getAccountsAPI().balance(oceanAPI.getMainAccount());
        assertNotNull(balance);
        assertEquals(1, balance.getEth().compareTo(BigInteger.ZERO));
        assertEquals(1, balance.getDrops().compareTo(BigInteger.ZERO));
    }

}
