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
public class ProvenanceEntry extends AbstractModel implements FromJsonToModel {

    public enum ProvenanceMethod {

        ENTITY(BigInteger.valueOf(0)),
        ACTIVITY(BigInteger.valueOf(1)),
        WAS_GENERATED_BY(BigInteger.valueOf(2)),
        USED(BigInteger.valueOf(3)),
        WAS_INFORMED_BY(BigInteger.valueOf(4)),
        WAS_STARTED_BY(BigInteger.valueOf(5)),
        WAS_ENDED_BY(BigInteger.valueOf(6)),
        WAS_INVALIDATED_BY(BigInteger.valueOf(7)),
        WAS_DERIVED_FROM(BigInteger.valueOf(8)),
        AGENT(BigInteger.valueOf(9)),
        WAS_ATTRIBUTED_TO(BigInteger.valueOf(10)),
        WAS_ASSOCIATED_WITH(BigInteger.valueOf(11)),
        ACTED_ON_BEHALF(BigInteger.valueOf(12));

        private final BigInteger method;

        ProvenanceMethod(final BigInteger newMethod) {
            method = newMethod;
        }
        static ProvenanceMethod fromValue(final BigInteger value)    {
            if (value.equals(BigInteger.valueOf(0))) return ENTITY;
            if (value.equals(BigInteger.valueOf(1))) return ACTIVITY;
            if (value.equals(BigInteger.valueOf(2))) return WAS_GENERATED_BY;
            if (value.equals(BigInteger.valueOf(3))) return USED;
            if (value.equals(BigInteger.valueOf(4))) return WAS_INFORMED_BY;
            if (value.equals(BigInteger.valueOf(5))) return WAS_STARTED_BY;
            if (value.equals(BigInteger.valueOf(6))) return WAS_ENDED_BY;
            if (value.equals(BigInteger.valueOf(7))) return WAS_INVALIDATED_BY;
            if (value.equals(BigInteger.valueOf(8))) return WAS_DERIVED_FROM;
            if (value.equals(BigInteger.valueOf(9))) return AGENT;
            if (value.equals(BigInteger.valueOf(10))) return WAS_ATTRIBUTED_TO;
            if (value.equals(BigInteger.valueOf(11))) return WAS_ASSOCIATED_WITH;
            if (value.equals(BigInteger.valueOf(12))) return ACTED_ON_BEHALF;
            return ENTITY;
        }
        public BigInteger getMethod() {
            return method;
        }
    }

    @JsonProperty
    public DID did;

    @JsonProperty
    public DID relatedDid;

    @JsonProperty
    public String agentId;

    @JsonProperty
    public String activityId;

    @JsonProperty
    public String agentInvolvedId;

    @JsonProperty
    public ProvenanceMethod method;

    @JsonProperty
    public String createdBy;

    @JsonProperty
    public BigInteger blockNumberUpdated;

    @JsonProperty
    public String signature;

    public ProvenanceEntry(String did, String relatedDid, String agentId, String activityId, String agentInvolvedId, BigInteger method, String createdBy, BigInteger blockNumberUpdated, String signature) throws DIDFormatException {
        this.did = DID.getFromHash(EthereumHelper.remove0x(did));
        this.relatedDid = DID.getFromHash(EthereumHelper.remove0x(relatedDid));
        this.agentId = agentId;
        this.activityId = activityId;
        this.agentInvolvedId = agentInvolvedId;
        this.method = ProvenanceMethod.fromValue(method);
        this.createdBy = createdBy;
        this.blockNumberUpdated = blockNumberUpdated;
        this.signature = signature;
    }

    public ProvenanceEntry(Tuple9 tuple) throws DIDFormatException {
        this(
            EncodingHelper.toHexString((byte[]) tuple.component1()),
            EncodingHelper.toHexString((byte[]) tuple.component2()),
            (String) tuple.component3(),
            EncodingHelper.toHexString((byte[]) tuple.component4()),
            (String) tuple.component5(),
            (BigInteger) tuple.component6(),
            (String) tuple.component7(),
            (BigInteger) tuple.component8(),
            EncodingHelper.toHexString((byte[]) tuple.component9())
        );
    }

}
