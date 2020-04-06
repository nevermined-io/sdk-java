package com.oceanprotocol.squid.models.brizo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oceanprotocol.squid.models.AbstractModel;
import com.oceanprotocol.squid.models.FromJsonToModel;

public class ExecuteService extends AbstractModel implements FromJsonToModel {


    @JsonProperty
    public String agreementId;

    @JsonProperty
    public String workflowId;

    @JsonProperty
    public String consumerAddress;

    @JsonProperty
    public String signature;

    public ExecuteService() {}

    public ExecuteService(String agreementId, String workflowId, String consumerAddress, String signature) {
        this.signature = signature;
        this.agreementId = agreementId;
        this.workflowId = workflowId;
        this.consumerAddress = consumerAddress;
    }

    @Override
    public String toString() {
        return "ExecuteService{" +
                " agreementId='" + agreementId + '\'' +
                ", workflowId='" + workflowId + '\'' +
                ", consumerAddress='" + consumerAddress + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }

}
