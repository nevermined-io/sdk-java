/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.manager;

import com.oceanprotocol.keeper.contracts.Dispenser;
import com.oceanprotocol.keeper.contracts.OceanToken;
import com.oceanprotocol.squid.exceptions.EthereumException;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.common.web3.KeeperService;
import com.oceanprotocol.common.web3.parity.JsonRpcSquidAdmin;
import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.Balance;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccountsManagerIT {

    private static final Logger log = LogManager.getLogger(AccountsManagerIT.class);

    private static AccountsManager manager;
    private static AccountsManager managerError;
    private static KeeperService keeper;
    private static KeeperService keeperError;
    private static AquariusService aquarius;

    private static OceanToken oceanToken;
    private static Dispenser dispenser;

    private static final Config config = ConfigFactory.load();
    private static String TEST_ADDRESS;

    @BeforeClass
    public static void setUp() throws Exception {
        log.debug("Setting Up DTO's");

        keeper= ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity);
        aquarius= ManagerHelper.getAquarius(config);
        manager= AccountsManager.getInstance(keeper, aquarius);

        // Loading Smart Contracts required
        oceanToken= ManagerHelper.loadOceanTokenContract(keeper, config.getString("contract.OceanToken.address"));
        dispenser= ManagerHelper.loadDispenserContract(keeper, config.getString("contract.Dispenser.address"));
        manager.setTokenContract(oceanToken);
        manager.setDispenserContract(dispenser);

        TEST_ADDRESS= config.getString("account.parity.address");

        Config badConfig= config.withValue(
                "keeper.url", ConfigValueFactory.fromAnyRef("http://fdasdfsa.dasx:8545"));

        keeperError= ManagerHelper.getKeeper(badConfig, ManagerHelper.VmClient.parity);
        managerError= AccountsManager.getInstance(keeperError, aquarius);
        managerError.setTokenContract(
                ManagerHelper.loadOceanTokenContract(keeperError, config.getString("contract.OceanToken.address"))
        );
        managerError.setDispenserContract(
                ManagerHelper.loadDispenserContract(keeperError, config.getString("contract.Dispenser.address"))
        );
    }

    @Test
    public void getInstance() {
        // Checking if web3j driver included in KeeperService implements the Web3j interface
        assertTrue(
                manager.getKeeperService().getWeb3().getClass().isAssignableFrom(JsonRpcSquidAdmin.class));
        assertTrue(
                manager.getAquariusService().getClass().isAssignableFrom(AquariusService.class));
    }

    @Test
    public void getAccounts() throws IOException, EthereumException {
        List<Account> accounts= manager.getAccounts();
        assertTrue(accounts.size()>0);
    }

    @Test
    public void getAccountsBalance() throws EthereumException {
        manager.requestTokens(BigInteger.ONE);
        log.debug("OceanToken Address: " + manager.tokenContract.getContractAddress());

        log.debug("Requesting " + BigInteger.ONE + " ocean tokens for " + TEST_ADDRESS);

        Balance balance= manager.getAccountBalance(TEST_ADDRESS);

        log.debug("OCEAN Balance is " + balance.getDrops().toString());
        log.debug("ETH balance is " + balance.getEth().toString());

        assertEquals(1, balance.getEth().compareTo(BigInteger.ZERO));
        assertEquals(1, balance.getDrops().compareTo(BigInteger.ZERO));

    }

    @Test(expected = EthereumException.class)
    public void getAccountsException() throws IOException, EthereumException, CipherException {

        List<Account> accounts= managerError.getAccounts();
        assertTrue(accounts.size()>0);
    }

    @Test(expected = EthereumException.class)
    public void getAccountsBalanceError() throws EthereumException, CipherException, IOException {
        managerError.requestTokens(BigInteger.valueOf(100));

        managerError.getAccountBalance(TEST_ADDRESS);
    }

}