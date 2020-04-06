/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.oceanprotocol.squid.models.AbstractModel;
import com.oceanprotocol.squid.models.FromJsonToModel;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class Condition extends AbstractModel implements FromJsonToModel {

    @JsonProperty
    public String name;

    @JsonProperty
    public int timelock;

    @JsonProperty
    public int timeout;

    @JsonProperty
    public String contractName;

    @JsonProperty
    public String functionName;

    @JsonProperty
    public List<ConditionParameter> parameters = new ArrayList<>();

    @JsonProperty
    public List<Event> events = new ArrayList<>();


    public Condition() {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class Event {

        @JsonProperty
        public String name;

        @JsonProperty
        public String actorType;

        @JsonProperty
        public Handler handler;

        public Event() {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class Handler {

        @JsonProperty
        public String moduleName;

        @JsonProperty
        public String functionName;

        @JsonProperty
        public String version;

        public Handler() {
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class ConditionParameter {

        @JsonProperty
        public String name;

        @JsonProperty
        public String type;

        @JsonProperty
        public Object value;

        public ConditionParameter() {
        }
    }


    public ConditionParameter getParameterByName(String name) {

        return this.parameters.stream()
                .filter(parameter -> parameter.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }


}