/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oceanprotocol.common.helpers.CryptoHelper;
import com.oceanprotocol.common.helpers.EthereumHelper;
import com.oceanprotocol.squid.exceptions.DIDFormatException;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DID {

    @JsonProperty
    public String did;

    public static final String PREFIX = "did:op:";

    public DID() {
        this.setEmptyDID();
    }

    public DID(String did) throws DIDFormatException {
        setDid(did);
    }

    public static DID builder(String seed) throws DIDFormatException {
        DID _did= new DID(
                PREFIX +
                EthereumHelper.remove0x(
                    CryptoHelper.sha3_256(seed))
        );
        _did.setDid(_did.getDid());
        return _did;
    }

    public static DID builder() throws DIDFormatException {
        return new DID(generateRandomToken());
    }

    public static DID getFromHash(String hash) throws DIDFormatException {
        return new DID(PREFIX + hash);
    }

    public String getDid() {
        return did;
    }

    public String getHash() {
        return did.substring(PREFIX.length());
    }

    public static String generateRandomToken() {
        String token = PREFIX + UUID.randomUUID().toString()
                + UUID.randomUUID().toString();
        return token.replaceAll("-", "");

    }

    @Override
    public String toString() {
        return did;
    }

    public DID setEmptyDID() {
        this.did = "";
        return this;
    }

    public DID setDid(String did) throws DIDFormatException {
        if (!did.startsWith(PREFIX))
            throw new DIDFormatException("Invalid DID Format, it should starts by " + PREFIX);
        this.did = did;
        return this;
    }


}