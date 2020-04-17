package io.keyko.ocean.manager;

import io.keyko.common.helpers.CryptoHelper;
import io.keyko.common.web3.KeeperService;
import io.keyko.ocean.exceptions.EthereumException;
import io.keyko.ocean.external.AquariusService;
import io.keyko.ocean.keeper.contracts.EscrowReward;
import io.keyko.ocean.models.service.template.TemplateSEA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.Hash;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tuples.generated.Tuple6;

import java.math.BigInteger;
import java.util.List;

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
    public TransactionReceipt proposeTemplate(
            String templateAddress, List<String> conditionTypes, List<byte[]> actorTypeIds, String name) throws EthereumException {
        try {
            log.debug("TemplateStoreManager - Propose - Owner: " + templateStoreManager.owner().send());

            return templateStoreManager.proposeTemplate(templateAddress, conditionTypes, actorTypeIds, name).send();
        } catch (Exception ex) {
            String msg = "Error proposing template " + templateAddress;
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

    /**
     * Suggest an agreement template smart contract to include in the white listed agreement templates
     *
     * @param templateId Hex str the ethereum address of the deployed template (smart contract address)
     * @return TransactionReceipt tx receipt
     * @throws EthereumException EVM error
     */
    public TransactionReceipt proposeTemplate(
            byte[] templateId, List<String> conditionTypes, List<byte[]> actorTypeIds, String name) throws EthereumException {
        try {
            log.debug("TemplateStoreManager - Propose - Owner: " + templateStoreManager.owner().send());
            return templateStoreManager.proposeTemplate(templateId, conditionTypes, actorTypeIds, name).send();
        } catch (Exception ex) {
            String msg = "Error proposing template " + templateId;
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

    /**
     * Approve (whitelist) an already proposed template
     *
     * @param templateId byte[]
     * @return TransactionReceipt tx receipt
     * @throws EthereumException EVM error
     */
    public TransactionReceipt approveTemplate(byte[] templateId) throws EthereumException {
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
    public TransactionReceipt revokeTemplate(byte[] templateId) throws EthereumException {
        try {
            return templateStoreManager.revokeTemplate(templateId).send();
        } catch (Exception ex) {
            String msg = "Error proposing template " + templateId;
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

    /**
     * Register a template actor type
     *
     * @param actorType Actor type ("provider", "owner", "consumer", ..)
     * @return TransactionReceipt tx receipt
     * @throws EthereumException EVM error
     */
    public TransactionReceipt registerTemplateActorType(String actorType) throws EthereumException {
        try {
            return templateStoreManager.registerTemplateActorType(actorType).send();
        } catch (Exception ex) {
            String msg = "Error registering actor type " + actorType;
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

    /**
     * De-Register a template actor type
     *
     * @param actorType Actor type ("provider", "owner", "consumer", ..)
     * @return TransactionReceipt tx receipt
     * @throws EthereumException EVM error
     */
    public TransactionReceipt deregisterTemplateActorType(String actorType) throws EthereumException {
        try {
            return templateStoreManager.deregisterTemplateActorType(
                    CryptoHelper.keccak256(actorType)
            ).send();
        } catch (Exception ex) {
            String msg = "Error registering actor type " + actorType;
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

    /**
     * Returns true or false depending if the template was approved
     *
     * @param templateID Hex str the ethereum address of the deployed template (smart contract address)
     * @return boolean is approved
     * @throws EthereumException EVM error
     */
    public boolean isTemplateIdApproved(byte[] templateID) throws EthereumException {
        try {
            return templateStoreManager.isTemplateIdApproved(templateID).send();
        } catch (Exception ex) {
            String msg = "Error checking if template " + templateID + " is approved";
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
    public TemplateSEA getTemplate(byte[] templateId) throws EthereumException {
        try {
            final Tuple6<BigInteger, String, String, BigInteger, List<String>, List<byte[]>> tuple = templateStoreManager.getTemplate(templateId).send();
            return new TemplateSEA(tuple.getValue1(), tuple.getValue2(), tuple.getValue3(), tuple.getValue4());
        } catch (Exception ex) {
            String msg = "Error getting template " + templateId;
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);

        }
    }

    public byte[] getActorTypeId(String actorType)throws EthereumException {
        try{
            return templateStoreManager.getTemplateActorTypeId(actorType).send();
        }catch (Exception ex) {
            String msg = "Error getting id for actorType " + actorType;
            log.error(msg + ": " + ex.getMessage());
            throw new EthereumException(msg, ex);
        }
    }

}
