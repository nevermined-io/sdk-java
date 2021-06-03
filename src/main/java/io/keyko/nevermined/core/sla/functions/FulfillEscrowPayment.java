package io.keyko.nevermined.core.sla.functions;

import io.keyko.common.helpers.EncodingHelper;
import io.keyko.nevermined.contracts.EscrowPaymentCondition;
import io.keyko.nevermined.exceptions.EscrowPaymentException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;

public class FulfillEscrowPayment {
//
//    private static final Logger log = LogManager.getLogger(FulfillEscrowPayment.class);
//
//    /**
//     * Executes a fulfill function of a EscrowPaymentCondition Condition
//     *
//     * @param escrowReward       the EscrowPaymentCondition contract
//     * @param serviceAgreementId the service agreement id
//     * @param lockRewardAddress  the address of the lockPayment contract
//     * @param amounts            reward amounts to distribute
//     * @param rewardAddresses    the Addresses of the users to distribute the rewards
//     * @param lockConditionId    the id of the lock condition
//     * @param releaseConditionId the id of the release condition
//     * @return a flag that indicates if the function was executed correctly
//     * @throws EscrowPaymentException EscrowPaymentException
//     */
//    public static Boolean executeFulfill(EscrowPaymentCondition escrowReward,
//                                         String serviceAgreementId,
//                                         String lockRewardAddress,
//                                         List<BigInteger> amounts,
//                                         List<String> rewardAddresses,
//                                         String lockConditionId,
//                                         String releaseConditionId) throws EscrowPaymentException {
//
//        byte[] serviceId;
//        byte[] lockConditionIdBytes;
//        byte[] releaseConditionIdBytes;
//
//        try {
//
////            String lockRewardAddressChecksum = Keys.toChecksumAddress(lockRewardAddress);
//            serviceId = EncodingHelper.hexStringToBytes(serviceAgreementId);
//
//            lockConditionIdBytes = EncodingHelper.hexStringToBytes(lockConditionId);
//            releaseConditionIdBytes = EncodingHelper.hexStringToBytes(releaseConditionId);
//
//            TransactionReceipt receipt = escrowPayment.fulfill(
//                    serviceId,
//                    amounts,
//                    rewardAddresses,
//                    lockRewardAddress,
//                    lockConditionIdBytes,
//                    releaseConditionIdBytes
//            ).send();
//
//            if (!receipt.getStatus().equals("0x1")) {
//                String msg = "The Status received is not valid executing EscrowPaymentCondition.Fulfill: " + receipt.getStatus() + " for serviceAgreement " + serviceAgreementId;
//                log.error(msg);
//                throw new EscrowPaymentException(msg);
//            }
//
//            log.debug("EscrowPaymentCondition.Fulfill transactionReceipt OK for serviceAgreement " + serviceAgreementId);
//            return true;
//
//        } catch (Exception e) {
//
//            String msg = "Error executing EscrowPaymentCondition.Fulfill for serviceAgreement " + serviceAgreementId;
//            log.error(msg + ": " + e.getMessage());
//            throw new EscrowPaymentException(msg, e);
//        }
//
//    }
}
