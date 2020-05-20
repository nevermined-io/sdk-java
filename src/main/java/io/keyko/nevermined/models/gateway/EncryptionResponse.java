package io.keyko.nevermined.models.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.FromJsonToModel;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EncryptionResponse extends AbstractModel implements FromJsonToModel {

    @JsonProperty
    public String hash;

    @JsonProperty
    public String method;

    @JsonProperty("public-key")
    public String publicKey;

    EncryptionResponse() {}

    public EncryptionResponse(String message, String method) {
        this.hash = hash;
        this.method = method;
    }

    public EncryptionResponse(String message, String method, String publicKey) {
        this.hash = hash;
        this.method = method;
        this.publicKey = publicKey;
    }
}
