package io.keyko.nevermined.manager;

import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.api.helper.AccountsHelper;
import io.keyko.nevermined.api.helper.InitializationHelper;
import io.keyko.nevermined.contracts.ERC20Upgradeable;
import io.keyko.nevermined.exceptions.ServiceAgreementException;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public String getTokenAddress(String tokenAddress)  {
        if (null == tokenAddress)
            return tokenContract.getContractAddress();
        else if (tokenAddress.equals("0x") || tokenAddress.equals("0x0"))
            return AccountsHelper.ZERO_ADDRESS;
        else
            return tokenAddress;
    }

    /**
     * Lock the payment for a service agreement
     *
     * @param agreementId the agreement id
     * @return true if was executed successfully.
     * @throws Exception exception
     */
    public Boolean lockPayment(String agreementId) throws Exception {
        return lockPayment(agreementId, -1);
    }

    /**
     * Lock the payment for a service agreement
     *
     * @param agreementId the agreement id
     * @param serviceIndex index of the service in the DDO
     * @return true if was executed successfully.
     * @throws Exception exception
     */
    public Boolean lockPayment(String agreementId, int serviceIndex) throws Exception {
        try {
            final Agreement agreement = new Agreement(agreementStoreManager.getAgreement(EncodingHelper.hexStringToBytes(agreementId)).send());
            final DDO ddo = resolveDID(agreement.did);
            Service service;
            if (serviceIndex >= 0)
                service = ddo.getService(serviceIndex);
            else
                service = ddo.getServiceByTemplate(agreement.templateId);

            final List<BigInteger> _amounts = service.fetchAmounts();
            final List<String> _receivers = service.fetchReceivers();
            final String rewardAddress = Keys.toChecksumAddress(escrowCondition.getContractAddress());
            final String _DDOtokenAddress = service.fetchConditionValue("_tokenAddress");
            final String contractTokenAddress = getTokenAddress(_DDOtokenAddress);

            final BigInteger amount = _amounts.stream()
                    .reduce(BigInteger.ZERO, BigInteger::add);

            TransactionReceipt txReceipt;
            if (!contractTokenAddress.equals(AccountsHelper.ZERO_ADDRESS)) {
                final ERC20Upgradeable erc20Contract = InitializationHelper.loadERC20Contract(getKeeperService(), contractTokenAddress);
                erc20Contract.approve(lockCondition.getContractAddress(), amount);

                txReceipt = lockCondition.fulfill(
                        EncodingHelper.hexStringToBytes(agreementId),
                        EncodingHelper.hexStringToBytes(agreement.did.getHash()),
                        rewardAddress,
                        contractTokenAddress,
                        _amounts,
                        _receivers
                ).send();

            }   else    { // If we need to use ETH, we send the amount to pay in Wei
                txReceipt = lockCondition.fulfill(
                        EncodingHelper.hexStringToBytes(agreementId),
                        EncodingHelper.hexStringToBytes(agreement.did.getHash()),
                        rewardAddress,
                        contractTokenAddress,
                        _amounts,
                        _receivers,
                        amount
                ).send();
            }

            return txReceipt.isStatusOK();

        } catch (TransactionException e) {
            log.error("Error looking reward for the agreement" + agreementId + e.getMessage());
            return false;
        }
    }

    /**
     * Grant access to an address to download a did.
     *
     * @param agreementId    the agreement id.
     * @param did            the did.
     * @param granteeAddress an eth address.
     * @return a flag true if was executed successfully.
     * @throws Exception exception
     */
    public Boolean grantAccess(String agreementId, DID did, String granteeAddress) throws Exception {

        try {
            TransactionReceipt txReceipt = accessCondition.fulfill(EncodingHelper.hexStringToBytes(agreementId),
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
     * Release reward to the asset providers and/or publiser after the access was granted.
     *
     * @param agreementId the agreement id.
     * @return true if was executed successfully.
     * @throws Exception exception
     */
    public Boolean releaseReward(String agreementId) throws Exception {
        return releaseReward(agreementId, -1);
    }

    /**
     * Release reward to the asset providers and/or publiser after the access was granted.
     *
     * @param agreementId the agreement id.
     * @param serviceIndex index of the service in the DDO
     * @return true if was executed successfully.
     * @throws Exception exception
     */
    public Boolean releaseReward(String agreementId, int serviceIndex) throws Exception {

        Agreement agreement = new Agreement(agreementStoreManager.getAgreement(EncodingHelper.hexStringToBytes(agreementId)).send());
        DDO ddo = resolveDID(agreement.did);
        Service service;
        if (serviceIndex >= 0)
            service = ddo.getService(serviceIndex);
        else
            service = ddo.getServiceByTemplate(agreement.templateId);

        TransactionReceipt txReceipt;

        try {
            final List<BigInteger> _amounts = service.fetchAmounts();
            final List<String> _receivers = service.fetchReceivers();
            final String _DDOtokenAddress = service.fetchConditionValue("_tokenAddress");

            txReceipt = escrowCondition.fulfill(
                    EncodingHelper.hexStringToBytes(agreementId),
                    EncodingHelper.hexStringToBytes(agreement.did.getHash()),
                    _amounts,
                    _receivers,
                    escrowCondition.getContractAddress(),
                    getTokenAddress(_DDOtokenAddress),
                    agreement.conditions.get(1),
                    agreement.conditions.get(0)
            ).send();

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
     * @return a flag true if was executed successfully.
     * @throws Exception exception
     */
    public Boolean refundReward(String agreementId) throws Exception {
        return releaseReward(agreementId);
    }

    public List<String> generateAgreementConditionIds(Service.ServiceTypes serviceType, String agreementId, String consumerAddress, DDO ddo, int serviceIndex) throws ServiceAgreementException {

        try {

            List<String> conditionIds = new ArrayList<>();
            Service service;
            if (serviceIndex >= 0)  {
                service = ddo.getService(serviceIndex);
            }   else {
                service = ddo.getServiceByType(serviceType);
            }

            if (serviceType.equals(Service.ServiceTypes.NFT_ACCESS))    {
                final BigInteger numberNfts = service.fetchNumberNFTs();
                // TODO: Generate NFT Holder Condition ID

                // TODO: Generate NFT Access Condition ID
                return conditionIds;
            }

            final List<BigInteger> amounts = service.fetchAmounts();
            final List<String> receivers = service.fetchReceivers();
            final String rewardAddress = Keys.toChecksumAddress(escrowCondition.getContractAddress());
            final String tokenAddress = getTokenAddress(service.fetchConditionValue("_tokenAddress"));
            String lockConditionId = generateLockPaymentConditionId(agreementId, ddo.getDID(), rewardAddress, tokenAddress, amounts, receivers);

            String accessConditionId;
            if (serviceType.equals(Service.ServiceTypes.ACCESS))    {
                accessConditionId = generateAccessConditionId(agreementId, ddo.getDID(), consumerAddress);
            } else if (serviceType.equals(Service.ServiceTypes.COMPUTE))    {
                accessConditionId = generateExecComputeConditionId(agreementId, ddo.getDID(), consumerAddress);
            } else if (serviceType.equals(Service.ServiceTypes.DID_SALES))    {
                accessConditionId = null;
            } else if (serviceType.equals(Service.ServiceTypes.NFT_SALES))    {
                final BigInteger numberNfts = service.fetchNumberNFTs();
                accessConditionId = null;
            }   else {
                throw new ServiceAgreementException(agreementId, "Error generating the condition ids, the service type is not valid");
            }

            String escrowConditionId = generateEscrowPaymentConditionId(agreementId, ddo.getDID(), amounts, receivers, rewardAddress, tokenAddress, lockConditionId, accessConditionId);
            return Arrays.asList(accessConditionId, lockConditionId, escrowConditionId);

        } catch (Exception e) {
            throw new ServiceAgreementException(agreementId, "Unable to encode values: " + e.getMessage());
        }

    }


    /**
     * Generates the Lock Payment condition identifier using the condition contract to be used in the service agreements
     * @param serviceAgreementId the service agreement id
     * @param did the asset decentralized identifier
     * @param rewardAddress the address where payment is locked,
     * @param tokenAddress token address used for the payment
     * @param amounts payment amounts
     * @param receivers of the payment
     * @return String with the condition Id
     */
    public String generateLockPaymentConditionId(String serviceAgreementId, DID did, String rewardAddress, String tokenAddress, List<BigInteger> amounts, List<String> receivers) throws Exception {
        final byte[] paramsHash = lockCondition.hashValues(
                EncodingHelper.hexStringToBytes(did.getHash()),
                Keys.toChecksumAddress(rewardAddress),
                Keys.toChecksumAddress(tokenAddress),
                amounts,
                receivers
        ).send();
        return EncodingHelper.toHexString(
                lockCondition.generateId(
                        EncodingHelper.hexStringToBytes(serviceAgreementId),
                        paramsHash
                ).send()
        );
    }

    /**
     * Generates the Compute condition identifier using the condition contract to be used in the service agreements
     * @param serviceAgreementId the service agreement id
     * @param did the asset decentralized identifier
     * @param grantee the address to get access to the asset
     * @return String with the condition Id
     */
    public String generateExecComputeConditionId(String serviceAgreementId, DID did, String grantee) throws Exception {
        final byte[] paramsHash = computeExecutionCondition.hashValues(
                EncodingHelper.hexStringToBytes(did.getHash()),
                Keys.toChecksumAddress(grantee)
        ).send();
        return EncodingHelper.toHexString(
                computeExecutionCondition.generateId(
                        EncodingHelper.hexStringToBytes(serviceAgreementId),
                        paramsHash
                ).send()
        );
    }

    /**
     * Generates the Access condition identifier using the condition contract to be used in the service agreements
     * @param serviceAgreementId the service agreement id
     * @param did the asset decentralized identifier
     * @param grantee the address to get access to the asset
     * @return String with the condition Id
     */
    public String generateAccessConditionId(String serviceAgreementId, DID did, String grantee) throws Exception {
        final byte[] paramsHash = accessCondition.hashValues(
                EncodingHelper.hexStringToBytes(did.getHash()),
                Keys.toChecksumAddress(grantee)
        ).send();
        return EncodingHelper.toHexString(
                accessCondition.generateId(
                        EncodingHelper.hexStringToBytes(serviceAgreementId),
                        paramsHash
                ).send()
        );
    }

    /**
     * Generates the Escrow Payment condition identifier using the condition contract to be used in the service agreements
     * @param serviceAgreementId the service agreement id
     * @param did the asset decentralized identifier
     * @param amounts payment amounts
     * @param receivers of the payment
     * @param escrowConditionAddress the address where payment is locked
     * @param tokenAddress token address used for the payment
     * @param lockConditionId condition id used for locking the payment
     * @param releaseConditionId condition id used for releasing the access
     * @return String with the condition Id
     */
    public String generateEscrowPaymentConditionId(String serviceAgreementId, DID did, List<BigInteger> amounts, List<String> receivers, String escrowConditionAddress, String tokenAddress, String lockConditionId, String releaseConditionId) throws Exception {
        final byte[] paramsHash = escrowCondition.hashValues(
                EncodingHelper.hexStringToBytes(did.getHash()),
                amounts,
                receivers,
                Keys.toChecksumAddress(escrowConditionAddress),
                Keys.toChecksumAddress(tokenAddress),
                EncodingHelper.hexStringToBytes(lockConditionId),
                EncodingHelper.hexStringToBytes(releaseConditionId)
        ).send();
        return EncodingHelper.toHexString(
                escrowCondition.generateId(
                        EncodingHelper.hexStringToBytes(serviceAgreementId),
                        paramsHash
                ).send()
        );
    }

}
