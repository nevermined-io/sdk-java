package io.keyko.nevermined.models.service.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.service.Service;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class MetadataService extends Service {

    @JsonIgnore
    public static final int DEFAULT_INDEX = 0;

    public MetadataService() {
        this.index = DEFAULT_INDEX;
        this.type= ServiceTypes.metadata.toString();
    }

    public MetadataService(AssetMetadata assetMetadata, String serviceEndpoint) {
        this(assetMetadata, serviceEndpoint, DEFAULT_INDEX);
    }

    public MetadataService(AssetMetadata assetMetadata, String serviceEndpoint, int serviceDefinitionId) {
        super(ServiceTypes.metadata, serviceEndpoint, serviceDefinitionId);
        this.attributes = assetMetadata.attributes;
    }

}