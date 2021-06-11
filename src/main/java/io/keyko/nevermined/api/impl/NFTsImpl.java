package io.keyko.nevermined.api.impl;

import io.keyko.nevermined.api.NFTsAPI;
import io.keyko.nevermined.exceptions.EscrowPaymentException;
import io.keyko.nevermined.exceptions.NFTException;
import io.keyko.nevermined.exceptions.OrderException;
import io.keyko.nevermined.exceptions.ServiceException;
import io.keyko.nevermined.manager.AssetsManager;
import io.keyko.nevermined.manager.NeverminedManager;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.OrderResult;

import java.math.BigInteger;

/**
 * Implementation of NFTsAPI
 */
public class NFTsImpl implements NFTsAPI {

    private NeverminedManager neverminedManager;
    private AssetsManager assetsManager;


    /**
     * Constructor
     *
     * @param neverminedManager  the neverminedManager
     * @param assetsManager the assetsManager
     */
    public NFTsImpl(NeverminedManager neverminedManager, AssetsManager assetsManager) {

        this.neverminedManager = neverminedManager;
        this.assetsManager = assetsManager;
    }

    @Override
    public boolean mint(DID did, BigInteger amount) throws NFTException {
        return assetsManager.mint(did, amount);
    }

    @Override
    public boolean burn(DID did, BigInteger amount) throws NFTException {
        return assetsManager.burn(did, amount);
    }

    @Override
    public boolean transfer(DID did, String address, BigInteger amount) throws NFTException {
        return assetsManager.transfer(did, address, amount);
    }

    @Override
    public BigInteger balance(String address, DID did) throws NFTException {
        return assetsManager.balance(address, did);
    }

    @Override
    public OrderResult order(DID did, int serviceIndex) throws OrderException, ServiceException, EscrowPaymentException {
        return neverminedManager.orderNFT(did, serviceIndex);
    }
}
