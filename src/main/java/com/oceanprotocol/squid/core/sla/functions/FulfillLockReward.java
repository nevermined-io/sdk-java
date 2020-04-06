/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.core.sla.functions;

import com.oceanprotocol.common.helpers.EncodingHelper;
import com.oceanprotocol.keeper.contracts.LockRewardCondition;
import com.oceanprotocol.squid.exceptions.LockRewardFulfillException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

public class FulfillLockReward {

    private static final Logger log = LogManager.getLogger(FulfillLockReward.class);

    /**
     * Executes a fulfill function of a LockReward Condition
     *
     * @param lockRewardCondition LockRewardCondition contract
     * @param serviceAgreementId  the service agreement id
     * @param escrowRewardAddress the address of the EscrowReward Contract
     * @param price               price of the asset
     * @return a flag that indicates if the function was executed correctly
     * @throws LockRewardFulfillException LockRewardFulfillException
     */
    public static Boolean executeFulfill(final LockRewardCondition lockRewardCondition,
                                         final String serviceAgreementId,
                                         final String escrowRewardAddress,
                                         final String price) throws LockRewardFulfillException {

        byte[] serviceId;

        try {

            String escrowRewardAddressChecksum = Keys.toChecksumAddress(escrowRewardAddress);
            serviceId = EncodingHelper.hexStringToBytes(serviceAgreementId);

            log.debug("service Agreement String: " + serviceAgreementId);
            log.debug("serviceID Bytes:" + serviceId);
            log.debug("EscrowRewardAddress: " + escrowRewardAddressChecksum);
            log.debug("Price: " + price);

            TransactionReceipt receipt = lockRewardCondition.fulfill(
                    serviceId,
                    escrowRewardAddressChecksum,
                    new BigInteger(price)
            ).send();

            if (!receipt.getStatus().equals("0x1")) {
                String msg = "The Status received is not valid executing LockRewardCondition.Fulfill: " + receipt.getStatus() + " for serviceAgreement " + serviceAgreementId;
                log.error(msg);
                throw new LockRewardFulfillException(msg);
            }

            log.debug("LockRewardCondition.Fulfill transactionReceipt OK for serviceAgreement " + serviceAgreementId);
            return true;

        } catch (Exception e) {

            String msg = "Error executing LockRewardCondition.Fulfill for serviceAgreement " + serviceAgreementId;
            log.error(msg + ": " + e.getMessage());
            throw new LockRewardFulfillException(msg, e);
        }

    }
}
