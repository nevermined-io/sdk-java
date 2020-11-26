package io.keyko.nevermined.manager;

import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.exceptions.DIDRegisterException;
import io.keyko.nevermined.exceptions.EthereumException;
import io.keyko.nevermined.exceptions.ProvenanceException;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.contracts.ProvenanceEntry;
import io.keyko.nevermined.models.contracts.ProvenanceEvent;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple9;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the functionality related with Assets Provenance
 */
public class ProvenanceManager extends BaseManager {

    public ProvenanceManager(KeeperService keeperService) {
        super(keeperService, null);
    }

    /**
     * Gets an instance of AssetManager
     *
     * @param keeperService   instance of keeperService
     * @return an initialized instance of AssetManager
     */
    public static ProvenanceManager getInstance(KeeperService keeperService) {
        return new ProvenanceManager(keeperService);
    }


    public boolean used(String provenanceId, DID did, String agentId, String activityId, String signature, String attributes)
            throws ProvenanceException {

        try {
            TransactionReceipt receipt = didRegistry.used(
                    EncodingHelper.hexStringToBytes(provenanceId.replace("0x", "")),
                    EncodingHelper.hexStringToBytes(did.getHash()),
                    agentId,
                    EncodingHelper.hexStringToBytes(activityId),
                    EncodingHelper.hexStringToBytes(signature),
                    attributes
            ).send();
            return receipt.getStatus().equals("0x1");

        } catch (Exception e) {
            throw new ProvenanceException("Error in Provenance used method ", e);
        }
    }

    public boolean wasDerivedFrom(String provenanceId, DID newEntityDid, DID usedEntityDid, String agentId, String activityId, String attributes)
        throws ProvenanceException {

        try {
            TransactionReceipt receipt = didRegistry.wasDerivedFrom(
                    EncodingHelper.hexStringToBytes(provenanceId.replace("0x", "")),
                    EncodingHelper.hexStringToBytes(newEntityDid.getHash()),
                    EncodingHelper.hexStringToBytes(usedEntityDid.getHash()),
                    agentId,
                    EncodingHelper.hexStringToBytes(activityId),
                    attributes
            ).send();
            return receipt.getStatus().equals("0x1");

        } catch (Exception e) {
            throw new ProvenanceException("Error in Provenance wasDerivedFrom method ", e);
        }
    }

    public boolean wasAssociatedWith(String provenanceId, DID did, String agentId, String activityId, String attributes)
        throws ProvenanceException {

        try {
            TransactionReceipt receipt = didRegistry.wasAssociatedWith(
                    EncodingHelper.hexStringToBytes(provenanceId.replace("0x", "")),
                    EncodingHelper.hexStringToBytes(did.getHash()),
                    agentId,
                    EncodingHelper.hexStringToBytes(activityId),
                    attributes
            ).send();
            return receipt.getStatus().equals("0x1");

        } catch (Exception e) {
            throw new ProvenanceException("Error in Provenance wasAssociatedWith method ", e);
        }
    }

    public boolean actedOnBehalf(String provenanceId, DID did, String delegateAgentId, String responsibleAgentId, String activityId, String signature, String attributes)
        throws ProvenanceException {

        try {
            TransactionReceipt receipt = didRegistry.actedOnBehalf(
                    EncodingHelper.hexStringToBytes(provenanceId.replace("0x", "")),
                    EncodingHelper.hexStringToBytes(did.getHash()),
                    delegateAgentId,
                    responsibleAgentId,
                    EncodingHelper.hexStringToBytes(activityId),
                    EncodingHelper.hexStringToBytes(signature),
                    attributes
            ).send();
            return receipt.getStatus().equals("0x1");

        } catch (Exception e) {
            throw new ProvenanceException("Error in Provenance actedOnBehalf method ", e);
        }
    }

    public boolean provenanceSignatureIsCorrect(String delegateAgentId, String provenanceId, String signature)
            throws ProvenanceException {

        try {
            final Boolean result = didRegistry.provenanceSignatureIsCorrect(
                    delegateAgentId,
                    EncodingHelper.hexStringToBytes(provenanceId.replace("0x", "")),
                    EncodingHelper.hexStringToBytes(signature)
            ).send();
            return result.booleanValue();

        } catch (Exception e) {
            throw new ProvenanceException("Error in Provenance actedOnBehalf method ", e);
        }
    }

