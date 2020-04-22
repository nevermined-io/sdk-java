package io.keyko.nevermind.api.impl;

import io.keyko.common.helpers.CryptoHelper;
import io.keyko.nevermind.api.TemplatesAPI;
import io.keyko.nevermind.exceptions.EthereumException;
import io.keyko.nevermind.manager.TemplatesManager;
import io.keyko.nevermind.models.service.template.TemplateSEA;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;


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
    public TransactionReceipt propose(String templateId)
            throws EthereumException {
        return templatesManager.proposeTemplate(templateId);
    }

    @Override
    public TransactionReceipt approve(String templateId) throws EthereumException {
        return templatesManager.approveTemplate(templateId);
    }

    @Override
    public TransactionReceipt revoke(String templateId) throws EthereumException {
        return templatesManager.revokeTemplate(templateId);
    }

    @Override
    public BigInteger getListSize() throws EthereumException {
        return templatesManager.getTemplateListSize();
    }

    @Override
    public TemplateSEA getTemplate(String templateId) throws EthereumException {
        return templatesManager.getTemplate(templateId);
    }

    @Override
    public boolean isApproved(String templateAddress) throws EthereumException {
        return templatesManager.isTemplateApproved(templateAddress);
    }

}
