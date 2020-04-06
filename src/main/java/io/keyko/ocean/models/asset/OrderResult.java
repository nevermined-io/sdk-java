/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package io.keyko.ocean.models.asset;

import io.keyko.ocean.models.AbstractModel;
import io.keyko.ocean.models.FromJsonToModel;

public class OrderResult extends AbstractModel implements FromJsonToModel {

    private String serviceAgreementId;
    private Boolean accessFullfilled = false;
    private Boolean refund = false;

    public OrderResult(String serviceAgreementId, Boolean accessFullfilled, Boolean refund) {

        this.serviceAgreementId = serviceAgreementId;
        this.accessFullfilled = accessFullfilled;
        this.refund = refund;
    }

    public String getServiceAgreementId() {
        return serviceAgreementId;
    }

    public void setServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
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
