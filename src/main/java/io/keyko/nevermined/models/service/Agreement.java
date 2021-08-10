package io.keyko.nevermined.models.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.helpers.EthereumHelper;
import io.keyko.nevermined.exceptions.DIDFormatException;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.FromJsonToModel;
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
        this.did = DID.getFromHash(EthereumHelper.remove0x(EncodingHelper.toHexString((byte[]) tuple6.component1())));
        this.didOwner = (String) tuple6.component2();
        this.templateId = (String) tuple6.component3();
        this.conditions = (ArrayList) tuple6.component4();
        this.lastUpdateBy = (String) tuple6.component5();
        this.blockNumberUpdated = (BigInteger) tuple6.component6();
    }

}
