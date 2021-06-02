package io.keyko.nevermined.models.service.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.nevermined.models.service.Service;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class ComputingService extends Service {

    @JsonIgnore
    public static final int DEFAULT_INDEX = 4;

    @JsonPropertyOrder(alphabetic = true)
    public static class Provider {

        @JsonProperty
        public String type;

        @JsonProperty
        public String description;

        @JsonProperty
        public Enviroment environment;

        @JsonPropertyOrder(alphabetic = true)
        public static class Container {

            @JsonProperty
            public String image;

            @JsonProperty
            public String tag;

            @JsonProperty
            public String checksum;

        }

        @JsonPropertyOrder(alphabetic = true)
        public static class Server {

            @JsonProperty
            public String serverId;

            @JsonProperty
            public String serverType;

            @JsonProperty
            public String price;

            @JsonProperty
            public String cpu;

            @JsonProperty
            public String gpu;

            @JsonProperty
            public String memory;

            @JsonProperty
            public String disk;

            @JsonProperty
            public Integer maxExecutionTime;

        }


        @JsonPropertyOrder(alphabetic = true)
        public static class Cluster {

            @JsonProperty
            public String type;

            @JsonProperty
            public String url;

        }


        @JsonPropertyOrder(alphabetic = true)
        public static class Enviroment {

            @JsonProperty
            public Cluster cluster;

            @JsonProperty
            public List<Container> supportedContainers = new ArrayList<>();

            @JsonProperty
            public List<Server> supportedServers = new ArrayList<>();

        }

    }

    public ComputingService() {
        this.index = DEFAULT_INDEX;
        type= ServiceTypes.COMPUTE.toString();
    }

    public ComputingService(String serviceEndpoint, int serviceDefinitionId, String templateId) {
        super(ServiceTypes.COMPUTE, serviceEndpoint, serviceDefinitionId);
        this.templateId = templateId;

    }

    public ComputingService(String serviceEndpoint, int serviceDefinitionId, ServiceAgreementTemplate serviceAgreementTemplate, String templateId) {
        super(ServiceTypes.COMPUTE, serviceEndpoint, serviceDefinitionId);
        this.templateId = templateId;
        this.attributes.serviceAgreementTemplate = serviceAgreementTemplate;
    }

}
