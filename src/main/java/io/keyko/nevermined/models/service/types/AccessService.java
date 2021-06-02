package io.keyko.nevermined.models.service.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.nevermined.models.service.Service;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class AccessService extends Service {

    @JsonIgnore
    public static final int DEFAULT_INDEX = 3;

    public AccessService() {
        this.index = DEFAULT_INDEX;
        this.type= ServiceTypes.ACCESS.toString();

    }

    public AccessService(String serviceEndpoint, int index, String templateId) {
        super(ServiceTypes.ACCESS, serviceEndpoint, index);
        this.type= ServiceTypes.ACCESS.toString();
        this.templateId = templateId;
    }


    public AccessService(String serviceEndpoint, int index,
                         ServiceAgreementTemplate serviceAgreementTemplate,
                         String templateId
    ) {
        super(ServiceTypes.ACCESS, serviceEndpoint, index);
        this.type= ServiceTypes.ACCESS.toString();
        this.templateId = templateId;
        this.attributes.serviceAgreementTemplate = serviceAgreementTemplate;

    }

}