package com.oceanprotocol.squid.models.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.oceanprotocol.common.helpers.EncodingHelper;
import com.oceanprotocol.common.helpers.EthereumHelper;
import com.oceanprotocol.squid.exceptions.DIDFormatException;
import com.oceanprotocol.squid.models.AbstractModel;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.FromJsonToModel;
import org.web3j.tuples.generated.Tuple6;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class Agreement extends AbstractModel implements FromJsonToModel {
    @JsonProperty
    public DID did;

    @JsonProperty
    public String didOwner;

    @JsonProperty
    public String templateId;

    @JsonProperty
    public List<byte[]> conditions = new ArrayList<>();

    @JsonProperty
    public String lastUpdateBy;

    @JsonProperty
    public BigInteger blockNumberUpdated;

    public Agreement(DID did, String didOwner, String templateId, List<byte[]> conditions, String lastUpdateBy, BigInteger blockNumberUpdated) {
        this.did = did;
        this.didOwner = didOwner;
        this.templateId = templateId;
        this.conditions = conditions;
        this.lastUpdateBy = lastUpdateBy;
        this.blockNumberUpdated = blockNumberUpdated;
    }

    public Agreement(Tuple6 tuple6) throws DIDFormatException {
        this.did = (DID) DID.getFromHash(EthereumHelper.remove0x(EncodingHelper.toHexString((byte[]) tuple6.getValue1())));
        this.didOwner = (String) tuple6.getValue2();
        this.templateId = (String) tuple6.getValue3();
        this.conditions = (ArrayList) tuple6.getValue4();
        this.lastUpdateBy = (String) tuple6.getValue5();
        this.blockNumberUpdated = (BigInteger) tuple6.getValue6();
    }

}
