package io.keyko.nevermined.api;

import io.keyko.nevermined.exceptions.ProvenanceException;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.contracts.ProvenanceEntry;
import io.keyko.nevermined.models.contracts.ProvenanceEvent;

import java.util.List;

/**
 * Exposes the Public API related with Provenance
 */
public interface ProvenanceAPI {

    /**
     * Implements the W3C PROV Usage action
     *
     * @param provenanceId  Provenance ID
     * @param did           Identifier of the entity created
     * @param agentId       Agent Identifier
     * @param activityId    Identifier of the activity creating the new entity
     * @param signature     Signature (optional) provided by the agent involved
     * @param attributes    Attributes associated with the action
     * @return true if the provenance event was registered correctly
     * @throws ProvenanceException Error registering the event
     */
    boolean used(String provenanceId, DID did, String agentId, String activityId, String signature, String attributes) throws ProvenanceException;

    /**
     * Implements the W3C PROV Derivation action
     *
     * @param provenanceId  Provenance ID
     * @param newEntityDid  Identifier of the new entity derived
     * @param usedEntityDid Identifier of the entity used to derive the new entity
     * @param agentId       Agent Identifier
     * @param activityId    Identifier of the activity creating the new entity
     * @param attributes    Attributes associated with the action
     * @return true if the provenance event was registered correctly
     * @throws ProvenanceException Error registering the event
     */
    boolean wasDerivedFrom(String provenanceId, DID newEntityDid, DID usedEntityDid, String agentId, String activityId, String attributes) throws ProvenanceException;

    /**
     * Implements the W3C PROV Association action
     *
     * @param provenanceId  Provenance ID
     * @param did           Identifier of the entity created
     * @param agentId       Agent Identifier
     * @param activityId    Identifier of the activity creating the new entity
     * @param attributes    Attributes associated with the action
     * @return true if the provenance event was registered correctly
     * @throws ProvenanceException Error registering the event
     */
    boolean wasAssociatedWith(String provenanceId, DID did, String agentId, String activityId, String attributes) throws ProvenanceException;

    /**
     * Implements the W3C PROV Delegation action
     *
     * @param provenanceId  Provenance ID
     * @param did           Identifier of the entity created
     * @param delegateAgentId       Delegate Agent Identifier
     * @param responsibleAgentId    Responsible Agent Identifier
     * @param activityId    Identifier of the activity creating the new entity
     * @param signature     Signature provided by the delegated agent
     * @param attributes    Attributes associated with the action
     * @return true if the provenance event was registered correctly
     * @throws ProvenanceException Error registering the event
     */
    boolean actedOnBehalf(String provenanceId, DID did, String delegateAgentId, String responsibleAgentId, String activityId, String signature, String attributes) throws ProvenanceException;

    /**
     * Fetch from the on-chain Provenance registry the information about one provenance event, given a provenance id
     *
     * @param provenanceId unique identifier of the provenance entry
     * @return ProvenanceEntry object with all the information stored on-chain
     * @throws ProvenanceException Error fetching the data
     */
    ProvenanceEntry getProvenanceEntry(String provenanceId) throws ProvenanceException;

    /**
     * Indicates if an address is a provenance delegate for a given DID
     * @param did Identifier of the asset
     * @param delegate address of the delegate
     * @return true if the address is a provenance delegate
     * @throws ProvenanceException Error fetching the data
     */
    boolean isProvenanceDelegate(DID did, String delegate) throws ProvenanceException;

    /**
     * Adds an address as delegate for a given DID
     * @param did Identifier of the asset
     * @param delegate address of the delegate
     * @return true if the address was added as a delegate for the DID given
     * @throws ProvenanceException Error fetching the data
     */
    boolean addDIDProvenanceDelegate(DID did, String delegate) throws ProvenanceException;

    /**
     * Remove an address as delegate for a given DID
     * @param did Identifier of the asset
     * @param delegate address of the delegate
     * @return true if the address was removed as a delegate for the DID given
     * @throws ProvenanceException Error fetching the data
     */
    boolean removeDIDProvenanceDelegate(DID did, String delegate) throws ProvenanceException;

    /**
     * Adds an address as delegate for a given DID
     * @param provenanceId unique identifier of the provenance entry
     * @return String with the address owning the provenance entry
     * @throws ProvenanceException Error fetching the data
     */
    String getProvenanceOwner(String provenanceId) throws ProvenanceException;

    /**
     * Search for ProvenanceAttributeRegistered events related with a specific DID
     *
     * @param did           Identifier of the entity we are looking provenance events
     * @return List of ProvenanceEvents found
     * @throws ProvenanceException DDOException
     */
    List<ProvenanceEvent> getDIDProvenanceEvents(DID did) throws ProvenanceException;

    /**
     * Search for provenance methods (used, wasGeneratedBy, etc.) given a DID
     *
     * @param did           Identifier of the entity we are looking provenance events
     * @return List of ProvenanceEvents found
     * @throws ProvenanceException DDOException
     */
    List<ProvenanceEvent> getProvenanceMethodEvents(ProvenanceEntry.ProvenanceMethod method, DID did) throws ProvenanceException;
}
