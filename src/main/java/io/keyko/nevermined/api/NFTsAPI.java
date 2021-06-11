package io.keyko.nevermined.api;

import io.keyko.nevermined.exceptions.EscrowPaymentException;
import io.keyko.nevermined.exceptions.NFTException;
import io.keyko.nevermined.exceptions.OrderException;
import io.keyko.nevermined.exceptions.ServiceException;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.OrderResult;

import java.math.BigInteger;

/**
 * Exposes the Public API related with NFTs
 */
public interface NFTsAPI {

    /**
     * Allows a DID owner to mint a NFT associated with the DID
     *
     * @param did the DID where the NFT is minted
     * @param amount the amount to mint to the NFT DID
     * @return true if everything worked
     * @throws NFTException Unable to mint
     */
    boolean mint(DID did, BigInteger amount) throws NFTException;

    /**
     * Allows a DID owner to burn NFT associated with the DID
     *
     * @param did the DID where the NFT is burned
     * @param amount the amount to burn to the NFT DID
     * @return true if everything worked
     * @throws NFTException Unable to burn
     */
    boolean burn(DID did, BigInteger amount) throws NFTException;

    /**
     * Allows a DID owner to transfer a specific amount of NFT associated with the DID
     *
     * @param did the DID associated to the NFT
     * @param address the receiver
     * @param amount the amount to transfer to the NFT DID
     * @return true if everything worked
     * @throws NFTException Unable to transfer
     */
    boolean transfer(DID did, String address, BigInteger amount) throws NFTException;

    /**
     * Gets the balance of the NFT associated to a DID
     *
     * @param address the account holding the NFT
     * @param did the DID associated to the NFT
     * @return BigInteger the address and DID/NFT balance
     * @throws NFTException unable to get the balance
     */
    BigInteger balance(String address, DID did) throws NFTException;

    /**
     * Purchases a NFT associated to a DID. It implies to initialize a Service Agreement between publisher and consumer
     *
     * @param did the decentralized identifier of the asset with the NFTs attached
     * @param serviceIndex the service index
     * @return OrderResult
     * @throws OrderException OrderException
     * @throws ServiceException ServiceException
     * @throws EscrowPaymentException EscrowPaymentException
     */
    OrderResult order(DID did, int serviceIndex) throws OrderException, ServiceException, EscrowPaymentException;
}
