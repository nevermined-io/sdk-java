/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.core.sla.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.common.helpers.CryptoHelper;
import com.oceanprotocol.common.helpers.EncodingHelper;
import com.oceanprotocol.keeper.contracts.AccessSecretStoreCondition;
import com.oceanprotocol.keeper.contracts.ComputeExecutionCondition;
import com.oceanprotocol.keeper.contracts.EscrowAccessSecretStoreTemplate;
import com.oceanprotocol.keeper.contracts.EscrowComputeExecutionTemplate;
import com.oceanprotocol.squid.exceptions.InitializeConditionsException;
import com.oceanprotocol.squid.models.AbstractModel;
import com.oceanprotocol.squid.models.service.Condition;
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
    public static Flowable<String> listenExecuteAgreement(EscrowAccessSecretStoreTemplate slaContract, String serviceAgreementId) {
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
     * Define and execute a Filter over the AccessSecretStoreCondition Contract to listen for an Fulfilled event
     *
     * @param accessCondition     the AccessSecretStoreCondition contract
     * @param serviceAgreementId the serviceAgreement Id
     * @return a Flowable to handle the event in an asynchronous fashion
     */
    public static Flowable<String> listenForFulfilledEvent(AccessSecretStoreCondition accessCondition, String serviceAgreementId) {

        EthFilter grantedFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                accessCondition.getContractAddress()
        );

        final Event event = AccessSecretStoreCondition.FULFILLED_EVENT;
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
     * @return a List with all the conditions of the template
     * @throws InitializeConditionsException InitializeConditionsException
     */
    public List<Condition> initializeConditions(Map<String, Object> params) throws InitializeConditionsException {

        try {
            conditionsTemplate = IOUtils.toString(
                    this.getClass().getClassLoader().getResourceAsStream("sla/" + getConditionFileTemplate()),
                    StandardCharsets.UTF_8);

        } catch (IOException ex) {
        }

        try {

            if (conditionsTemplate == null)
                conditionsTemplate = new String(Files.readAllBytes(Paths.get("src/main/resources/sla/" + getConditionFileTemplate())));

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
