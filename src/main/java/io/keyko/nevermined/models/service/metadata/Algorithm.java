package io.keyko.nevermined.models.service.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class Algorithm {

    @JsonProperty
    public String language;

    @JsonProperty
    public String format;

    @JsonProperty
    public String version;

    @JsonProperty
    public String entrypoint;

    @JsonProperty
    public Requirements requirements;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class Requirements {

        @JsonProperty
        public Workflow.Container container;

    }

}
