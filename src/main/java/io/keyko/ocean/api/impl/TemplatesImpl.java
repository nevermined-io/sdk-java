package io.keyko.ocean.api.impl;

import io.keyko.ocean.api.TemplatesAPI;
import io.keyko.ocean.exceptions.EthereumException;
import io.keyko.ocean.manager.TemplatesManager;
import io.keyko.ocean.models.service.template.TemplateSEA;
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
    public TransactionReceipt propose(String templateId, List<String> conditionTypes, List<byte[]> actorTypeIds, String name)
            throws EthereumException {
        return templatesManager.proposeTemplate(templateId, conditionTypes, actorTypeIds, name);
    }

    @Override
    public TransactionReceipt approve(String templateId) throws EthereumException {
        return templatesManager.approveTemplate(templateId.getBytes());
    }

    @Override
    public TransactionReceipt revoke(String templateId) throws EthereumException {
        return templatesManager.revokeTemplate(templateId.getBytes());
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
    public TemplateSEA getTemplate(String templateId) throws EthereumException {
        return templatesManager.getTemplate(templateId.getBytes());
    }

}
