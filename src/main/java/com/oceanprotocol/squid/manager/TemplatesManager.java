/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.manager;

import com.oceanprotocol.common.web3.KeeperService;
import com.oceanprotocol.squid.exceptions.EthereumException;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.squid.models.service.template.TemplateSEA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple4;

import java.math.BigInteger;

/**
 * Controller class to manage the SEA Template functions
 */
public class TemplatesManager extends BaseManager {

    private static final Logger log = LogManager.getLogger(TemplatesManager.class);


    private TemplatesManager(KeeperService keeperService, AquariusService aquariusService) {
        super(keeperService, aquariusService);
    }

    /**
     * Given the KeeperService and AquariusService, returns a new instance of AccountsManager
     * using them as attributes
     *
     * @param keeperService   Keeper Dto
     * @param aquariusService Provider Dto
     * @return AccountsManager AccountsManager instance
     */
    public static TemplatesManager getInstance(KeeperService keeperService, AquariusService aquariusService) {
        return new TemplatesManager(keeperService, aquariusService);
    }


    /**
     * Suggest an agreement template smart contract to include in the white listed agreement templates
     *
     * @param templateAddress Hex str the ethereum address of the deployed template (smart contract address)
     * @return TransactionReceipt tx receipt
     * @throws EthereumException EVM error
     */
    public TransactionReceipt proposeTemplate(String templateAddress) throws EthereumException {
        try {
            log.debug("TemplateStoreManager - Propose - Owner: " + templateStoreManager.owner().send());
            return templateStoreManager.proposeTemplate(templateAddress).send();
        } catch (Exception ex) {
            String msg = "Error proposing template " + templateAddress;
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

    /**
     * Approve (whitelist) an already proposed template
     *
     * @param templateAddress Hex str the ethereum address of the deployed template (smart contract address)
     * @return TransactionReceipt tx receipt
     * @throws EthereumException EVM error
     */
    public TransactionReceipt approveTemplate(String templateAddress) throws EthereumException {
        try {
            log.debug("TemplateStoreManager - Approve - Owner: " + templateStoreManager.owner().send());
            return templateStoreManager.approveTemplate(templateAddress).send();
        } catch (Exception ex) {
            String msg = "Error approving template " + templateAddress;
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

    /**
     * Cancel the propsed/approved template or essentially de-whitelist the template.
     *
     * @param templateAddress Hex str the ethereum address of the deployed template (smart contract address)
     * @return TransactionReceipt tx receipt
     * @throws EthereumException EVM error
     */
    public TransactionReceipt revokeTemplate(String templateAddress) throws EthereumException {
        try {
            return templateStoreManager.revokeTemplate(templateAddress).send();
        } catch (Exception ex) {
            String msg = "Error proposing template " + templateAddress;
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

    /**
     * Returns true or false depending if the template was approved
     *
     * @param templateAddress Hex str the ethereum address of the deployed template (smart contract address)
     * @return boolean is approved
     * @throws EthereumException EVM error
     */
    public boolean isTemplateApproved(String templateAddress) throws EthereumException {
        try {
            return templateStoreManager.isTemplateApproved(templateAddress).send();
        } catch (Exception ex) {
            String msg = "Error checking if template " + templateAddress + " is approved";
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

    /**
     * Returns the number of templates registered
     *
     * @return boolean is approved
     * @throws EthereumException EVM error
     */
    public BigInteger getTemplateListSize() throws EthereumException {
        try {
            return templateStoreManager.getTemplateListSize().send();
        } catch (Exception ex) {
            String msg = "Error getting the number of existing templates";
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

    /**
     * Getting template using an address given
     *
     * @param templateAddress template address
     * @return TemplateSEA instance
     * @throws EthereumException EVM error
     */
    public TemplateSEA getTemplate(String templateAddress) throws EthereumException {
        try {
            Tuple4<BigInteger, String, String, BigInteger> tuple4 = templateStoreManager.getTemplate(templateAddress).send();
            return new TemplateSEA(tuple4.getValue1(), tuple4.getValue2(), tuple4.getValue3(), tuple4.getValue4());
        } catch (Exception ex) {
            String msg = "Error getting template " + templateAddress;
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);

        }
    }

}
