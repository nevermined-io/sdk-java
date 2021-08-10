package io.keyko.nevermined.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import io.keyko.common.helpers.CryptoHelper;
import io.keyko.common.helpers.EthereumHelper;
import io.keyko.nevermined.exceptions.DIDFormatException;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Hash;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DID {

    public String did;
    public String seed;

    public static final String PREFIX = "did:nv:";

    public DID() {
        this.setEmptyDID();
    }

    public DID(String did) throws DIDFormatException {
        setDid(did);
        seed = did.replace(PREFIX, "");
    }

    public static DID builder(String hash) throws DIDFormatException {
        DID _did= new DID(
                PREFIX +
                EthereumHelper.remove0x(
                    CryptoHelper.sha3_256(hash))
        );
        _did.setDid(_did.getDid());
        _did.seed = hash;
        return _did;
    }

    public static DID builder() throws DIDFormatException {
        return new DID(generateRandomToken());
    }

    public static DID getFromHash(String hash) throws DIDFormatException {
        return new DID(PREFIX + EthereumHelper.remove0x(hash));
    }

    public static String getAssetId(String _did)    {
        return _did.replace(PREFIX, "");
    }

    public static DID getFromSeed(String seed, String address) throws UnsupportedEncodingException, DIDFormatException {
        String params = EthereumHelper.add0x(
                EthereumHelper.encodeParameterValue("bytes32", seed)
                        + TypeEncoder.encode(new Address(address)));
        final DID did = DID.getFromHash(Hash.sha3(params));
        did.seed = seed;
        return did;
    }

    @JsonValue
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