package io.keyko.nevermined.core.sla.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import io.keyko.common.helpers.CryptoHelper;
import io.keyko.common.helpers.EncodingHelper;
import io.keyko.nevermined.contracts.AccessCondition;
import io.keyko.nevermined.contracts.ComputeExecutionCondition;
import io.keyko.nevermined.contracts.AccessTemplate;
import io.keyko.nevermined.contracts.EscrowComputeExecutionTemplate;
import io.keyko.nevermined.exceptions.InitializeConditionsException;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.AssetRewards;
import io.keyko.nevermined.models.service.Condition;
import io.reactivex.Flowable;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Handles functionality related with the execution of a Service Agreement
 */
public abstract class ServiceAgreementHandler {

    private static final Logger log = LogManager.getLogger(ServiceAgreementHandler.class);

    private String conditionsTemplate = null;


    /**
     * Generates a new and random Service Agreement Id
     *
     * @return a String with the new Service Agreement Id
     */
    public static String generateSlaId() {
        String token = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        return token.replaceAll("-", "");
    }

    /**
     * Define and execute a Filter over the Service Agreement Contract to listen for an AgreementInitialized event
     *
     * @param slaContract        the address of the service agreement contract
     * @param serviceAgreementId the service agreement Id
     * @return a Flowable to handle the in an asynchronous fashion
     */
    public static Flowable<String> listenExecuteAgreement(AccessTemplate slaContract, String serviceAgreementId) {
        EthFilter slaFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                slaContract.getContractAddress()
        );

        final Event event = slaContract.AGREEMENTCREATED_EVENT;
        final String eventSignature = EventEncoder.encode(event);
        String slaTopic = "0x" + serviceAgreementId;
        slaFilter.addSingleTopic(eventSignature);
        slaFilter.addOptionalTopics(slaTopic);

        return slaContract.agreementCreatedEventFlowable(slaFilter)
                .map(eventResponse -> EncodingHelper.toHexString(eventResponse._agreementId));
    }


    /**
     * Define and execute a Filter over the Service Agreement Contract to listen for an AgreementInitialized event
     *
     * @param slaContract        the address of the service agreement contract
     * @param serviceAgreementId the service agreement Id
     * @return a Flowable to handle the event in an asynchronous fashion
     */
    public static Flowable<String> listenExecuteAgreement(EscrowComputeExecutionTemplate slaContract, String serviceAgreementId) {
        EthFilter slaFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                slaContract.getContractAddress()
        );

        final Event event = slaContract.AGREEMENTCREATED_EVENT;
        final String eventSignature = EventEncoder.encode(event);
        String slaTopic = "0x" + serviceAgreementId;
        slaFilter.addSingleTopic(eventSignature);
        slaFilter.addOptionalTopics(slaTopic);

        return slaContract.agreementCreatedEventFlowable(slaFilter)
                .map(eventResponse -> EncodingHelper.toHexString(eventResponse._agreementId));
    }

    /**
     * Define and execute a Filter over the AccessCondition Contract to listen for an Fulfilled event
     *
     * @param accessCondition     the AccessCondition contract
     * @param serviceAgreementId the serviceAgreement Id
     * @return a Flowable to handle the event in an asynchronous fashion
     */
    public static Flowable<String> listenForFulfilledEvent(AccessCondition accessCondition, String serviceAgreementId) {

        EthFilter grantedFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                accessCondition.getContractAddress()
        );

        final Event event = AccessCondition.FULFILLED_EVENT;
        final String eventSignature = EventEncoder.encode(event);
        String slaTopic = "0x" + serviceAgreementId;

        grantedFilter.addSingleTopic(eventSignature);
        grantedFilter.addOptionalTopics(slaTopic);


        return accessCondition.fulfilledEventFlowable(grantedFilter)
                .map(eventResponse ->  EncodingHelper.toHexString(eventResponse._agreementId));
    }

    /**
     * Define and execute a Filter over the ComputeExecutionCondition Contract to listen for an Fulfilled event
     *
     * @param computeCondition    the ComputeExecutionCondition contract
     * @param serviceAgreementId the serviceAgreement Id
     * @return a Flowable to handle the event in an asynchronous fashion
     */
    public static Flowable<String> listenForFulfilledEvent(ComputeExecutionCondition computeCondition, String serviceAgreementId) {

        EthFilter grantedFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                computeCondition.getContractAddress()
        );

        final Event event = ComputeExecutionCondition.FULFILLED_EVENT;
        final String eventSignature = EventEncoder.encode(event);
        String slaTopic = "0x" + serviceAgreementId;

        grantedFilter.addSingleTopic(eventSignature);
        grantedFilter.addOptionalTopics(slaTopic);


        return computeCondition.fulfilledEventFlowable(grantedFilter)
                .map(eventResponse ->  EncodingHelper.toHexString(eventResponse._agreementId));
    }


    /**
     * gets the name of the file that contains a template for the conditions
     * @return the name of the template file
     */
    public abstract String getConditionFileTemplate();

    /**
     * Gets and Initializes all the conditions associated with a template
     *
     * @param params params to fill the conditions
     * @param assetRewards the asset rewards configuration
     * @return a List with all the conditions of the template
     * @throws InitializeConditionsException InitializeConditionsException
     */
    public List<io.keyko.nevermined.models.service.Condition> initializeConditions(Map<String, Object> params, AssetRewards assetRewards) throws InitializeConditionsException {

        try {
            conditionsTemplate = IOUtils.toString(
                    this.getClass().getClassLoader().getResourceAsStream("sla/" + getConditionFileTemplate()),
                    StandardCharsets.UTF_8);

        } catch (IOException ex) {
        }

        try {

            if (conditionsTemplate == null)
                conditionsTemplate = new String(Files.readAllBytes(Paths.get("src/main/resources/sla/" + getConditionFileTemplate())));

            // Amounts and Receivers parameters
            conditionsTemplate = conditionsTemplate.replaceAll("\\{parameter.receivers\\}", assetRewards.getReceiversArrayString());
            conditionsTemplate = conditionsTemplate.replaceAll("\\{parameter.amounts\\}", assetRewards.getAmountsArrayString());
            conditionsTemplate = conditionsTemplate.replaceAll("\\{parameter._numberNfts\\}", assetRewards.numberNFTs.toString());

            params.forEach((_name, _func) -> {
                if (_func instanceof byte[])
                    conditionsTemplate = conditionsTemplate.replaceAll("\\{" + _name + "\\}", CryptoHelper.getHex((byte[]) _func));
                else
                    conditionsTemplate = conditionsTemplate.replaceAll("\\{" + _name + "\\}", _func.toString());
            });

            return AbstractModel
                    .getMapperInstance()
                    .readValue(conditionsTemplate, new TypeReference<List<Condition>>() {
                    });
        } catch (Exception e) {
            String msg = "Error initializing conditions for template";
            log.error(msg);
            throw new InitializeConditionsException(msg, e);
        }
    }


}
