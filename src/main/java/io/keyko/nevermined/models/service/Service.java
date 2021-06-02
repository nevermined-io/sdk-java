package io.keyko.nevermined.models.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.common.helpers.EncodingHelper;
import io.keyko.nevermined.exceptions.DDOException;
import io.keyko.nevermined.exceptions.NeverminedRuntimeException;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.FromJsonToModel;
import io.keyko.nevermined.models.service.attributes.ServiceAdditionalInformation;
import io.keyko.nevermined.models.service.attributes.ServiceCuration;
import io.keyko.nevermined.models.service.attributes.ServiceMain;
import org.web3j.crypto.Keys;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class Service extends AbstractModel implements FromJsonToModel {

    /**
     * Type of service in the DDO
     */
    public enum ServiceTypes {
        ACCESS, METADATA, AUTHORIZATION, COMPUTE, PROVENANCE, NFT_ACCESS, NFT_SALES, DID_SALES;

        @Override
        public String toString() {
            return super.toString().toLowerCase().replace("_", "-");
        }
    }

    /**
     * Type of Asset. Represented in the base.type attribute
     */
    public enum AssetTypes {
        DATASET, ALGORITHM, WORKFLOW, SERVICE, FL_COORDINATOR;

        @Override
        public String toString() {
            return super.toString().toLowerCase().replace("_", "-");
        }
    }


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
    public static final int DEFAULT_COMPUTE_INDEX = 4;

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

    public ServiceTypes fetchServiceType()   {
        return ServiceTypes.valueOf(type.toUpperCase().replace("-", "_"));
    }

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class ServiceAgreementTemplate {

        @JsonProperty
        public String contractName;

        @JsonProperty
        public List<Condition.Event> events = new ArrayList<>();

        @JsonProperty
        public List<String> fulfillmentOrder = Arrays.asList(
                "lockPayment.fulfill",
                "access.fulfill",
                "escrowPayment.fulfill");

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
        public List<String> execCompute = Arrays.asList();

        @JsonProperty
        public List<String> escrowReward = Arrays.asList(
                Condition.ConditionTypes.lockPayment.toString(),
                Condition.ConditionTypes.access.toString());

        public static List<String> defaultComputeEscrowPaymentCondition() {
            return Arrays.asList(
                    Condition.ConditionTypes.lockPayment.toString(),
                    Condition.ConditionTypes.execCompute.toString());
        }
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

    public String getTemplateId()   {
        return this.templateId;
    }

    public List<BigInteger> retrieveTimeOuts() {
        List<BigInteger> timeOutsList = new ArrayList<BigInteger>();
        for (Condition condition : attributes.serviceAgreementTemplate.conditions) {
            timeOutsList.add(BigInteger.valueOf(condition.timeout));
        }
        return timeOutsList;
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

    public List<BigInteger> fetchAmounts() throws DDOException {
        List<BigInteger> _amounts = new ArrayList<>();

        final Condition.ConditionParameter amounts = attributes.serviceAgreementTemplate.conditions.stream()
                .flatMap(condition -> condition.parameters.stream())
                .filter(param -> param.name.equals("_amounts"))
                .findFirst()
                .orElseThrow(() -> new DDOException("Unable to find the _amounts parameter"));

        for (Object _someAmount: (ArrayList) amounts.value) {
            if (_someAmount instanceof Integer || _someAmount instanceof Long)
                _amounts.add(new BigInteger(String.valueOf(_someAmount)));
            else if (_someAmount instanceof BigInteger)
                _amounts.add((BigInteger) _someAmount);
            else
                _amounts.add(new BigInteger(_someAmount.toString()));
        }
        return _amounts;
    }

    public List<String> fetchReceivers() throws DDOException {
        final Condition.ConditionParameter receivers = attributes.serviceAgreementTemplate.conditions.stream()
                .flatMap(condition -> condition.parameters.stream())
                .filter(param -> param.name.equals("_receivers"))
                .findFirst()
                .orElseThrow(() -> new DDOException("Unable to find the _receivers parameter"));
        return ((List<String>) receivers.value).stream().map(a -> Keys.toChecksumAddress(a))
                .collect(Collectors.toList());
    }

    public BigInteger fetchTotalPrice() throws DDOException {
        return fetchAmounts().stream().reduce(BigInteger.ZERO, BigInteger::add);
    }

    public BigInteger fetchNumberNFTs() throws DDOException {
        return new BigInteger(fetchConditionValue("_numberNfts"));
    }

    public String fetchConditionValue(String paramName) throws DDOException {
        final Condition.ConditionParameter parameter = attributes.serviceAgreementTemplate.conditions.stream()
                .flatMap(condition -> condition.parameters.stream())
                .filter(param -> param.name.equals(paramName))
                .findFirst()
                .orElseThrow(() -> new DDOException("Unable to find the " + paramName + " parameter"));
        return (String) parameter.value;
    }

    private static byte[] wrappedEncoder(String s) {
        try {
            return EncodingHelper.hexStringToBytes(s);
        }
        catch(UnsupportedEncodingException e) {
            throw new NeverminedRuntimeException("There was a problem enconding a string ", e);
        }
    }

    public static List<byte[]> transformConditionIdsToByte(List<String> conditions) {
        return conditions.stream().map(Service::wrappedEncoder).collect(Collectors.toList());
    }

}