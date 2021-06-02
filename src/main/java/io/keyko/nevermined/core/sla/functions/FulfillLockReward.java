package io.keyko.nevermined.core.sla.functions;

public class FulfillLockReward {
//
//    private static final Logger log = LogManager.getLogger(FulfillLockReward.class);
//    private static final Config config = ConfigFactory.load();
//
//    /**
//     * Executes a fulfill function of a LockReward Condition
//     *
//     * @param lockPaymentCondition LockPaymentCondition contract
//     * @param agreementId  the service agreement id
//     * @param escrowRewardAddress the address of the EscrowPaymentCondition Contract
//     * @param price               price of the asset
//     * @return a flag that indicates if the function was executed correctly
//     * @throws LockPaymentFulfillException LockPaymentFulfillException
//     */
//    public static Boolean executeFulfill(final LockPaymentCondition lockPaymentCondition,
//                                         final String agreementId,
//                                         final String escrowRewardAddress,
//                                         final String price) throws LockPaymentFulfillException, UnsupportedEncodingException {
//
//        final ConditionsManager conditionsManager = ConditionsManager.getInstance(keeperService, metadataApiService);
//        conditionsManager.lockPayment(agreementId);
//        byte[] serviceId;
//
//        final Agreement agreement = new Agreement(agreementStoreManager.getAgreement(EncodingHelper.hexStringToBytes(agreementId)).send());
//        final DDO ddo = resolveDID(agreement.did);
//        final Service service  = ddo.getServiceByTemplate(agreement.templateId);
//
//        final List<BigInteger> _amounts = service.fetchAmounts();
//        final List<String> _receivers = service.fetchReceivers();
//        final String rewardAddress = service.fetchConditionValue("_rewardAddress");
//        final String _DDOtokenAddress = service.fetchConditionValue("_tokenAddress");
//        final String contractTokenAddress = getTokenAddress(_DDOtokenAddress);
//
//        try {
//
//            String escrowRewardAddressChecksum = Keys.toChecksumAddress(escrowRewardAddress);
//            serviceId = EncodingHelper.hexStringToBytes(agreementId);
//
//            log.debug("service Agreement String: " + agreementId);
//            log.debug("serviceID Bytes:" + serviceId);
//            log.debug("EscrowPaymentConditionAddress: " + escrowRewardAddressChecksum);
//            log.debug("Price: " + price);
//
//            TransactionReceipt receipt = lockPaymentCondition.fulfill(
//                    serviceId,
//                    escrowRewardAddressChecksum,
//                    new BigInteger(price)
//            ).send();
//
//            if (!receipt.getStatus().equals("0x1")) {
//                String msg = "The Status received is not valid executing LockPaymentCondition.Fulfill: " + receipt.getStatus() + " for serviceAgreement " + agreementId;
//                log.error(msg);
//                throw new LockPaymentFulfillException(msg);
//            }
//
//            log.debug("LockPaymentCondition.Fulfill transactionReceipt OK for serviceAgreement " + agreementId);
//            return true;
//
//        } catch (Exception e) {
//
//            String msg = "Error executing LockPaymentCondition.Fulfill for serviceAgreement " + agreementId;
//            log.error(msg + ": " + e.getMessage());
//            throw new LockPaymentFulfillException(msg, e);
//        }
//
//    }
}
