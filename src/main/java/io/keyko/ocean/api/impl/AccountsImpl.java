/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package io.keyko.ocean.api.impl;

import io.keyko.ocean.api.AccountsAPI;
import io.keyko.ocean.exceptions.EthereumException;
import io.keyko.ocean.manager.AccountsManager;
import io.keyko.ocean.models.Account;
import io.keyko.ocean.models.Balance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;

/**
 * Implementation of AccountsAPI
 */
public class AccountsImpl implements AccountsAPI {

    private AccountsManager accountsManager;


    /**
     * Constructor
     *
     * @param accountsManager the accountsManager
     */
    public AccountsImpl(AccountsManager accountsManager) {

        this.accountsManager = accountsManager;
    }

    @Override
    public List<Account> list() throws EthereumException {

        return accountsManager.getAccounts();
    }

    @Override
    public Balance balance(Account account) throws EthereumException {

        return accountsManager.getAccountBalance(account.address);
    }

    @Override
    public TransactionReceipt requestTokens(BigInteger amount) throws EthereumException {

        return accountsManager.requestTokens(amount);
    }
}
