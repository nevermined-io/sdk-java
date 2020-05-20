package io.keyko.nevermined.models.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.FromJsonToModel;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Status extends AbstractModel implements FromJsonToModel {

    @JsonProperty
    public Map<String, String> contracts = new HashMap<>();

    @JsonProperty("keeper-url")
    public String keeperUrl;

    @JsonProperty("keeper-version")
    public String keeperVersion;

    @JsonProperty()
    public String network;

    @JsonProperty("provider-address")
    public String providerAddress;

    @JsonProperty("ecdsa-public-key")
    public String ecdsaPublicKey;

    @JsonProperty("rsa-public-key")
    public String rsaPublicKey;

    @JsonProperty()
    public String software;

    @JsonProperty()
    public String version;

}
