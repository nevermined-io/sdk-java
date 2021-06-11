package io.keyko.nevermined.api.impl;

import io.keyko.nevermined.api.ConditionsAPI;
import io.keyko.nevermined.manager.ConditionsManager;
import io.keyko.nevermined.models.DID;

import java.math.BigInteger;

public class ConditionsImpl implements ConditionsAPI {

    final private ConditionsManager conditionsManager;

    /**
     * Constructor
     *
     * @param conditionsManager the conditionsManager
     */
    public ConditionsImpl(ConditionsManager conditionsManager) {

        this.conditionsManager = conditionsManager;
    }

    @Override
    public boolean lockPayment(String agreementId) throws Exception {
        return conditionsManager.lockPayment(agreementId);
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
    public boolean releaseReward(String agreementId) throws Exception {
        return conditionsManager.releaseReward(agreementId);
    }

    @Override
    public boolean refundReward(String agreementId) throws Exception {
        return conditionsManager.refundReward(agreementId);
    }

    @Override
    public boolean transferNFT(String agreementId, DID did, String grantee, BigInteger numberNFTs, String lockConditionId) throws Exception {
        return conditionsManager.transferNFT(agreementId, did, grantee, numberNFTs, lockConditionId);
    }
}
