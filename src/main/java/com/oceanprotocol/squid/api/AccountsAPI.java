/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api;

import com.oceanprotocol.squid.exceptions.EthereumException;
import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.Balance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;

/**
 * Exposes the Public API related with the management of Accounts
 */
public interface AccountsAPI {

    /**
     * Returns a list of the accounts registered in Keeper
     *
     * @return a List of all Account registered in Keeper
     * @throws EthereumException EthereumException
     */
    public List<Account> list() throws EthereumException;

    /**
     * Returns the Balance of an account
     *
     * @param account the account we want to get the balance
     * @return the Balance of the account
     * @throws EthereumException EthereumException
     */
    public Balance balance(Account account) throws EthereumException;

    /**
     * Requests Ocean Tokens from the OceanMarket Smart Contract
     *
     * @param amount the amount of tokens
     * @return a TransactionReceipt from the transaction sent to the smart contract
     * @throws EthereumException EthereumException
     */
    public TransactionReceipt requestTokens(BigInteger amount) throws EthereumException;

}
