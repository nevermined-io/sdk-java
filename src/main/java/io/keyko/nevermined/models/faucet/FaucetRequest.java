package io.keyko.nevermined.models.faucet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.FromJsonToModel;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FaucetRequest extends AbstractModel implements FromJsonToModel {


    @JsonProperty
    public String address;

    @JsonProperty
    public String agent;

    public FaucetRequest(String address, String agent)  {
        this.address = address;
        this.agent = agent;
    }

    public FaucetRequest(String address)  {
        this(address, "SDK-JAVA");
    }

}
