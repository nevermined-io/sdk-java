/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.oceanprotocol.common.helpers.EncodingHelper;
import com.oceanprotocol.common.helpers.EthereumHelper;
import com.oceanprotocol.squid.exceptions.OceanRuntimeException;
import com.oceanprotocol.squid.exceptions.ServiceAgreementException;
import com.oceanprotocol.squid.models.AbstractModel;
import com.oceanprotocol.squid.models.FromJsonToModel;
import com.oceanprotocol.squid.models.service.attributes.ServiceAdditionalInformation;
import com.oceanprotocol.squid.models.service.attributes.ServiceCuration;
import com.oceanprotocol.squid.models.service.attributes.ServiceMain;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class Service extends AbstractModel implements FromJsonToModel {

    /**
     * Type of service in the DDO
     */
    public enum ServiceTypes {access, metadata, authorization, compute, provenance}

    /**
     * Type of Asset. Represented in the base.type attribute
     */
    public enum AssetTypes {dataset, algorithm, workflow, service}


    @JsonIgnore
    public static final String CONSUMER_ADDRESS_PARAM = "consumerAddress";

    @JsonIgnore
    public static final String SERVICE_AGREEMENT_PARAM = "serviceAgreementId";

    @JsonIgnore
    public static final String URL_PARAM = "url";

    @JsonIgnore
    public static final String WORKFLOWID_PARAM = "workflowDID";

    @JsonIgnore
    public static final String SIGNATURE_PARAM = "signature";


    @JsonIgnore
    public static final int DEFAULT_METADATA_INDEX = 0;
    @JsonIgnore
    public static final int DEFAULT_PROVENANCE_INDEX = 1;
    @JsonIgnore
    public static final int DEFAULT_AUTHORIZATION_INDEX = 2;
    @JsonIgnore
    public static final int DEFAULT_ACCESS_INDEX = 3;
    @JsonIgnore
    public static final int DEFAULT_COMPUTING_INDEX = 4;


    @JsonProperty
    public int index;

    @JsonProperty
    public String type;

    @JsonProperty
    public String templateId;

    @JsonProperty
    public String serviceEndpoint;

    @JsonProperty
    public Attributes attributes;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class Attributes {

        @JsonProperty
        public String encryptedFiles = null;

        @JsonProperty
        public ServiceMain main;

        @JsonProperty
        public ServiceAdditionalInformation additionalInformation;

        @JsonProperty
        public Service.ServiceAgreementTemplate serviceAgreementTemplate;

        @JsonProperty
        public ServiceCuration curation;

    }

    //@JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class ServiceAgreementTemplate {

        @JsonProperty
        public String contractName;

        @JsonProperty
        public List<Condition.Event> events = new ArrayList<>();

        @JsonProperty
        public List<String> fulfillmentOrder = Arrays.asList(
                "lockReward.fulfill",
                "accessSecretStore.fulfill",
                "escrowReward.fulfill");

        @JsonProperty
        public ConditionDependency conditionDependency = new ConditionDependency();

        @JsonProperty
        public List<Condition> conditions = new ArrayList<>();

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class ConditionDependency {

        @JsonProperty
        public List<String> lockReward = Arrays.asList();

        @JsonProperty
        public List<String> accessSecretStore = Arrays.asList();

        @JsonProperty
        public List<String> escrowReward = Arrays.asList("lockReward", "accessSecretStore");

    }

    public Service() {
    }

    public Service(ServiceTypes type, String serviceEndpoint, int index) {
        this.type = type.toString();
        this.index = index;
        this.serviceEndpoint = serviceEndpoint;

        this.attributes = new Attributes();
        this.attributes.main = new ServiceMain();
        this.attributes.additionalInformation = new ServiceAdditionalInformation();
    }

    public List<BigInteger> retrieveTimeOuts() {
        List<BigInteger> timeOutsList = new ArrayList<BigInteger>();
        for (Condition condition : attributes.serviceAgreementTemplate.conditions) {
            timeOutsList.add(BigInteger.valueOf(condition.timeout));
        }
        return timeOutsList;
    }

    public Integer calculateServiceTimeout() {

        List<BigInteger> timeOutsList = retrieveTimeOuts();
        return timeOutsList.stream().mapToInt(BigInteger::intValue).max().orElse(0);
    }

    public List<BigInteger> retrieveTimeLocks() {
        List<BigInteger> timeLocksList = new ArrayList<BigInteger>();
        for (Condition condition : attributes.serviceAgreementTemplate.conditions) {
            timeLocksList.add(BigInteger.valueOf(condition.timelock));
        }
        return timeLocksList;
    }

    public Condition getConditionbyName(String name) {

        return this.attributes.serviceAgreementTemplate.conditions.stream()
                .filter(condition -> condition.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String fetchConditionValues() throws UnsupportedEncodingException {

        String data = "";

        for (Condition condition : attributes.serviceAgreementTemplate.conditions) {
            String token = "";

            for (Condition.ConditionParameter param : condition.parameters) {
                token = token + EthereumHelper.encodeParameterValue(param.type, param.value);
            }

            data = data + EthereumHelper.remove0x(Hash.sha3(token));
        }

        return data;
    }

    public String fetchTimeout() throws IOException {
        String data = "";

        for (Condition condition : attributes.serviceAgreementTemplate.conditions) {
            data = data + EthereumHelper.remove0x(
                    EncodingHelper.hexEncodeAbiType("uint256", condition.timeout));
        }

        return data;
    }


    public String fetchTimelock() throws IOException {
        String data = "";

        for (Condition condition : attributes.serviceAgreementTemplate.conditions) {
            data = data + EthereumHelper.remove0x(
                    EncodingHelper.hexEncodeAbiType("uint256", condition.timelock));
        }

        return data;
    }


    private byte[] wrappedEncoder(String s) {
        try {
            return EncodingHelper.hexStringToBytes(s);
        }
        catch(UnsupportedEncodingException e) {
            throw new OceanRuntimeException("There was a problem enconding a string ", e);
        }
    }

    public List<byte[]> generateByteConditionIds(String agreementId, Map<String, String> conditionsAddresses, String publisherAddress, String consumerAddress) throws Exception{

        List<String> conditions = generateConditionIds(agreementId,  conditionsAddresses, publisherAddress,  consumerAddress);
        return conditions.stream().map(this::wrappedEncoder).collect(Collectors.toList());
    }

    public List<String> generateConditionIds(String agreementId, Map<String, String> conditionsAddresses, String publisherAddress, String consumerAddress) throws Exception{
        return null;
    }

    public String generateLockRewardId(String serviceAgreementId, String escrowRewardAddress, String lockRewardConditionAddress) throws UnsupportedEncodingException {

        Condition lockRewardCondition = this.getConditionbyName("lockReward");

        Condition.ConditionParameter rewardAddress = lockRewardCondition.getParameterByName("_rewardAddress");
        Condition.ConditionParameter amount = lockRewardCondition.getParameterByName("_amount");

        String params = EthereumHelper.add0x(EthereumHelper.encodeParameterValue(rewardAddress.type, escrowRewardAddress)
                + EthereumHelper.encodeParameterValue(amount.type, amount.value.toString())
        );

        String valuesHash = Hash.sha3(params);

        return Hash.sha3(
                EthereumHelper.add0x(
                        EthereumHelper.encodeParameterValue("bytes32", serviceAgreementId)
                                + EthereumHelper.encodeParameterValue("address", lockRewardConditionAddress)
                                + EthereumHelper.encodeParameterValue("bytes32", valuesHash)
                )
        );

    }

    public String generateEscrowRewardConditionId(String serviceAgreementId, String consumerAddress, String publisherAddress, String escrowRewardConditionAddress,
                                                  String lockConditionId, String releaseConditionId) throws UnsupportedEncodingException {

        Condition accessSecretStoreCondition = this.getConditionbyName("escrowReward");

        Condition.ConditionParameter amount = accessSecretStoreCondition.getParameterByName("_amount");
        Condition.ConditionParameter receiver = accessSecretStoreCondition.getParameterByName("_receiver");
        Condition.ConditionParameter sender = accessSecretStoreCondition.getParameterByName("_sender");
        Condition.ConditionParameter lockCondition = accessSecretStoreCondition.getParameterByName("_lockCondition");
        Condition.ConditionParameter releaseCondition = accessSecretStoreCondition.getParameterByName("_releaseCondition");

        String params = EthereumHelper.add0x(EthereumHelper.encodeParameterValue(amount.type, amount.value.toString())
                + EthereumHelper.encodeParameterValue(receiver.type, publisherAddress)
                + EthereumHelper.encodeParameterValue(sender.type, consumerAddress)
                + EthereumHelper.encodeParameterValue(lockCondition.type, lockConditionId)
                + EthereumHelper.encodeParameterValue(releaseCondition.type, releaseConditionId));

        String valuesHash = Hash.sha3(params);

        return Hash.sha3(
                EthereumHelper.add0x(
                        EthereumHelper.encodeParameterValue("bytes32", serviceAgreementId)
                                + EthereumHelper.encodeParameterValue("address", escrowRewardConditionAddress)
                                + EthereumHelper.encodeParameterValue("bytes32", valuesHash)
                )
        );

    }

    public String generateReleaseConditionId(String serviceAgreementId, String consumerAddress, String releaseConditionAddress, String conditionName) throws UnsupportedEncodingException {

        Condition accessSecretStoreCondition = this.getConditionbyName(conditionName);

        Condition.ConditionParameter documentId = accessSecretStoreCondition.getParameterByName("_documentId");
        Condition.ConditionParameter grantee = accessSecretStoreCondition.getParameterByName("_grantee");

        String params = EthereumHelper.add0x(EthereumHelper.encodeParameterValue(documentId.type, documentId.value)
                + EthereumHelper.encodeParameterValue(grantee.type, consumerAddress));

        String valuesHash = Hash.sha3(params);

        return Hash.sha3(
                EthereumHelper.add0x(
                        EthereumHelper.encodeParameterValue("bytes32", serviceAgreementId)
                                + EthereumHelper.encodeParameterValue("address", releaseConditionAddress)
                                + EthereumHelper.encodeParameterValue("bytes32", valuesHash)
                )
        );

    }

    public String generateServiceAgreementSignatureFromHash(Web3j web3, String consumerAddress, String consumerPassword, String hash) throws IOException {
        return EthereumHelper.ethSignMessage(web3, hash, consumerAddress, consumerPassword);
    }

    /**
     * Generates a Hash representing a Service Agreement
     * The Hash is having the following parameters:
     * (templateId, conditionKeys, conditionValues, timeout, serviceAgreementId)
     *
     * @param serviceAgreementId                Service Agreement Id
     * @param consumerAddress                   the address of the consumer of the service
     * @param publisherAddress                  the address of the publisher of the asset
     * @param conditionsAddresses               addresses of the conditions
     * @return Hash
     * @throws ServiceAgreementException ServiceAgreementException
     */
    public String generateServiceAgreementHash(String serviceAgreementId, String consumerAddress, String publisherAddress,
                                               Map<String, String> conditionsAddresses ) throws ServiceAgreementException {

        String params = "";

        try {
            List<String> conditions = this.generateConditionIds(serviceAgreementId, conditionsAddresses, publisherAddress, consumerAddress);

            String releaseId = conditions.get(0);
            String lockRewardId = conditions.get(1);
            String escrowRewardId = conditions.get(2);

            params =
                    EthereumHelper.remove0x(
                            templateId
                                    + releaseId
                                    + lockRewardId
                                    + escrowRewardId
                                    + fetchTimelock()
                                    + fetchTimeout()
                                    + serviceAgreementId
                    );

        } catch (Exception e) {
            throw new ServiceAgreementException(serviceAgreementId, "Error generating Service Agreement Hash ", e);
        }

        return Hash.sha3(EthereumHelper.add0x(params));
    }

}