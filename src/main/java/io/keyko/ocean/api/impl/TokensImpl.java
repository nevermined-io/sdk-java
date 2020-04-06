/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package io.keyko.ocean.api.impl;

import io.keyko.ocean.api.TokensAPI;
import io.keyko.ocean.exceptions.EthereumException;
import io.keyko.ocean.manager.AccountsManager;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

public class TokensImpl implements TokensAPI {

    private AccountsManager accountsManager;

    /**
     * Constructor
     *
     * @param accountsManager the accountsManager
     */
    public TokensImpl(AccountsManager accountsManager) {

        this.accountsManager = accountsManager;
    }

    @Override
    public TransactionReceipt request(BigInteger amount) throws EthereumException {
        return accountsManager.requestTokens(amount);
    }

    @Override
    public TransactionReceipt transfer(String receiverAccount, BigInteger amount) throws EthereumException {
        return accountsManager.transfer(receiverAccount, amount);
    }
}
