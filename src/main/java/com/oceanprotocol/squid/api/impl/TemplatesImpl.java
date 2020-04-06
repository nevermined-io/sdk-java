/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api.impl;

import com.oceanprotocol.squid.api.TemplatesAPI;
import com.oceanprotocol.squid.exceptions.EthereumException;
import com.oceanprotocol.squid.manager.TemplatesManager;
import com.oceanprotocol.squid.models.service.template.TemplateSEA;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;


public class TemplatesImpl implements TemplatesAPI {

    private TemplatesManager templatesManager;

    /**
     * Constructor
     *
     * @param templatesManager the templatesManager
     */
    public TemplatesImpl(TemplatesManager templatesManager) {

        this.templatesManager = templatesManager;
    }

    @Override
    public TransactionReceipt propose(String templateAddress) throws EthereumException {
        return templatesManager.proposeTemplate(templateAddress);
    }

    @Override
    public TransactionReceipt approve(String templateAddress) throws EthereumException {
        return templatesManager.approveTemplate(templateAddress);
    }

    @Override
    public TransactionReceipt revoke(String templateAddress) throws EthereumException {
        return templatesManager.revokeTemplate(templateAddress);
    }

    @Override
    public boolean isApproved(String templateAddress) throws EthereumException {
        return templatesManager.isTemplateApproved(templateAddress);
    }

    @Override
    public BigInteger getListSize() throws EthereumException {
        return templatesManager.getTemplateListSize();
    }

    @Override
    public TemplateSEA getTemplate(String templateAddress) throws EthereumException {
        return templatesManager.getTemplate(templateAddress);
    }

}
