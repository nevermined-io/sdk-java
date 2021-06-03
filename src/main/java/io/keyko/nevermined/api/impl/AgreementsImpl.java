package io.keyko.nevermined.api.impl;

import io.keyko.nevermined.api.AgreementsAPI;
import io.keyko.nevermined.exceptions.ServiceAgreementException;
import io.keyko.nevermined.manager.AgreementsManager;
import io.keyko.nevermined.manager.NeverminedManager;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.service.Agreement;
import io.keyko.nevermined.models.service.AgreementStatus;
import io.keyko.nevermined.models.service.Service;

import java.util.List;

public class AgreementsImpl implements AgreementsAPI {

    private AgreementsManager agreementsManager;
    private NeverminedManager neverminedManager;


    /**
     * Constructor
     *
     * @param agreementsManager the accountsManager
     * @param neverminedManager an instance of neverminedManager
     */
    public AgreementsImpl(AgreementsManager agreementsManager, NeverminedManager neverminedManager) {
        this.neverminedManager = neverminedManager;
        this.agreementsManager = agreementsManager;
    }

    @Override
    public boolean create(DID did, String agreementId, int serviceIndex, String consumerAddress) throws ServiceAgreementException {

        try {
            DDO ddo = neverminedManager.resolveDID(did);
            Service service = ddo.getService(serviceIndex);

            List<byte[]> conditionsId = neverminedManager.generateServiceConditionsId(
                    agreementId, consumerAddress, ddo, serviceIndex);

            // TODO: Add NFT use cases: NFT_ACCESS, NFT_SALES, DID_SALES
            if (service.type.equals(Service.ServiceTypes.ACCESS.toString()))
                return agreementsManager.createAccessAgreement(agreementId,
                        ddo,
                        conditionsId,
                        consumerAddress,
                        service
                );
            else if (service.type.equals(Service.ServiceTypes.COMPUTE.toString()))
                return agreementsManager.createComputeAgreement(agreementId,
                        ddo,
                        conditionsId,
                        consumerAddress,
                        service
                );
            else
                throw new ServiceAgreementException(agreementId, "There was a problem creating the agreement. Service Type not supported");
        } catch (Exception e){
            throw new ServiceAgreementException(agreementId, "There was a problem creating the agreement", e);
        }

    }

    @Override
    public AgreementStatus status(String agreementId) throws ServiceAgreementException {
        try {
            return agreementsManager.getStatus(agreementId);
        }catch (Exception e) {
            throw new ServiceAgreementException(agreementId, "There was a problem getting the status of the agreement", e);
        }
    }

    @Override
    public boolean isAccessGranted(String agreementId, DID did, String consumerAddress) throws ServiceAgreementException {
        try {
            final Agreement agreement = agreementsManager.getAgreement(agreementId);

            if (!agreement.did.getHash().equalsIgnoreCase(did.getHash()))
                return false;

            final AgreementStatus status = status(agreementId);
            return status.conditionsFulfilled;
        }catch (Exception e) {
            throw new ServiceAgreementException(agreementId, "There was a problem getting the status of the agreement", e);
        }
    }

}