/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models.service.metadata;

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
    public List<Algorithm.Requirement> requirements;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class Requirement {

        @JsonProperty
        public String requirement;

        @JsonProperty
        public String version;

    }

}
