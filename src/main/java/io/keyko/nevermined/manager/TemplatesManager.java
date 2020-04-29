package io.keyko.nevermined.manager;

import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.exceptions.EthereumException;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.models.service.template.TemplateSEA;
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


    private TemplatesManager(KeeperService keeperService, MetadataApiService metadataApiService) {
        super(keeperService, metadataApiService);
    }

    /**
     * Given the KeeperService and MetadataApiService, returns a new instance of AccountsManager
     * using them as attributes
     *
     * @param keeperService   Keeper Dto
     * @param metadataApiService Provider Dto
     * @return AccountsManager AccountsManager instance
     */
    public static TemplatesManager getInstance(KeeperService keeperService, MetadataApiService metadataApiService) {
        return new TemplatesManager(keeperService, metadataApiService);
    }


    /**
     * Suggest an agreement template smart contract to include in the white listed agreement templates
     *
     * @param templateAddress Hex str the ethereum address of the deployed template (smart contract address)
     * @return TransactionReceipt tx receipt
     * @throws EthereumException EVM error
     */
    public TransactionReceipt proposeTemplate(
            String templateAddress) throws EthereumException {
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
     * @param templateId String
     * @return TransactionReceipt tx receipt
     * @throws EthereumException EVM error
     */
    public TransactionReceipt approveTemplate(String templateId) throws EthereumException {
        try {
            log.debug("TemplateStoreManager - Approve - Owner: " + templateStoreManager.owner().send());
            return templateStoreManager.approveTemplate(templateId).send();
        } catch (Exception ex) {
            String msg = "Error approving template " + templateId;
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

    /**
     * Cancel the propsed/approved template or essentially de-whitelist the template.
     *
     * @param templateId Hex str the ethereum address of the deployed template (smart contract address)
     * @return TransactionReceipt tx receipt
     * @throws EthereumException EVM error
     */
    public TransactionReceipt revokeTemplate(String templateId) throws EthereumException {
        try {
            return templateStoreManager.revokeTemplate(templateId).send();
        } catch (Exception ex) {
            String msg = "Error proposing template " + templateId;
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
     * @param templateId template address
     * @return TemplateSEA instance
     * @throws EthereumException EVM error
     */
    public TemplateSEA getTemplate(String templateId) throws EthereumException {
        try {
//            final Tuple6<BigInteger, String, String, BigInteger, List<String>, List<byte[]>> tuple = 
            final Tuple4<BigInteger, String, String, BigInteger> tuple = templateStoreManager.getTemplate(templateId).send();
            return new TemplateSEA(tuple.getValue1(), tuple.getValue2(), tuple.getValue3(), tuple.getValue4());
        } catch (Exception ex) {
            String msg = "Error getting template " + templateId;
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);

        }
    }

}
