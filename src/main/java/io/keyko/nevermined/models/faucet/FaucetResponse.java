package io.keyko.nevermined.models.faucet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.FromJsonToModel;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FaucetResponse extends AbstractModel implements FromJsonToModel {


    @JsonProperty
    public boolean success;

    @JsonProperty
    public String message;

    @JsonProperty
    public String txHash;

    public FaucetResponse(boolean success, String message)  {
        this.success = success;
        this.message = message;
    }

    public FaucetResponse() {}
}
