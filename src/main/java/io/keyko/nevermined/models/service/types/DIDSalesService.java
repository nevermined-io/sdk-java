package io.keyko.nevermined.models.service.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.nevermined.models.service.Service;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class DIDSalesService extends Service {

    @JsonIgnore
    public static final int DEFAULT_INDEX = 5;

    public DIDSalesService() {
        this.index = DEFAULT_INDEX;
        this.type= ServiceTypes.DID_SALES.toString();

    }

    public DIDSalesService(String serviceEndpoint, int index, String templateId) {
        super(ServiceTypes.DID_SALES, serviceEndpoint, index);
        this.type= ServiceTypes.DID_SALES.toString();
        this.templateId = templateId;
    }


    public DIDSalesService(String serviceEndpoint, int index,
                           ServiceAgreementTemplate serviceAgreementTemplate,
                           String templateId
    ) {
        super(ServiceTypes.DID_SALES, serviceEndpoint, index);
        this.type= ServiceTypes.DID_SALES.toString();
        this.templateId = templateId;
        this.attributes.serviceAgreementTemplate = serviceAgreementTemplate;

    }

}