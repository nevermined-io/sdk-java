package io.keyko.nevermined.api;

import io.keyko.nevermined.models.DID;

import java.math.BigInteger;
import java.util.List;

/**
 * Exposes the Public API related with the management of ConditionStatusMap
 */
public interface ConditionsAPI {

    /**
     * Lock the amount of token that are going to be paid for the asset.
     *
     * @param agreementId the agreement id
     * @return true if the execution was good
     * @throws Exception Exception
     */
    boolean lockPayment(String agreementId) throws Exception;

    /**
     * Grant access to an address using the parity secret store.
     *
     * @param agreementId the agreement id
     * @param did         the did
     * @param grantee     the address that is going to be granted to access the data.
     * @return a flag if the execution was good
     * @throws Exception Exception
     */
    boolean grantAccess(String agreementId, DID did, String grantee) throws Exception;

    /**
     * Grant compute to an address using the parity secret store.
     *
     * @param agreementId the agreement id
     * @param did         the did
     * @param grantee     the address that is going to be granted to execute the compute service.
     * @return a flag if the execution was good
     * @throws Exception Exception
     */
    boolean grantServiceExecution(String agreementId, DID did, String grantee) throws Exception;

    /**
     * Release the payment to the data publisher and/or provider addresses
     *
     * @param agreementId the agreement id.
     * @return true if the execution was good
     * @throws Exception Exception
     */
    boolean releaseReward(String agreementId) throws Exception;

    /**
     * Refund the payment to the consumer.
     *
     * @param agreementId the agreement id.
     * @return a flag if the execution was good
     * @throws Exception Exception
     */
    boolean refundReward(String agreementId) throws Exception;

    /**
     * Transfer a NFT to a receiver using the NFT_SALES flow of the Service Agreements
     *
     * @param agreementId    the agreement id.
     * @param did            the DID of the asset
     * @param granteeAddress the public address to receive the nfts
     * @param numberNFTs     the number of NFTs to transfer
     * @param lockConditionId the identifier of the condition locking the funds
     * @return a flag true if was executed successfully.
     * @throws Exception exception
     */
    boolean transferNFT(String agreementId, DID did, String granteeAddress, BigInteger numberNFTs, String lockConditionId) throws Exception;

    /**
     * Transfer the DID Ownership to a receiver using the DID_SALES flow of the Service Agreements
     *
     * @param agreementId    the agreement id.
     * @param did            the DID of the asset
     * @param granteeAddress the public address to receive the DID Ownership
     * @return a flag true if was executed successfully.
     * @throws Exception exception
     */
    boolean transferDIDOwnership(String agreementId, DID did, String granteeAddress) throws Exception;

}
