package com.oceanprotocol.squid.models.service.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.oceanprotocol.common.helpers.EthereumHelper;
import com.oceanprotocol.squid.models.service.Condition;
import com.oceanprotocol.squid.models.service.Service;
import org.web3j.crypto.Hash;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class ComputingService extends Service {

    @JsonIgnore
    public static final int DEFAULT_INDEX = 4;

    @JsonPropertyOrder(alphabetic = true)
    public static class Provider {

        @JsonProperty
        public String type;

        @JsonProperty
        public String description;

        @JsonProperty
        public Enviroment environment;

        @JsonPropertyOrder(alphabetic = true)
        public static class Container {

            @JsonProperty
            public String image;

            @JsonProperty
            public String tag;

            @JsonProperty
            public String checksum;

        }

        @JsonPropertyOrder(alphabetic = true)
        public static class Server {

            @JsonProperty
            public String serverId;

            @JsonProperty
            public String serverType;

            @JsonProperty
            public String price;

            @JsonProperty
            public String cpu;

            @JsonProperty
            public String gpu;

            @JsonProperty
            public String memory;

            @JsonProperty
            public String disk;

            @JsonProperty
            public Integer maxExecutionTime;

        }


        @JsonPropertyOrder(alphabetic = true)
        public static class Cluster {

            @JsonProperty
            public String type;

            @JsonProperty
            public String url;

        }


        @JsonPropertyOrder(alphabetic = true)
        public static class Enviroment {

            @JsonProperty
            public Cluster cluster;

            @JsonProperty
            public List<Container> supportedContainers = new ArrayList<>();

            @JsonProperty
            public List<Server> supportedServers = new ArrayList<>();

        }

    }

    public ComputingService() {
        this.index = DEFAULT_INDEX;
        type= ServiceTypes.compute.toString();
    }

    public ComputingService(String serviceEndpoint, int serviceDefinitionId, String templateId) {
        super(ServiceTypes.compute, serviceEndpoint, serviceDefinitionId);
        this.templateId = templateId;

    }

    public ComputingService(String serviceEndpoint, int serviceDefinitionId, ServiceAgreementTemplate serviceAgreementTemplate, String templateId) {
        super(ServiceTypes.compute, serviceEndpoint, serviceDefinitionId);
        this.templateId = templateId;
        this.attributes.serviceAgreementTemplate = serviceAgreementTemplate;
    }


    @Override
    public List<String> generateConditionIds(String agreementId, Map<String, String> conditionsAddresses, String publisherAddress, String consumerAddress)  throws Exception{

        String escrowRewardAddress = conditionsAddresses.get("escrowRewardAddress");
        String lockRewardConditionAddress = conditionsAddresses.get("lockRewardConditionAddress");
        String computeExecutionConditionAddress = conditionsAddresses.get("computeExecutionConditionAddress");

        List<String> conditionIds = new ArrayList<>();
        String lockRewardId = generateLockRewardId(agreementId, escrowRewardAddress,lockRewardConditionAddress);
        String computeExecutionConditionId = generateComputeExecutionConditionId(agreementId, consumerAddress, computeExecutionConditionAddress);
        String escrowRewardId = generateEscrowRewardConditionId(agreementId, consumerAddress, publisherAddress, escrowRewardAddress, lockRewardId, computeExecutionConditionId);
        conditionIds.add(computeExecutionConditionId);
        conditionIds.add(lockRewardId);
        conditionIds.add(escrowRewardId);
        return conditionIds;
    }

    public String generateComputeExecutionConditionId(String serviceAgreementId, String consumerAddress, String computeExecutionConditionAddress) throws UnsupportedEncodingException {

        Condition accessSecretStoreCondition = this.getConditionbyName("execCompute");

        Condition.ConditionParameter documentId = accessSecretStoreCondition.getParameterByName("_documentId");
        Condition.ConditionParameter grantee = accessSecretStoreCondition.getParameterByName("_grantee");


        String params = EthereumHelper.add0x(EthereumHelper.encodeParameterValue(documentId.type, documentId.value)
                + EthereumHelper.encodeParameterValue(grantee.type, consumerAddress));

        String valuesHash = Hash.sha3(params);

        return Hash.sha3(
                EthereumHelper.add0x(
                        EthereumHelper.encodeParameterValue("bytes32", serviceAgreementId)
                                + EthereumHelper.encodeParameterValue("address", computeExecutionConditionAddress)
                                + EthereumHelper.encodeParameterValue("bytes32", valuesHash)
                )
        );

    }

}
