package io.keyko.nevermined.api;

import io.keyko.nevermined.exceptions.ServiceAgreementException;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.service.Agreement;
import io.keyko.nevermined.models.service.AgreementStatus;

/**
 * Exposes the Public API related with the management of Agreements
 */
public interface AgreementsAPI {

    /**
     * Create a service agreement.
     *
     * @param did                 the did
     * @param agreementId         the agreement id
     * @param serviceIndex the service definition id of the agreement
     * @param consumerAddress     the address of the consumer
     * @return a flag a true if the creation of the agreement was successful.
     * @throws ServiceAgreementException Exception
     */
    boolean create(DID did, String agreementId, int serviceIndex, String consumerAddress) throws ServiceAgreementException;

    /**
     * Get the status of a service agreement and the status of all the conditions part of that agreement
     *
     * @param agreementId id of the agreement
     * @return AgreementStatus with condition status of each of the agreement's conditions.
     * @throws ServiceAgreementException Exception
     */
    AgreementStatus status(String agreementId) throws ServiceAgreementException;

    /**
     * Returns if a service agreement is granted
     *
     * @param agreementId the agreement id
     * @param did the did
     * @param consumerAddress the address of the consumer
     * @return true if access is granted
     * @throws ServiceAgreementException Exception
     */
    boolean isAccessGranted(String agreementId, DID did, String consumerAddress) throws ServiceAgreementException;

    /**
     * Get the status of a service agreement.
     *
     * @param agreementId id of the agreement
     * @return AgreementStatus with condition status of each of the agreement's conditions.
     * @throws ServiceAgreementException Exception
     */
    Agreement getAgreement(String agreementId) throws ServiceAgreementException;
}
