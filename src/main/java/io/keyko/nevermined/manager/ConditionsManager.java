package io.keyko.nevermined.manager;

import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.service.Agreement;
import io.keyko.nevermined.models.service.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tuples.generated.Tuple2;

import java.math.BigInteger;

public class ConditionsManager extends BaseManager {

    private static final Logger log = LogManager.getLogger(ConditionsManager.class);

    public ConditionsManager(KeeperService keeperService, MetadataApiService metadataApiService) {
        super(keeperService, metadataApiService);
    }

    /**
     * Given the KeeperService and MetadataApiService, returns a new instance of ConditionsManager
     * using them as attributes
     *
     * @param keeperService   Keeper Dto
     * @param metadataApiService Provider Dto
     * @return ConditionsManager
     */
    public static ConditionsManager getInstance(KeeperService keeperService, MetadataApiService metadataApiService) {
        return new ConditionsManager(keeperService, metadataApiService);
    }

    /**
     * Lock reward for a service agreement.
     *
     * @param agreementId the agreement id.
     * @param amount      the amount to be locked.
     * @return a flag true if was executed successfully.
     * @throws Exception exception
     */
    public Boolean lockReward(String agreementId, BigInteger amount) throws Exception {
        try {
            tokenApprove(tokenContract, lockRewardCondition.getContractAddress(), amount.toString());
            TransactionReceipt txReceipt = lockRewardCondition.fulfill(EncodingHelper.hexStringToBytes(agreementId),
                    Keys.toChecksumAddress(escrowReward.getContractAddress()),
                    amount).send();
            return txReceipt.isStatusOK();
        } catch (TransactionException e) {
            log.error("Error looking reward for the agreement" + agreementId + e.getMessage());
            return false;
        }
    }

    /**
     * Grant access to an address to consume a did.
     *
     * @param agreementId    the agreement id.
     * @param did            the did.
     * @param granteeAddress an eth address.
     * @return a flag true if was executed successfully.
     * @throws Exception exception
     */
    public Boolean grantAccess(String agreementId, DID did, String granteeAddress) throws Exception {

        try {
            TransactionReceipt txReceipt = accessSecretStoreCondition.fulfill(EncodingHelper.hexStringToBytes(agreementId),
                    EncodingHelper.hexStringToBytes("0x" + did.getHash()),
                    granteeAddress).send();
            return txReceipt.isStatusOK();
        } catch (TransactionException e) {
            log.error("Error granting access to address" + granteeAddress + "to did" + did + e.getMessage());
            return false;
        }
    }

    /**
     * Grant compute to an address.
     *
     * @param agreementId    the agreement id.
     * @param did            the did.
     * @param granteeAddress an eth address.
     * @return a flag true if was executed successfully.
     * @throws Exception exception
     */
    public Boolean grantCompute(String agreementId, DID did, String granteeAddress) throws Exception {

        try {
            TransactionReceipt txReceipt = computeExecutionCondition.fulfill(EncodingHelper.hexStringToBytes(agreementId),
                    EncodingHelper.hexStringToBytes("0x" + did.getHash()),
                    granteeAddress).send();
            return txReceipt.isStatusOK();
        } catch (TransactionException e) {
            log.error("Error granting compute to address" + granteeAddress + "to did" + did + e.getMessage());
            return false;
        }
    }

    /**
     * Release reward to the address after the access was granted.
     *
     * @param agreementId the agreement id.
     * @param amount      the price.
     * @return a flag true if was executed successfully.
     * @throws Exception exception
     */
    public Boolean releaseReward(String agreementId, BigInteger amount) throws Exception {

        Agreement agreement = new Agreement(agreementStoreManager.getAgreement(EncodingHelper.hexStringToBytes(agreementId)).send());

        DDO ddo = resolveDID(agreement.did);
        Service service  = ddo.getServiceByTemplate(agreement.templateId);

        Tuple2<String, String> agreementData = null;
        TransactionReceipt txReceipt;

        if (service.type.equals(Service.ServiceTypes.ACCESS.toString())) {
            agreementData = escrowAccessSecretStoreTemplate.getAgreementData(EncodingHelper.hexStringToBytes(agreementId)).send();
        } else if (service.type.equals(Service.ServiceTypes.COMPUTE.toString())) {
            agreementData = escrowComputeExecutionTemplate.getAgreementData(EncodingHelper.hexStringToBytes(agreementId)).send();
        }

        try {
            txReceipt = escrowReward.fulfill(EncodingHelper.hexStringToBytes(agreementId),
                    amount,
                    agreementData.component2(),
                    agreementData.component1(),
                    agreement.conditions.get(1),
                    agreement.conditions.get(0)).send();

            return txReceipt.isStatusOK();
        } catch (TransactionException e) {
            log.error("Error releasing reward for the agreement" + agreementId + e.getMessage());
            return false;
        }
    }

    /**
     * Refund the price in case that some of the step was wrong.
     *
     * @param agreementId the agreement id.
     * @param amount      the price.
     * @return a flag true if was executed successfully.
     * @throws Exception exception
     */
    public Boolean refundReward(String agreementId, BigInteger amount) throws Exception {
        return releaseReward(agreementId, amount);
    }

}
