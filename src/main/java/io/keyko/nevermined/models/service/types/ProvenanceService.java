package io.keyko.nevermined.models.service.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.nevermined.models.service.Service;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class ProvenanceService extends Service {


    @JsonIgnore
    public static final int DEFAULT_INDEX = 1;

    @JsonIgnore
    public static final String DEFAULT_SERVICE = "provenance";

    public ProvenanceService() {
        this.index = DEFAULT_INDEX;
        this.type= ServiceTypes.PROVENANCE.toString();
    }

    public ProvenanceService(String serviceEndpoint, int index, String service) {
        super(ServiceTypes.PROVENANCE, serviceEndpoint, index);
        this.attributes.main.service = service;
    }

    public ProvenanceService(String serviceEndpoint, int index) {
        super(ServiceTypes.PROVENANCE, serviceEndpoint, index);
        this.attributes.main.service = DEFAULT_SERVICE;
    }

}
