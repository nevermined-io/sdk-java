package io.keyko.nevermind.models.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.nevermind.models.AbstractModel;
import io.keyko.nevermind.models.FromJsonToModel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class AgreementStatus extends AbstractModel implements FromJsonToModel {

    @JsonProperty
    public String agreementId;

    @JsonProperty
    public List<ConditionStatusMap> conditions = new ArrayList<>();

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class ConditionStatusMap {

        @JsonProperty
        public Map<String, BigInteger> conditions = new HashMap<>();


    }


}