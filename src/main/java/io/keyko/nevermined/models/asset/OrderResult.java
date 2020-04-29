package io.keyko.nevermined.models.asset;

import io.keyko.common.helpers.EthereumHelper;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.FromJsonToModel;

public class OrderResult extends AbstractModel implements FromJsonToModel {

    private String serviceAgreementId;
    private Boolean accessFullfilled = false;
    private Boolean refund = false;

    public OrderResult(String serviceAgreementId, Boolean accessFullfilled, Boolean refund) {

        setServiceAgreementId(serviceAgreementId);
        this.accessFullfilled = accessFullfilled;
        this.refund = refund;
    }

    public String getServiceAgreementId() {
        return serviceAgreementId;
    }

    public void setServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = EthereumHelper.remove0x(serviceAgreementId);
    }

    public Boolean isAccessGranted() {
        return accessFullfilled;
    }

    public void setAccessFullfilled(Boolean accessFullfilled) {
        this.accessFullfilled = accessFullfilled;
    }

    public Boolean isRefund() {
        return refund;
    }

    public void setRefund(Boolean refund) {
        this.refund = refund;
    }
}
