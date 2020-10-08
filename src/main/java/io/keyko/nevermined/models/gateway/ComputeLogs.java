package io.keyko.nevermined.models.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.FromJsonToModel;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComputeLogs extends AbstractModel implements FromJsonToModel {
    @JsonProperty
    public String content;

    @JsonProperty
    public String podName;
}
