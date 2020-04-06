/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models.service.template;

import java.math.BigInteger;

public class TemplateSEA {

    public enum TemplateState {

        Uninitialized(BigInteger.valueOf(0)),
        Proposed(BigInteger.valueOf(1)),
        Approved(BigInteger.valueOf(2)),
        Revoked(BigInteger.valueOf(3));

        private final BigInteger status;

        TemplateState(final BigInteger newStatus) {
            status = newStatus;
        }

        public BigInteger getStatus() {
            return status;
        }
    }

    ;

    public BigInteger state;

    public String owner;

    public String lastUpdatedBy;

    public BigInteger blockNumberUpdated;

    public TemplateSEA(BigInteger state, String owner, String lastUpdatedBy, BigInteger blockNumberUpdated) {
        this.state = state;
        this.owner = owner;
        this.lastUpdatedBy = lastUpdatedBy;
        this.blockNumberUpdated = blockNumberUpdated;
    }

    @Override
    public String toString() {
        return "TemplateSEA{" +
                "state=" + state +
                ", owner='" + owner + '\'' +
                ", lastUpdatedBy='" + lastUpdatedBy + '\'' +
                ", blockNumberUpdated=" + blockNumberUpdated +
                '}';
    }
}
