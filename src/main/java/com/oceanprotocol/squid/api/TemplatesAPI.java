/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api;


import com.oceanprotocol.squid.exceptions.EthereumException;
import com.oceanprotocol.squid.models.service.template.TemplateSEA;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

/**
 * Exposes the Public API related with the Service Agreements Templates Management
 */
public interface TemplatesAPI {

    /**
     * Suggest an agreement template smart contract to include in the white listed agreement templates
     *
     * @param templateAddress Hex str the ethereum address of the deployed template (smart contract address)
     * @return TransactionReceipt tx receipt
     * @throws EthereumException EthereumException
     */
    TransactionReceipt propose(String templateAddress) throws EthereumException;

    /**
     * Approve (whitelist) an already proposed template. Once a template is approved
     * it can be used for creating agreements in Ocean Protocol keeper network
     *
     * @param templateAddress Hex str the ethereum address of the deployed template (smart contract address)
     * @return TransactionReceipt tx receipt
     * @throws  EthereumException EthereumException
     */
    TransactionReceipt approve(String templateAddress) throws EthereumException;

    /**
     * Cancel the propsed/approved template or essentially de-whitelist the template.
     * This prevents the creation of any further agreements that are based on this template.
     *
     * @param templateAddress Hex str the ethereum address of the deployed template (smart contract address)
     * @return TransactionReceipt tx receipt
     * @throws EthereumException EthereumException
     */
    TransactionReceipt revoke(String templateAddress) throws EthereumException;

    /**
     * Checks if an agreement template is approved
     *
     * @param templateAddress Hex str the ethereum address of the deployed template (smart contract address)
     * @return boolean is the template approved?
     * @throws EthereumException EthereumException
     */
    boolean isApproved(String templateAddress) throws EthereumException;

    /**
     * Get the number of agreement templates registered
     *
     * @return BigInteger number of templates
     * @throws EthereumException EthereumException
     */
    BigInteger getListSize() throws EthereumException;

    /**
     * Return the attributes associated to a registered Template in the Smart Contracts
     *
     * @param templateAddress Hex str the ethereum address of the deployed template (smart contract address)
     * @return TemplateSEA
     * @throws EthereumException EthereumException
     */
    TemplateSEA getTemplate(String templateAddress) throws EthereumException;
}
