package io.keyko.nevermined.models.asset;

import io.keyko.common.helpers.EthereumHelper;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.FromJsonToModel;

public class OrderResult extends AbstractModel implements FromJsonToModel {

    private String serviceAgreementId;
    private String executionId;
    private Boolean accessFullfilled = false;
    private Boolean refund = false;
    private int serviceIndex = -1;

    public OrderResult(String serviceAgreementId, Boolean accessFullfilled, Boolean refund, int serviceIndex) {
        setServiceAgreementId(serviceAgreementId);
        this.accessFullfilled = accessFullfilled;
        this.refund = refund;
        this.serviceIndex = serviceIndex;
    }

    public OrderResult(String serviceAgreementId, Boolean accessFullfilled, Boolean refund) {
        this(serviceAgreementId, accessFullfilled, refund, -1);
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

    public int getServiceIndex() {
        return serviceIndex;
    }

    public void setServiceIndex(int serviceIndex) {
        this.serviceIndex = serviceIndex;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
}
