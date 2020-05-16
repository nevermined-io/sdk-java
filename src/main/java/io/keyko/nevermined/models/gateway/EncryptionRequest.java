package io.keyko.nevermined.models.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.FromJsonToModel;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EncryptionRequest extends AbstractModel implements FromJsonToModel {

    @JsonProperty
    public String message;

    @JsonProperty
    public String method;

    @JsonProperty
    public String did = "";

    EncryptionRequest() {}

    public EncryptionRequest(String message, String method) {
        this.message = message;
        this.method = method;
    }

    public EncryptionRequest(String message, String method, String did) {
        this.message = message;
        this.method = method;
        this.did = did;
    }
}
