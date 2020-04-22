package io.keyko.nevermind.models.service.template;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.nevermind.models.AbstractModel;
import io.keyko.nevermind.models.FromJsonToModel;
import io.keyko.nevermind.models.service.types.AccessService;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class AccessTemplate extends AbstractModel implements FromJsonToModel {

    @JsonProperty
    public String type = "Access";

    @JsonProperty
    public String id = "";

    @JsonProperty
    public String name = "dataAssetAccessServiceAgreement";

    @JsonProperty
    public String description;

    @JsonProperty
    public String creator;


    @JsonProperty
    public AccessService.ServiceAgreementTemplate serviceAgreementTemplate;


    public AccessTemplate() {

    }


}