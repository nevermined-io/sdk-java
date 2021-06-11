package io.keyko.nevermined.models.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.common.helpers.EncodingHelper;
import io.keyko.nevermined.exceptions.DDOException;
import io.keyko.nevermined.exceptions.NeverminedRuntimeException;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.AssetRewards;
import io.keyko.nevermined.models.FromJsonToModel;
import io.keyko.nevermined.models.service.attributes.ServiceAdditionalInformation;
import io.keyko.nevermined.models.service.attributes.ServiceCuration;
import io.keyko.nevermined.models.service.attributes.ServiceMain;
import org.web3j.crypto.Keys;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@JsonPropertyOrder(alphabetic = true)
public class ServiceDescriptor extends AbstractModel implements FromJsonToModel {

    @JsonProperty
    public Service service;

    @JsonProperty
    public AssetRewards assetRewards;


    public ServiceDescriptor() {
    }

    public ServiceDescriptor(Service service, AssetRewards assetRewards) {
        this.service = service;
        this.assetRewards = assetRewards;
    }

    public static ServiceDescriptor fetchServiceByType(List<ServiceDescriptor> serviceDescriptors, Service.ServiceTypes serviceType) {
        try {
            return serviceDescriptors.stream()
                    .filter(sd -> sd.service.type.equals(serviceType.toString()))
                    .findFirst()
                    .orElseThrow();
        } catch (Exception e)   {
            return null;
        }
    }

}