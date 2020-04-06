/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.manager;

import com.oceanprotocol.common.web3.KeeperService;
import com.oceanprotocol.squid.exceptions.EthereumException;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.Balance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class to manage the token functions
 */
public class AccountsManager extends BaseManager {

    private static final Logger log = LogManager.getLogger(AccountsManager.class);

    private AccountsManager(KeeperService keeperService, AquariusService aquariusService) {
        super(keeperService, aquariusService);
    }

    /**
     * Given the KeeperService and AquariusService, returns a new instance of AccountsManager
     * using them as attributes
     *
     * @param keeperService   Keeper Dto
     * @param aquariusService Provider Dto
     * @return AccountsManager
     */
    public static AccountsManager getInstance(KeeperService keeperService, AquariusService aquariusService) {
        return new AccountsManager(keeperService, aquariusService);
    }


    /**
     * Returns the list of ethereum accounts registered in the Keeper node
     * If getBalance is true, get the ethereum and ocean balance of each account
     *
     * @return List of accounts
     * @throws EthereumException if the EVM throws an exception
     */
    public List<Account> getAccounts() throws EthereumException {

        try {

            EthAccounts ethAccounts = getKeeperService().getWeb3().ethAccounts().send();

            List<Account> accounts = new ArrayList<>();
            for (String account : ethAccounts.getAccounts()) {
                accounts.add(new Account(account));
            }

            return accounts;

        } catch (IOException e) {
            log.error("Error getting etherum accounts from keeper" + ": " + e.getMessage());
            throw new EthereumException("Error getting etherum accounts from keeper", e);

        }
    }

    /**
     * Given an account returns a Balance object with the Ethereum and Ocean balance
     *
     * @param accountAddress account
     * @return Balance
     * @throws EthereumException if the EVM throws an exception
     */
    public Balance getAccountBalance(String accountAddress) throws EthereumException {
        return new Balance(
                getEthAccountBalance(accountAddress),
                getOceanAccountBalance(accountAddress)
        );
    }

    /**
     * Given an account returns the Ethereum balance
     *
     * @param accountAddress account
     * @return ethereum balance
     * @throws EthereumException if the EVM throws an exception
     */
    public BigInteger getEthAccountBalance(String accountAddress) throws EthereumException {
        try {
            return getKeeperService()
                    .getWeb3()
                    .ethGetBalance(accountAddress, DefaultBlockParameterName.LATEST).send()
                    .getBalance();
        } catch (Exception ex) {
            String msg = "Unable to get account(" + accountAddress + ") Ocean balance";
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

    /**
     * Given an account returns the Ocean balance
     * Contract: OceanToken
     * Method: balanceOf
     *
     * @param accountAddress account
     * @return ocean balance
     * @throws EthereumException if the EVM throws an exception
     */
    public BigInteger getOceanAccountBalance(String accountAddress) throws EthereumException {
        try {
            return tokenContract.balanceOf(accountAddress).send();
        } catch (Exception ex) {
            String msg = "Unable to get account(" + accountAddress + ") Ocean balance";
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }


    /**
     * Requests Ocean Tokens from the Dispenser Smart Contract
     * Contract: OceanMarket
     * Method: requestTokens
     *
     * @param amount amount of tokens requests
     * @return TransactionReceipt
     * @throws EthereumException if the EVM throws an exception
     */
    public TransactionReceipt requestTokens(BigInteger amount) throws EthereumException {
        try {
            return dispenser.requestTokens(amount).send();
        } catch (Exception ex) {
            String msg = "Unable request tokens " + amount.toString();
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

    /**
     * Transfer tokens from one account to the receiver address
     *
     * @param receiverAccount Address of the transfer receiver
     * @param amount          Amount of tokens to transfer
     * @return TransactionReceipt tx receipt
     * @throws EthereumException if the EVM throws an exception
     */
    public TransactionReceipt transfer(String receiverAccount, BigInteger amount) throws EthereumException {
        try {
            return tokenContract.transfer(receiverAccount, amount).send();
        } catch (Exception ex) {
            String msg = "Unable transfer tokens " + amount.toString();
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

}
