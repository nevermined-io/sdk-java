package io.keyko.nevermined.models.service.attributes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.CustomDateDeserializer;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.service.metadata.Algorithm;
import io.keyko.nevermined.models.service.metadata.Service;
import io.keyko.nevermined.models.service.metadata.Workflow;
import io.keyko.nevermined.models.service.types.ComputingService;

import java.util.ArrayList;
import java.util.Date;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class ServiceMain extends AbstractModel {

    @JsonProperty
    public String type;


    // Properties of Authorization Service
    @JsonProperty
    public String service;

    // Properties of Computing Service
    @JsonProperty
    public ComputingService.Provider provider;

    // Properties of AccessService
    @JsonProperty
    public String name;

    @JsonProperty
    public String creator;

//
//    @JsonProperty
//    public int timeout;

    // Properties of Metadata

    // Workflow Asset

    @JsonProperty
    public Workflow workflow;

    // Service Asset
    @JsonProperty
    public String spec;

    @JsonProperty
    public String specChecksum;

    @JsonProperty
    public Service.Definition definition;

    // Algorithm Asset
    @JsonProperty
    public Algorithm algorithm;

    // Dataset Asset
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    public Date dateCreated;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    public Date datePublished;

    @JsonProperty
    public String author;

    @JsonProperty
    public String license;

    @JsonProperty
    public ArrayList<AssetMetadata.File> files = new ArrayList<>();

    @JsonProperty
    public String encryptedService = null;

    @JsonProperty
    public String price;


}
