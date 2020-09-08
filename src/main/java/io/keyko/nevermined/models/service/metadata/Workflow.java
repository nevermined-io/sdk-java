package io.keyko.nevermined.models.service.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.nevermined.exceptions.DIDFormatException;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.AssetMetadata;
import jnr.ffi.annotations.In;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class Workflow {

    @JsonProperty
    public List<Workflow.Stage> stages;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class Stage {

        @JsonProperty
        public Integer index;

        @JsonProperty
        public String stageType;

        @JsonProperty
        public Requirements requirements;

        @JsonProperty
        public List<Input> input;

        @JsonProperty
        public Transformation transformation;

        @JsonProperty
        public Output output;

        public static List<Input> parseInputs(String stringInputs)    {
            final String[] inputs = stringInputs.split(",");
            List listInputs = new ArrayList();
            for (String entry: inputs)  {
                try {
                    Input input = new Input();
                    input.id = new DID(entry);
                    listInputs.add(input);
                } catch (DIDFormatException e) {
                }
            }
            return listInputs;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class Requirements {

        @JsonProperty
        public Container container;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class Container {

        @JsonProperty
        public String image;

        @JsonProperty
        public String tag;

        @JsonProperty
        public String checksum;

        public static Container parseString(String containerLine)   {
            final String[] containerTokens = containerLine.split(":");
            Container container = new Container();
            container.image = containerTokens[0];
            if (containerTokens.length>1)
                container.tag = containerTokens[1];
            else
                container.tag = "latest";
            return container;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class Input {

        @JsonProperty
        public Integer index;

        @JsonProperty
        public DID id;



    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class Transformation {

        @JsonProperty
        public DID id;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class Output {

        @JsonProperty
        public String metadataUrl;

        @JsonProperty
        public String secretStoreUrl;

        @JsonProperty
        public String accessProxyUrl;

        @JsonProperty
        public AssetMetadata metadata;

    }

}
