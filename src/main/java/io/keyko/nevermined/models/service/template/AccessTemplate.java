package io.keyko.nevermined.models.service.template;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.FromJsonToModel;
import io.keyko.nevermined.models.service.types.AccessService;

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