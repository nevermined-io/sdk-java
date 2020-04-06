/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api;


import com.oceanprotocol.squid.exceptions.EthereumException;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

/**
 * Exposes the Public API related with the request and transfer of Ocean Tokens
 */
public interface TokensAPI {

    /**
     * Request a number of Ocean Tokens
     *
     * @param amount Number of tokens requested
     * @return TransactionReceipt tx receipt
     * @throws EthereumException EVM error
     */
    TransactionReceipt request(BigInteger amount) throws EthereumException;

    /**
     * Transfer tokens from one account to the receiver address
     *
     * @param receiverAccount Address of the transfer receiver
     * @param amount          Amount of tokens to transfer
     * @return boolean indicating success/failure of the transfer
     * @throws EthereumException EVM error
     */
    TransactionReceipt transfer(String receiverAccount, BigInteger amount) throws EthereumException;

}
