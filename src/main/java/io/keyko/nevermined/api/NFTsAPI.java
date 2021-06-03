package io.keyko.nevermined.api;

import io.keyko.nevermined.exceptions.NftException;
import io.keyko.nevermined.models.DID;

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
     * @throws NftException Unable to mint
     */
    boolean mint(DID did, BigInteger amount) throws NftException;

    /**
     * Allows a DID owner to burn NFT associated with the DID
     *
     * @param did the DID where the NFT is burned
     * @param amount the amount to burn to the NFT DID
     * @return true if everything worked
     * @throws NftException Unable to burn
     */
    boolean burn(DID did, BigInteger amount) throws NftException;

    /**
     * Allows a DID owner to transfer a specific amount of NFT associated with the DID
     *
     * @param did the DID associated to the NFT
     * @param address the receiver
     * @param amount the amount to transfer to the NFT DID
     * @return true if everything worked
     * @throws NftException Unable to transfer
     */
    boolean transfer(DID did, String address, BigInteger amount) throws NftException;

    /**
     * Gets the balance of the NFT associated to a DID
     *
     * @param address the account holding the NFT
     * @param did the DID associated to the NFT
     * @return BigInteger the address and DID/NFT balance
     * @throws NftException unable to get the balance
     */
    BigInteger balance(String address, DID did) throws NftException;


}
