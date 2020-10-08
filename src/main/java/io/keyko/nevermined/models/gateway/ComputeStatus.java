package io.keyko.nevermined.models.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.FromJsonToModel;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComputeStatus extends AbstractModel implements FromJsonToModel {
    @JsonProperty
    public String startedAt;

    @JsonProperty
    public String finishedAt;

    @JsonProperty
    public String status;

    @JsonProperty
    public DID did;

    @JsonProperty
    public List<PodStatus> pods;
}