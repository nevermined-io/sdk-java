/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api;

import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.Balance;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class TokensApiIT {

    private static final Logger log = LogManager.getLogger(TokensApiIT.class);

    private static OceanAPI oceanAPI;
    private static Config config;

    @BeforeClass
    public static void setUp() throws Exception {


        config = ConfigFactory.load();
        oceanAPI = OceanAPI.getInstance(config);

        assertNotNull(oceanAPI.getAccountsAPI());
        assertNotNull(oceanAPI.getMainAccount());

    }

    @Test
    public void transfer() throws Exception {

        String receiverAddress= config.getString("account.parity.address2");
        String receiverPasswd= config.getString("account.parity.password2");

        Account receiverAccount= new Account(receiverAddress, receiverPasswd);

        oceanAPI.getTokensAPI().request(BigInteger.ONE);
        Balance balanceBefore = oceanAPI.getAccountsAPI().balance(receiverAccount);
        assertNotNull(balanceBefore);

        oceanAPI.getTokensAPI().transfer(receiverAddress, BigInteger.ONE);

        Balance balanceAfter = oceanAPI.getAccountsAPI().balance(receiverAccount);

        log.debug("Balance Before is: " + balanceBefore);
        log.debug("Balance After is: " + balanceAfter);

        assertEquals(-1, balanceBefore.getOceanTokens().compareTo(balanceAfter.getOceanTokens()));

    }

    @Test
    public void requestTokens() throws Exception {

        BigInteger tokens = BigInteger.ONE;

        Balance balanceBefore = oceanAPI.getAccountsAPI().balance(oceanAPI.getMainAccount());
        log.debug("Balance before: " + balanceBefore.toString());

        TransactionReceipt receipt = oceanAPI.getTokensAPI().request(tokens);

        assertTrue(receipt.isStatusOK());

        Balance balanceAfter = oceanAPI.getAccountsAPI().balance(oceanAPI.getMainAccount());

        log.debug("Balance after: " + balanceAfter.toString());

        BigDecimal before= balanceBefore.getOceanTokens();
        BigDecimal after= balanceAfter.getOceanTokens();
        assertEquals(0, after.compareTo(before.add(BigDecimal.ONE)));
    }

}