    // Manage delegates
    public boolean isProvenanceDelegate(DID did, String delegate)
            throws ProvenanceException {

        try {
            return didRegistry.isProvenanceDelegate(
                    EncodingHelper.hexStringToBytes(did.getHash()),
                    delegate
            ).send();

        } catch (Exception e) {
            throw new ProvenanceException("Error in Provenance isProvenanceDelegate method ", e);
        }
    }

    public boolean addDIDProvenanceDelegate(DID did, String delegate)
            throws ProvenanceException {

        try {
            final TransactionReceipt receipt = didRegistry.addDIDProvenanceDelegate(
                    EncodingHelper.hexStringToBytes(did.getHash()),
                    delegate
            ).send();
            return receipt.getStatus().equals("0x1");
        } catch (Exception e) {
            throw new ProvenanceException("Error in Provenance addDIDProvenanceDelegate method ", e);
        }
    }

    public boolean removeDIDProvenanceDelegate(DID did, String delegate)
            throws ProvenanceException {

        try {
            final TransactionReceipt receipt = didRegistry.removeDIDProvenanceDelegate(
                    EncodingHelper.hexStringToBytes(did.getHash()),
                    delegate
            ).send();
            return receipt.getStatus().equals("0x1");
        } catch (Exception e) {
            throw new ProvenanceException("Error in Provenance removeDIDProvenanceDelegate method ", e);
        }
    }

    public String getProvenanceOwner(String provenanceId)
            throws ProvenanceException {

        try {
            return didRegistry.getProvenanceOwner(
                    EncodingHelper.hexStringToBytes(provenanceId.replace("0x", ""))
            ).send();

        } catch (Exception e) {
            throw new ProvenanceException("Error in Provenance getProvenanceOwner method ", e);
        }
    }

    // Fetch Provenance information
    public ProvenanceEntry getProvenanceEntry(String provenanceId)
            throws ProvenanceException {

        try {
            final Tuple9<byte[], byte[], String, byte[], String, BigInteger, String, BigInteger, byte[]> provenanceTuple =
                    didRegistry.getProvenanceEntry(
                        EncodingHelper.hexStringToBytes(provenanceId.replace("0x", ""))
            ).send();
            return new ProvenanceEntry(provenanceTuple);

        } catch (Exception e) {
            throw new ProvenanceException("Error in Provenance getProvenanceEntry method ", e);
        }
    }


    // Events
    public List<ProvenanceEvent> getDIDProvenanceEvents(DID did) throws ProvenanceException {

        List<ProvenanceEvent> provenanceEvents = new ArrayList<>();
        EthFilter didFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                didRegistry.getContractAddress()
        );

