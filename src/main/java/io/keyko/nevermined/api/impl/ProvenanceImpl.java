package io.keyko.nevermined.api.impl;

import io.keyko.nevermined.api.ProvenanceAPI;
import io.keyko.nevermined.exceptions.ProvenanceException;
import io.keyko.nevermined.manager.NeverminedManager;
import io.keyko.nevermined.manager.ProvenanceManager;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.contracts.ProvenanceEntry;
import io.keyko.nevermined.models.contracts.ProvenanceEvent;

import java.util.List;

/**
 * Implementation of AssetsAPI
 */
public class ProvenanceImpl implements ProvenanceAPI {

    private NeverminedManager neverminedManager;
    private ProvenanceManager provenanceManager;


    /**
     * Constructor
     *
     * @param neverminedManager  the neverminedManager
     * @param provenanceManager the provenanceManager
     */
    public ProvenanceImpl(NeverminedManager neverminedManager, ProvenanceManager provenanceManager) {

        this.neverminedManager = neverminedManager;
        this.provenanceManager = provenanceManager;
    }

    @Override
    public boolean used(String provenanceId, DID did, String agentId, String activityId, String signature, String attributes) throws ProvenanceException {
        return provenanceManager.used(provenanceId, did, agentId, activityId, signature, attributes);
    }

    @Override
    public boolean wasDerivedFrom(String provenanceId, DID newEntityDid, DID usedEntityDid, String agentId, String activityId, String attributes) throws ProvenanceException {
        return provenanceManager.wasDerivedFrom(provenanceId, newEntityDid, usedEntityDid, agentId, activityId, attributes);
    }

    @Override
    public boolean wasAssociatedWith(String provenanceId, DID did, String agentId, String activityId, String attributes) throws ProvenanceException {
        return provenanceManager.wasAssociatedWith(provenanceId, did, agentId, activityId, attributes);
    }

    @Override
    public boolean actedOnBehalf(String provenanceId, DID did, String delegateAgentId, String responsibleAgentId, String activityId, String signature, String attributes) throws ProvenanceException {
        return provenanceManager.actedOnBehalf(provenanceId, did, delegateAgentId, responsibleAgentId, activityId, signature, attributes);
    }

    @Override
    public ProvenanceEntry getProvenanceEntry(String provenanceId) throws ProvenanceException {
        return provenanceManager.getProvenanceEntry(provenanceId);
    }

    @Override
    public boolean isProvenanceDelegate(DID did, String delegate) throws ProvenanceException {
        return provenanceManager.isProvenanceDelegate(did, delegate);
    }

    @Override
    public boolean addDIDProvenanceDelegate(DID did, String delegate) throws ProvenanceException {
        return provenanceManager.addDIDProvenanceDelegate(did, delegate);
    }

    @Override
    public boolean removeDIDProvenanceDelegate(DID did, String delegate) throws ProvenanceException {
        return provenanceManager.removeDIDProvenanceDelegate(did, delegate);
    }

    @Override
    public String getProvenanceOwner(String provenanceId) throws ProvenanceException {
        return provenanceManager.getProvenanceOwner(provenanceId);
    }

    @Override
    public List<ProvenanceEvent> getDIDProvenanceEvents(DID did) throws ProvenanceException {
        return provenanceManager.getDIDProvenanceEvents(did);
    }

    @Override
    public List<ProvenanceEvent> getProvenanceMethodEvents(ProvenanceEntry.ProvenanceMethod method, DID did) throws ProvenanceException {
        return provenanceManager.getProvenanceMethodEvents(method, did);
    }


}
