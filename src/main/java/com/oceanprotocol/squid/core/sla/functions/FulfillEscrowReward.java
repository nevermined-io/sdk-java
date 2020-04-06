/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.core.sla.functions;

import com.oceanprotocol.common.helpers.EncodingHelper;
import com.oceanprotocol.keeper.contracts.EscrowReward;
import com.oceanprotocol.squid.exceptions.EscrowRewardException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

public class FulfillEscrowReward {

    private static final Logger log = LogManager.getLogger(FulfillEscrowReward.class);

    /**
     * Executes a fulfill function of a EscrowReward Condition
     *
     * @param escrowReward       the EscrowReward contract
     * @param serviceAgreementId the service agreement id
     * @param lockRewardAddress  the address of the lockReward contract
     * @param price          price of the asset
     * @param consumerAddress    the Address of the consumer
     * @param lockConditionId    the id of the lock condition
     * @param releaseConditionId the id of the release condition
     * @return a flag that indicates if the function was executed correctly
     * @throws EscrowRewardException EscrowRewardException
     */
    public static Boolean executeFulfill(EscrowReward escrowReward,
                                         String serviceAgreementId,
                                         String lockRewardAddress,
                                         String price,
                                         String consumerAddress,
                                         String lockConditionId,
                                         String releaseConditionId) throws EscrowRewardException {

        byte[] serviceId;
        byte[] lockConditionIdBytes;
        byte[] releaseConditionIdBytes;

        try {

            String lockRewardAddressChecksum = Keys.toChecksumAddress(lockRewardAddress);
            serviceId = EncodingHelper.hexStringToBytes(serviceAgreementId);

            lockConditionIdBytes = EncodingHelper.hexStringToBytes(lockConditionId);
            releaseConditionIdBytes = EncodingHelper.hexStringToBytes(releaseConditionId);

            TransactionReceipt receipt = escrowReward.fulfill(
                    serviceId,
                    new BigInteger(price),
                    lockRewardAddressChecksum,
                    consumerAddress,
                    lockConditionIdBytes,
                    releaseConditionIdBytes
            ).send();

            if (!receipt.getStatus().equals("0x1")) {
                String msg = "The Status received is not valid executing EscrowReward.Fulfill: " + receipt.getStatus() + " for serviceAgreement " + serviceAgreementId;
                log.error(msg);
                throw new EscrowRewardException(msg);
            }

            log.debug("EscrowReward.Fulfill transactionReceipt OK for serviceAgreement " + serviceAgreementId);
            return true;

        } catch (Exception e) {

            String msg = "Error executing EscrowReward.Fulfill for serviceAgreement " + serviceAgreementId;
            log.error(msg + ": " + e.getMessage());
            throw new EscrowRewardException(msg, e);
        }

    }
}
