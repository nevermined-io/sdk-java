package com.oceanprotocol.squid.manager;

import com.oceanprotocol.common.helpers.EncodingHelper;
import com.oceanprotocol.common.web3.KeeperService;
import com.oceanprotocol.squid.exceptions.ConditionNotFoundException;
import com.oceanprotocol.squid.exceptions.EthereumException;
import com.oceanprotocol.squid.exceptions.ServiceException;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.service.Agreement;
import com.oceanprotocol.squid.models.service.AgreementStatus;
import com.oceanprotocol.squid.models.service.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AgreementsManager extends BaseManager {

    private static final Logger log = LogManager.getLogger(AgreementsManager.class);

    public AgreementsManager(KeeperService keeperService, AquariusService aquariusService) {
        super(keeperService, aquariusService);
    }

    /**
     * Given the KeeperService and AquariusService, returns a new instance of AgreementsManager
     * using them as attributes
     *
     * @param keeperService   Keeper Dto
     * @param aquariusService Provider Dto
     * @return AgreementsManager
     */
    public static AgreementsManager getInstance(KeeperService keeperService, AquariusService aquariusService) {
        return new AgreementsManager(keeperService, aquariusService);
    }

    /**
     * Create an agreement using the escrowAccessSecretStoreTemplate. This method should be more specific in the future when we have more than one template.
     *
     * @param agreementId    the agreement id
     * @param ddo the ddo
     * @param conditionIds   list with the conditions ids
     * @param accessConsumer eth address of the consumer of the agreement.
     * @param service an instance of Service
     * @return a flag that is true if the agreement was successfully created.
     * @throws Exception exception
     */
    public Boolean createAccessAgreement(String agreementId, DDO ddo, List<byte[]> conditionIds,
                                         String accessConsumer, Service service) throws Exception {

        log.debug("Creating agreement with id: " + agreementId);
        TransactionReceipt txReceipt = escrowAccessSecretStoreTemplate.createAgreement(
                EncodingHelper.hexStringToBytes("0x" + agreementId),
                EncodingHelper.hexStringToBytes("0x" + ddo.getDid().getHash()),
                conditionIds,
                service.retrieveTimeOuts(),
                service.retrieveTimeLocks(),
                accessConsumer).send();
        return txReceipt.isStatusOK();
    }

    /**
     * Create an agreement using the escrowComputeExecutionTemplate. This method should be more specific in the future when we have more than one template.
     *
     * @param agreementId    the agreement id
     * @param ddo the ddo
     * @param conditionIds   list with the conditions ids
     * @param accessConsumer eth address of the consumer of the agreement.
     * @param service an instance of Service
     * @return a flag that is true if the agreement was successfully created.
     * @throws Exception exception
     */
    public Boolean createComputeAgreement(String agreementId, DDO ddo, List<byte[]> conditionIds,
                                         String accessConsumer, Service service) throws Exception {

        log.debug("Creating agreement with id: " + agreementId);
        TransactionReceipt txReceipt = escrowComputeExecutionTemplate.createAgreement(
                EncodingHelper.hexStringToBytes("0x" + agreementId),
                EncodingHelper.hexStringToBytes("0x" + ddo.getDid().getHash()),
                conditionIds,
                service.retrieveTimeOuts(),
                service.retrieveTimeLocks(),
                accessConsumer).send();
        return txReceipt.isStatusOK();
    }

    /**
     * Retrieve the agreement for a agreement_id.
     *
     * @param agreementId id of the agreement
     * @return Agreement
     * @throws Exception Exception
     */
    public Agreement getAgreement(String agreementId) throws Exception {
        return new Agreement(agreementStoreManager.getAgreement(EncodingHelper.hexStringToBytes(agreementId)).send());
    }

    /**
     * Get the status of a service agreement.
     *
     * @param agreementId id of the agreement
     * @return AgreementStatus with condition status of each of the agreement's conditions.
     * @throws Exception Exception
     */
    public AgreementStatus getStatus(String agreementId) throws Exception {

        List<byte[]> condition_ids = agreementStoreManager.getAgreement(EncodingHelper.hexStringToBytes(agreementId)).send().getValue4();
        AgreementStatus agreementStatus = new AgreementStatus();
        agreementStatus.agreementId = agreementId;
        AgreementStatus.ConditionStatusMap condition = new AgreementStatus.ConditionStatusMap();

        for (int i = 0; i <= condition_ids.size() - 1; i++) {

            Tuple7<String, BigInteger, BigInteger, BigInteger, BigInteger, String, BigInteger> agreementCondition =
                    conditionStoreManager.getCondition(condition_ids.get(i)).send();

            String address = agreementCondition.getValue1();
            String conditionName = getConditionNameByAddress(Keys.toChecksumAddress(address));
            BigInteger state = agreementCondition.getValue2();
            condition.conditions.put(conditionName, state);

        }
        agreementStatus.conditions.add(condition);
        return agreementStatus;
    }

    /**
     * Auxiliar method to get the name of the different conditions address.
     *
     * @param address contract address
     * @return string
     * @throws Exception exception
     */
    private String getConditionNameByAddress(String address) throws Exception {
        if (this.lockRewardCondition.getContractAddress().equals(address)) return "lockReward";
        else if (this.accessSecretStoreCondition.getContractAddress().equals(address)) return "accessSecretStore";
        else if (this.escrowReward.getContractAddress().equals(address)) return "escrowReward";
        else if (this.computeExecutionCondition.getContractAddress().equals(address)) return "computeExecution";
        else log.error("The current address" + address + "is not a condition address.");
        throw new ConditionNotFoundException("The current address" + address + "is not a condition address.");
    }



    private List<DID> getAccessAgreementsFulfilledByConsumer(String consumerAddress) throws ServiceException {

        EthFilter didFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                accessSecretStoreCondition.getContractAddress()
        );
        try {

            final Event event = accessSecretStoreCondition.FULFILLED_EVENT;
            final String eventSignature = EventEncoder.encode(event);
            didFilter.addSingleTopic(eventSignature);
            didFilter.addNullTopic();
            didFilter.addNullTopic();
            didFilter.addOptionalTopics(Numeric.toHexStringWithPrefixZeroPadded(Numeric.toBigInt(consumerAddress), 64));

            EthLog ethLog;

            try {
                ethLog = getKeeperService().getWeb3().ethGetLogs(didFilter).send();
            } catch (IOException e) {
                throw new EthereumException("Error creating consumedAssets filter.");
            }

            List<EthLog.LogResult> logs = ethLog.getLogs();
            List<DID> DIDlist = new ArrayList<>();
            for (int i = 0; i <= logs.size() - 1; i++) {
                DIDlist.add(DID.getFromHash(Numeric.cleanHexPrefix((((EthLog.LogObject) logs.get(i)).getTopics().get(2)))));
            }
            return DIDlist;

        } catch (Exception ex) {
            log.error("\"Unable to retrieve access agreements fulfilled by " + consumerAddress + ex.getMessage());
            throw new ServiceException("Unable to retrieve access agreements fulfilled by " + consumerAddress + ex.getMessage());
        }

    }


    private List<DID> getComputeAgreementsFulfilledByConsumer(String consumerAddress) throws ServiceException {

        EthFilter didFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                computeExecutionCondition.getContractAddress()
        );
        try {

            final Event event = computeExecutionCondition.FULFILLED_EVENT;
            final String eventSignature = EventEncoder.encode(event);
            didFilter.addSingleTopic(eventSignature);
            didFilter.addNullTopic();
            didFilter.addNullTopic();
            didFilter.addOptionalTopics(Numeric.toHexStringWithPrefixZeroPadded(Numeric.toBigInt(consumerAddress), 64));

            EthLog ethLog;

            try {
                ethLog = getKeeperService().getWeb3().ethGetLogs(didFilter).send();
            } catch (IOException e) {
                throw new EthereumException("Error creating consumedAssets filter.");
            }

            List<EthLog.LogResult> logs = ethLog.getLogs();
            List<DID> DIDlist = new ArrayList<>();
            for (int i = 0; i <= logs.size() - 1; i++) {
                DIDlist.add(DID.getFromHash(Numeric.cleanHexPrefix((((EthLog.LogObject) logs.get(i)).getTopics().get(2)))));
            }
            return DIDlist;

        } catch (Exception ex) {
            log.error("Unable to retrieve compute agreements fulfilled by " + consumerAddress + ex.getMessage());
            throw new ServiceException("Unable to retrieve compute agreements fulfilled by " + consumerAddress + ex.getMessage());
        }

    }

    /**
     * List of Asset objects purchased by consumerAddress
     *
     * @param consumerAddress ethereum address of consumer
     * @return list of dids
     * @throws ServiceException ServiceException
     */
    public List<DID> getConsumerAssets(String consumerAddress) throws ServiceException {

        return Stream.concat(getAccessAgreementsFulfilledByConsumer(consumerAddress).stream(),
                             getComputeAgreementsFulfilledByConsumer(consumerAddress).stream())
                .collect(Collectors.toList());
    }



}
