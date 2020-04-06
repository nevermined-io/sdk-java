/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.manager;

import com.oceanprotocol.keeper.contracts.OceanToken;
import com.oceanprotocol.squid.exceptions.EthereumException;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.common.web3.KeeperService;
import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.Balance;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountsManagerTest {

    private static final Logger log = LogManager.getLogger(AccountsManagerTest.class);

    private static AquariusService aquarius;
    private static final Config config = ConfigFactory.load();

    @BeforeClass
    public static void setUp() throws Exception {
        log.debug("Setting Up DTO's");

        aquarius= ManagerHelper.getAquarius(config);
    }

    @Test
    public void getAccounts()  throws IOException, CipherException, EthereumException {
        List<String> expectedAccounts= new ArrayList<>();
        expectedAccounts.add("0x123");
        expectedAccounts.add("0x456");
        expectedAccounts.add("0x789");

        KeeperService _keeper= mock(KeeperService.class);
        Admin _web3j= mock(Admin.class);
        Credentials _credentials= mock(Credentials.class);

        Request<?, EthAccounts> _request= (Request<?, EthAccounts>) mock(Request.class);
        EthAccounts _response= mock(EthAccounts.class);

        when(_response.getAccounts()).thenReturn(expectedAccounts);
        when(_request.send()).thenReturn(_response);
        Mockito.doReturn(_request).when(_web3j).ethAccounts();
        when(_keeper.getWeb3()).thenReturn(_web3j);
        when(_keeper.getCredentials()).thenReturn(_credentials);

        AccountsManager fakeManager= AccountsManager.getInstance(_keeper, aquarius);

        List<Account> accounts= fakeManager.getAccounts();

        assertTrue(accounts.size() == expectedAccounts.size());
        assertTrue(accounts.get(0).address.startsWith("0x"));
    }

    @Test
    public void getAccountBalance() throws IOException, CipherException, Exception {
        String address= "0x123";
        BigInteger ethBalance= BigInteger.valueOf(3);
        BigInteger oceanBalance= BigInteger.valueOf(12);

        KeeperService _keeper= mock(KeeperService.class);
        Admin _web3j= mock(Admin.class);
        Credentials _credentials= mock(Credentials.class);
        OceanToken _token= mock(OceanToken.class);
        Request<?, EthGetBalance> _request= (Request<?, EthGetBalance>) mock(Request.class);
        EthGetBalance _response= mock(EthGetBalance.class);
        RemoteCall _call= mock(RemoteCall.class);

        when(_response.getBalance()).thenReturn(ethBalance, oceanBalance);
        when(_request.send()).thenReturn(_response);
        Mockito.doReturn(_request).when(_web3j).ethGetBalance(any(), any());

        when(_keeper.getWeb3()).thenReturn(_web3j);
        when(_keeper.getCredentials()).thenReturn(_credentials);

        when(_call.send()).thenReturn(oceanBalance);
        Mockito.doReturn(_call).when(_token).balanceOf(any());

        AccountsManager manager= AccountsManager.getInstance(_keeper, aquarius);
        manager.setTokenContract(_token);

        Balance balance= manager.getAccountBalance(address);

        assertEquals(ethBalance, balance.getEth());
        assertEquals(oceanBalance, balance.getDrops());
    }
}