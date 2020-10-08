package io.keyko.nevermined.models.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.FromJsonToModel;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PodStatus extends AbstractModel implements FromJsonToModel {
    @JsonProperty
    public String startedAt;

    @JsonProperty
    public String finishedAt;

    @JsonProperty
    public String podName;

    @JsonProperty
    public String status;
}
