package com.oceanprotocol.squid.models.service.attributes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class ServiceCuration {

    @JsonProperty
    public float rating;

    @JsonProperty
    public int numVotes;

    @JsonProperty
    public String schema;

    @JsonProperty
    public boolean isListed;

}