        try {
            String didTopic = "0x" + did.getHash();
            final Event event = didRegistry.PROVENANCEATTRIBUTEREGISTERED_EVENT;
            final String eventSignature = EventEncoder.encode(event);
            didFilter.addSingleTopic(eventSignature);
            didFilter.addNullTopic();
            didFilter.addOptionalTopics(didTopic);
            EthLog ethLog;

            try {
                ethLog = getKeeperService().getWeb3().ethGetLogs(didFilter).send();
            } catch (IOException e) {
                throw new EthereumException("Error searching DID " + did.toString() + " onchain: " + e.getMessage());
            }

            for (EthLog.LogResult logResult: ethLog.getLogs())  {
                List<Type> indexedValues = FunctionReturnDecoder.decode(((EthLog.LogObject) logResult).getData(), event.getIndexedParameters());
                List<Type> nonIndexedValues = FunctionReturnDecoder.decode(((EthLog.LogObject) logResult).getData(), event.getNonIndexedParameters());
                provenanceEvents.add(
                        new ProvenanceEvent(
                            indexedValues.get(0).toString(),
                            indexedValues.get(1).toString(),
                            indexedValues.get(2).toString(),
                            nonIndexedValues.get(0).toString(),
                            nonIndexedValues.get(1).toString(),
                            nonIndexedValues.get(2).toString(),
                            (BigInteger) nonIndexedValues.get(3).getValue(),
                            nonIndexedValues.get(4).toString(),
                            (BigInteger) nonIndexedValues.get(5).getValue()
                        )
                );
            }
        } catch (Exception e)   {
            log.error("Error searching for provenance event");
            throw new ProvenanceException(e.getMessage());
        }
        return provenanceEvents;
    }

    // Events
    public List<ProvenanceEvent> getProvenanceMethodEvents(ProvenanceEntry.ProvenanceMethod method, DID did) throws ProvenanceException {

        List<ProvenanceEvent> provenanceEvents = new ArrayList<>();
        EthFilter didFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                didRegistry.getContractAddress()
        );

        try {
            Event event;
            String didTopic = "0x" + did.getHash();
            if (method.equals(ProvenanceEntry.ProvenanceMethod.WAS_GENERATED_BY))
                event = didRegistry.WASGENERATEDBY_EVENT;
            else if (method.equals(ProvenanceEntry.ProvenanceMethod.USED))
                event = didRegistry.USED_EVENT;
            else if (method.equals(ProvenanceEntry.ProvenanceMethod.WAS_DERIVED_FROM))
                event = didRegistry.WASDERIVEDFROM_EVENT;
            else if (method.equals(ProvenanceEntry.ProvenanceMethod.WAS_ASSOCIATED_WITH))
                event = didRegistry.WASASSOCIATEDWITH_EVENT;
            else if (method.equals(ProvenanceEntry.ProvenanceMethod.ACTED_ON_BEHALF))
                event = didRegistry.ACTEDONBEHALF_EVENT;
            else
                throw new ProvenanceException("ProvenanceMethod provided not supported: " + method.toString());

            final String eventSignature = EventEncoder.encode(event);
            didFilter.addSingleTopic(eventSignature);

            didFilter.addOptionalTopics(didTopic);
            EthLog ethLog;

            try {
                ethLog = getKeeperService().getWeb3().ethGetLogs(didFilter).send();
            } catch (IOException e) {
                throw new EthereumException("Error searching DID " + did.toString() + " onchain: " + e.getMessage());
            }

            for (EthLog.LogResult logResult: ethLog.getLogs())  {
                List<Type> indexedValues = FunctionReturnDecoder.decode(((EthLog.LogObject) logResult).getData(), event.getIndexedParameters());
                List<Type> nonIndexedValues = FunctionReturnDecoder.decode(((EthLog.LogObject) logResult).getData(), event.getNonIndexedParameters());

                if (method.equals(ProvenanceEntry.ProvenanceMethod.WAS_GENERATED_BY) ||
                    method.equals(ProvenanceEntry.ProvenanceMethod.USED) ||
                    method.equals(ProvenanceEntry.ProvenanceMethod.WAS_ASSOCIATED_WITH))    {

                    provenanceEvents.add(
                            new ProvenanceEvent(
                                    nonIndexedValues.get(0).toString(), // provId
                                    indexedValues.get(0).toString(), // did
                                    indexedValues.get(1).toString(), // agentId
                                    indexedValues.get(2).toString(), // activityId
                                    null, // relatedDid
                                    null, // agentInvolvedId
                                    method.getMethod(), // method
                                    nonIndexedValues.get(1).toString(), // attributes
                                    (BigInteger) nonIndexedValues.get(2).getValue() // blockNumberUpdate
                            )
                    );
                }   else if (method.equals(ProvenanceEntry.ProvenanceMethod.WAS_DERIVED_FROM))  {

                    provenanceEvents.add(
                            new ProvenanceEvent(
                                    nonIndexedValues.get(1).toString(), // provId
                                    indexedValues.get(0).toString(), // did
                                    indexedValues.get(2).toString(), // agentId
                                    nonIndexedValues.get(0).toString(), // activityId
                                    indexedValues.get(1).toString(), // relatedDid
                                    null, // agentInvolvedId
                                    method.getMethod(), // method
                                    nonIndexedValues.get(2).toString(), // attributes
                                    (BigInteger) nonIndexedValues.get(3).getValue() // blockNumberUpdate
                            )
                    );
                }   else { // Acted on Behalf

                    provenanceEvents.add(
                            new ProvenanceEvent(
                                    nonIndexedValues.get(0).toString(), // provId
                                    indexedValues.get(0).toString(), // did
                                    indexedValues.get(1).toString(), // agentId
                                    indexedValues.get(2).toString(), // activityId
                                    null, // relatedDid
                                    null, // agentInvolvedId
                                    method.getMethod(), // method
                                    nonIndexedValues.get(2).toString(), // attributes
                                    (BigInteger) nonIndexedValues.get(3).getValue() // blockNumberUpdate
                            )
                    );
                }

            }
        } catch (Exception e)   {
            log.error("Error searching for provenance event");
            throw new ProvenanceException(e.getMessage());
        }
        return provenanceEvents;
    }


}
