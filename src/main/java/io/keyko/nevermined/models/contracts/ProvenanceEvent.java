package io.keyko.nevermined.models.contracts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.helpers.EthereumHelper;
import io.keyko.nevermined.exceptions.DIDFormatException;
import io.keyko.nevermined.models.AbstractModel;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.FromJsonToModel;
import org.web3j.tuples.generated.Tuple9;

import java.math.BigInteger;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class ProvenanceEvent extends AbstractModel implements FromJsonToModel {

    @JsonProperty
    public String provId;

    @JsonProperty
    public DID did;

    @JsonProperty
    public String agentId;

    @JsonProperty
    public String activityId;

    @JsonProperty
    public DID relatedDid;

    @JsonProperty
    public String agentInvolvedId;

    @JsonProperty
    public ProvenanceEntry.ProvenanceMethod method;

    @JsonProperty
    public String attributes;

    @JsonProperty
    public BigInteger blockNumberUpdated;

    public ProvenanceEvent(String provId, String did, String agentId, String activityId, String relatedDid, String agentInvolvedId, BigInteger method, String attributes, BigInteger blockNumberUpdated) throws DIDFormatException {
        this.provId = provId;
        this.did = DID.getFromHash(EthereumHelper.remove0x(did));
        this.agentId = agentId;
        this.activityId = activityId;
        if (null != relatedDid && !relatedDid.isEmpty())
            this.relatedDid = DID.getFromHash(EthereumHelper.remove0x(relatedDid));
        if (null != agentInvolvedId && !agentInvolvedId.isEmpty())
            this.agentInvolvedId = agentInvolvedId;
        this.method = ProvenanceEntry.ProvenanceMethod.fromValue(method);
        this.attributes = attributes;
        this.blockNumberUpdated = blockNumberUpdated;
    }

    public ProvenanceEvent(Tuple9 tuple) throws DIDFormatException {
        this(
                EncodingHelper.toHexString((byte[]) tuple.component1()),
                EncodingHelper.toHexString((byte[]) tuple.component2()),
                EncodingHelper.toHexString((byte[]) tuple.component3()),
                EncodingHelper.toHexString((byte[]) tuple.component4()),
                EncodingHelper.toHexString((byte[]) tuple.component5()),
                EncodingHelper.toHexString((byte[]) tuple.component6()),
                (BigInteger) tuple.component7(),
                EncodingHelper.toHexString((byte[]) tuple.component8()),
                (BigInteger) tuple.component9()
        );
    }

}
