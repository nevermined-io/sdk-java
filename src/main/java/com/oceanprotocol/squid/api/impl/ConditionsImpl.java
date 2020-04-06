package com.oceanprotocol.squid.api.impl;

import com.oceanprotocol.squid.api.ConditionsAPI;
import com.oceanprotocol.squid.manager.ConditionsManager;
import com.oceanprotocol.squid.models.DID;

import java.math.BigInteger;

public class ConditionsImpl implements ConditionsAPI {

    private ConditionsManager conditionsManager;


    /**
     * Constructor
     *
     * @param conditionsManager the conditionsManager
     */
    public ConditionsImpl(ConditionsManager conditionsManager) {

        this.conditionsManager = conditionsManager;
    }

    @Override
    public boolean lockReward(String agreementId, BigInteger amount) throws Exception {
        return conditionsManager.lockReward(agreementId, amount);
    }

    @Override
    public boolean grantAccess(String agreementId, DID did, String grantee) throws Exception {
        return conditionsManager.grantAccess(agreementId, did, grantee);
    }

    @Override
    public boolean grantServiceExecution(String agreementId, DID did, String grantee) throws Exception {
        return conditionsManager.grantCompute(agreementId, did, grantee);
    }

    @Override
    public boolean releaseReward(String agreementId, BigInteger amount) throws Exception {
        return conditionsManager.releaseReward(agreementId, amount);
    }

    @Override
    public boolean refundReward(String agreementId, BigInteger amount) throws Exception {
        return conditionsManager.refundReward(agreementId, amount);
    }
}
