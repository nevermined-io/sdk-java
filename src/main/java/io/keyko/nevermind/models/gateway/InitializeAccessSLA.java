package io.keyko.nevermind.models.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.keyko.nevermind.models.AbstractModel;
import io.keyko.nevermind.models.FromJsonToModel;

public class InitializeAccessSLA extends AbstractModel implements FromJsonToModel {

    @JsonProperty
    public String did;

    @JsonProperty
    public String serviceAgreementId;

    @JsonProperty
    public String serviceDefinitionId;

    @JsonProperty
    public String signature;

    @JsonProperty
    public String consumerAddress;

    public InitializeAccessSLA() {
    }

    public InitializeAccessSLA(String did, String serviceAgreementId, String serviceDefinitionId, String signature, String consumerAddress) {
        this.did = did;
        this.serviceAgreementId = serviceAgreementId;
        this.serviceDefinitionId = serviceDefinitionId;
        this.signature = signature;
        this.consumerAddress = consumerAddress;
    }

    @Override
    public String toString() {
        return "InitializeAccessSLA{" +
                "did='" + did + '\'' +
                ", serviceAgreementId='" + serviceAgreementId + '\'' +
                ", serviceDefinitionId='" + serviceDefinitionId + '\'' +
                ", signature='" + signature + '\'' +
                ", consumerAddress='" + consumerAddress + '\'' +
                '}';
    }
}