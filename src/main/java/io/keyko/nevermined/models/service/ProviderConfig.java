package io.keyko.nevermined.models.service;

import org.web3j.crypto.Keys;

import java.util.ArrayList;
import java.util.List;

public class ProviderConfig {

    private String accessEndpoint;
    private String metadataEndpoint;
    private String provenanceEndpoint;
    private String secretStoreEndpoint;
    private String gatewayUrl;
    private List<String> providerAddresses = new ArrayList<>();

    public ProviderConfig(String accessEndpoint, String metadataEndpoint, String gatewayUrl, String provenanceEndpoint) {
        this.accessEndpoint = accessEndpoint;
        this.metadataEndpoint = metadataEndpoint;
        this.gatewayUrl= gatewayUrl;
        this.provenanceEndpoint= provenanceEndpoint;
    }

    public ProviderConfig(String accessEndpoint, String metadataEndpoint, String gatewayUrl, String provenanceEndpoint, String secretStoreEndpoint) {
        this(accessEndpoint, metadataEndpoint, gatewayUrl, provenanceEndpoint);
        this.secretStoreEndpoint = secretStoreEndpoint;
    }

    public ProviderConfig(String accessEndpoint, String purchaseEndpoint, String metadataEndpoint, String gatewayUrl, String provenanceEndpoint, String secretStoreEndpoint, List<String> providers) {
        this(accessEndpoint, purchaseEndpoint, metadataEndpoint, gatewayUrl, provenanceEndpoint);
        setSecretStoreEndpoint(secretStoreEndpoint);
        setProviderAddresses(providers);
    }

    public ProviderConfig(String accessEndpoint,  String metadataEndpoint, String gatewayUrl, String provenanceEndpoint, String secretStoreEndpoint, String provider) {
        this(accessEndpoint, metadataEndpoint, gatewayUrl, provenanceEndpoint, secretStoreEndpoint);
        this.addProvider(provider);
    }

    public List<String> addProvider(String providerAddress) {
        this.providerAddresses.add(Keys.toChecksumAddress(providerAddress));
        return this.providerAddresses;
    }

    public List<String> getProviderAddresses() {
        return providerAddresses;
    }

    public void setProviderAddresses(List<String> providerAddresses) {
        this.providerAddresses = providerAddresses;
    }

    public String getAccessEndpoint() {
        return accessEndpoint;
    }

    public ProviderConfig setAccessEndpoint(String accessEndpoint) {
        this.accessEndpoint = accessEndpoint;
        return this;
    }

    public String getMetadataEndpoint() {
        return metadataEndpoint;
    }

    public ProviderConfig setMetadataEndpoint(String metadataEndpoint) {
        this.metadataEndpoint = metadataEndpoint;
        return this;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public ProviderConfig setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
        return this;
    }

    public String getProvenanceEndpoint() {
        return provenanceEndpoint;
    }

    public ProviderConfig setProvenanceEndpoint(String provenanceEndpoint) {
        this.provenanceEndpoint = provenanceEndpoint;
        return this;
    }

    public String getSecretStoreEndpoint() {
        return secretStoreEndpoint;
    }

    public void setSecretStoreEndpoint(String secretStoreEndpoint) {
        this.secretStoreEndpoint = secretStoreEndpoint;
    }
}