package io.keyko.nevermined.models.asset;


import com.fasterxml.jackson.annotation.*;
import io.keyko.common.helpers.CryptoHelper;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.service.Service;
import io.keyko.nevermined.models.service.attributes.ServiceAdditionalInformation;
import io.keyko.nevermined.models.service.attributes.ServiceCuration;
import io.keyko.nevermined.models.service.attributes.ServiceMain;

import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class AssetMetadata extends AbstractModel {

    @JsonProperty
    public Service.Attributes attributes;

    public static AssetMetadata builder() {
        AssetMetadata assetMetadata = new AssetMetadata();
        assetMetadata.attributes = new Service.Attributes();
        assetMetadata.attributes.main = new ServiceMain();
        assetMetadata.attributes.additionalInformation = new ServiceAdditionalInformation();
        assetMetadata.attributes.curation = new ServiceCuration();
        return assetMetadata;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class Link {

        @JsonProperty
        public String name;

        @JsonProperty
        public String type;

        @JsonProperty
        public String url;

        public Link() {
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class File {

        @JsonProperty//(access = JsonProperty.Access.READ_ONLY)
        public String url;

        @JsonProperty
        public Integer index;

        @JsonProperty
        public String name;

        @JsonProperty
        public String contentType;

        @JsonProperty
        public String checksum;

        @JsonProperty
        public String checksumType;

        @JsonProperty
        public String contentLength;

        @JsonProperty
        public String encoding;

        @JsonProperty
        public String compression;

        @JsonProperty
        public String resourceId;

        @JsonSetter("url")
        public void setUrl(String url) {
            this.name = new java.io.File(url).getName();
            this.url = url;
        }


        public File() {
        }
    }


    // TODO Remove
    public String generateMetadataChecksum(String did) {

        String concatFields = this.attributes.main.files.stream()
                .map(file -> file.checksum != null ? file.checksum : "")
                .collect(Collectors.joining(""))
                .concat(this.attributes.main.name)
                .concat(this.attributes.main.author)
                .concat(this.attributes.main.license)
                .concat(did);
        return "0x" + CryptoHelper.sha3256(concatFields);


    }

    // TODO Remove
    public AssetMetadata eraseFileUrls() {
        this.attributes.main.files.forEach(f -> {
            f.url = null;
        });

        return this;
    }


}
