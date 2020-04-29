package io.keyko.nevermined.api;

import io.keyko.nevermined.exceptions.ServiceAgreementException;
import io.keyko.nevermined.models.Account;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.service.AgreementStatus;
import org.web3j.tuples.generated.Tuple2;

/**
 * Exposes the Public API related with the management of Agreements
 */
public interface AgreementsAPI {

    /**
     * Prepare the service agreement.
     *
     * @param did                 the did
     * @param serviceDefinitionId the service definition id of the agreement
     * @param consumerAccount     the address of the consumer
     * @return Tuple with agreement id and signature.
     * @throws ServiceAgreementException Exception
     */
    public Tuple2<String, String> prepare(DID did, int serviceDefinitionId, Account consumerAccount) throws ServiceAgreementException;

    /**
     * Create a service agreement.
     *
     * @param did                 the did
     * @param agreementId         the agreement id
     * @param index the service definition id of the agreement
     * @param consumerAddress     the address of the consumer
     * @return a flag a true if the creation of the agreement was successful.
     * @throws ServiceAgreementException Exception
     */
    public boolean create(DID did, String agreementId, int index, String consumerAddress) throws ServiceAgreementException;

    /**
     * Get the status of a service agreement.
     *
     * @param agreementId id of the agreement
     * @return AgreementStatus with condition status of each of the agreement's conditions.
     * @throws ServiceAgreementException Exception
     */
    public AgreementStatus status(String agreementId) throws ServiceAgreementException;
}
