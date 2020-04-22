package io.keyko.nevermind.models.service.attributes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.nevermind.models.AbstractModel;
import io.keyko.nevermind.models.asset.AssetMetadata;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class ServiceAdditionalInformation extends AbstractModel {

    // Dataset Asset Properties

    @JsonProperty
    public ArrayList<String> tags;

    @JsonProperty
    public ArrayList<String> categories;

    @JsonProperty
    public String description;

    @JsonProperty
    public String copyrightHolder;

    @JsonProperty
    public String workExample;

    @JsonProperty
    public ArrayList<AssetMetadata.Link> links = new ArrayList<>();

    @JsonProperty
    public String inLanguage;

}
