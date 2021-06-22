package io.keyko.nevermined.models.service.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.nevermined.models.service.Service;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class NFTSalesService extends Service {

    @JsonIgnore
    public static final int DEFAULT_INDEX = 6;

    public NFTSalesService() {
        this.index = DEFAULT_INDEX;
        this.type= ServiceTypes.NFT_SALES.toString();

    }

    public NFTSalesService(String serviceEndpoint, int index, String templateId) {
        super(ServiceTypes.NFT_SALES, serviceEndpoint, index);
        this.type= ServiceTypes.NFT_SALES.toString();
        this.templateId = templateId;
    }


    public NFTSalesService(String serviceEndpoint, int index,
                           ServiceAgreementTemplate serviceAgreementTemplate,
                           String templateId
    ) {
        super(ServiceTypes.NFT_SALES, serviceEndpoint, index);
        this.type= ServiceTypes.NFT_SALES.toString();
        this.templateId = templateId;
        this.attributes.serviceAgreementTemplate = serviceAgreementTemplate;

    }

}