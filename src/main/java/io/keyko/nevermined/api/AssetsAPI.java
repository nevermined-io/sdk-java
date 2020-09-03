package io.keyko.nevermined.api;

import io.keyko.nevermined.exceptions.*;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.asset.OrderResult;
import io.keyko.nevermined.models.metadata.SearchResult;
import io.keyko.nevermined.models.service.AuthConfig;
import io.keyko.nevermined.models.service.ProviderConfig;
import io.keyko.nevermined.models.service.types.ComputingService;
import io.reactivex.Flowable;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Exposes the Public API related with Assets
 */
public interface AssetsAPI {

    /**
     * Creates a new DDO, registering it on-chain through DidRegistry contract and off-chain in Metadata
     *
     * @param metadata       the metadata of the DDO
     * @param providerConfig the endpoints of the DDO's services
     * @param authConfig     Auth configuration
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    DDO create(AssetMetadata metadata, ProviderConfig providerConfig, AuthConfig authConfig) throws DDOException;

    /**
     * Creates a new DDO, registering it on-chain through DidRegistry contract and off-chain in Metadata
     *
     * @param metadata       the metadata of the DDO
     * @param providerConfig the endpoints of the DDO's services
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    DDO create(AssetMetadata metadata, ProviderConfig providerConfig) throws DDOException;


    /**
     * Creates a new ComputingService DDO, registering it on-chain through DidRegistry contract and off-chain in Metadata
     *
     * @param metadata       the metadata of the DDO
     * @param providerConfig the endpoints of the DDO's services
     * @param computingProvider the computing provider configuration
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    DDO createComputeService(AssetMetadata metadata, ProviderConfig providerConfig, ComputingService.Provider computingProvider) throws DDOException;


    /**
     * Gets a DDO from a DID
     *
     * @param did the DID to resolve
     * @return an instance of the DDO represented by the DID
     * @throws EthereumException EthereumException
     * @throws DDOException      DDOException
     */
    DDO resolve(DID did) throws EthereumException, DDOException;

    /**
     * Gets the list of the files that belongs to a DDO
     * @param did the DID to resolve
     * @return a list of the Files
     * @throws DDOException EncryptionException
     */
    List<AssetMetadata.File> getMetadataFiles(DID did) throws DDOException;

    /**
     * Gets all the DDO that match the search criteria
     *
     * @param text the criteria
     * @return a List with all the DDOs found
     * @throws DDOException DDOException
     */
    SearchResult search(String text) throws DDOException;

    /**
     * Gets all the DDOs that match the search criteria
     *
     * @param text   the criteria
     * @param offset parameter to paginate
     * @param page   parameter to paginate
     * @return a List with all the DDOs found
     * @throws DDOException DDOException
     */
    SearchResult search(String text, int offset, int page) throws DDOException;

    /**
     * Gets all the DDOs that match the parameters of the query
     *
     * @param params the criteria
     * @param offset parameter to paginate
     * @param page   parameter to paginate
     * @param sort   parameter to sort
     * @return a List with all the DDOs found
     * @throws DDOException DDOException
     */
    SearchResult query(Map<String, Object> params, int offset, int page, int sort) throws DDOException;

    /**
     * Gets all the DDOs that match the parameters of the query
     *
     * @param params the criteria
     * @return a List with all the DDOs found
     * @throws DDOException DDOException
     */
    SearchResult query(Map<String, Object> params) throws DDOException;

    /**
     * Downloads an Asset previously ordered through a Service Agreement
     *
     * @param serviceAgreementId  the service agreement id of the asset
     * @param did                 the did
     * @param serviceDefinitionId the service definition id
     * @param basePath            the path where the asset will be downloaded
     * @return a flag that indicates if the consume flow was executed correctly
     * @throws ConsumeServiceException ConsumeServiceException
     */
    Boolean consume(String serviceAgreementId, DID did, int serviceDefinitionId, String basePath) throws ConsumeServiceException;

    /**
     * Downloads an Asset previously ordered through a Service Agreement
     *
     * @param serviceAgreementId  the service agreement id of the asset
     * @param did                 the did
     * @param serviceDefinitionId the service definition id
     * @param fileIndex           index id of the file to consume
     * @param basePath            the path where the asset will be downloaded
     * @return a flag that indicates if the consume flow was executed correctly
     * @throws ConsumeServiceException ConsumeServiceException
     */
    Boolean consume(String serviceAgreementId, DID did, int serviceDefinitionId, int fileIndex, String basePath) throws ConsumeServiceException;


    /**
     * Gets the input stream of one file of the asset
     * @param serviceAgreementId  the service agreement id of the asset
     * @param did                 the did
     * @param serviceDefinitionId the service definition id
     * @return the input stream wit the binary content of the file
     * @throws ConsumeServiceException ConsumeServiceException
     */
    InputStream consumeBinary(String serviceAgreementId, DID did, int serviceDefinitionId) throws ConsumeServiceException;

    /**
     * Gets the input stream of one file of the asset
     * @param serviceAgreementId  the service agreement id of the asset
     * @param did                 the did
     * @param serviceDefinitionId the service definition id
     * @param fileIndex               the index of the file
     * @return the input stream wit the binary content of the file
     * @throws ConsumeServiceException ConsumeServiceException
     */
    InputStream consumeBinary(String serviceAgreementId, DID did, int serviceDefinitionId, int fileIndex) throws ConsumeServiceException;


    /**
     * Gets a range of bytes of the input stream of one file of the asset
     * @param serviceAgreementId  the service agreement id of the asset
     * @param did                 the did
     * @param serviceDefinitionId the service definition id
     * @param fileIndex               the index of the file
     * @param rangeStart          the start of the bytes range
     * @param rangeEnd            the end of the bytes range
     * @return                    the input stream wit the binary content of the specified range
     * @throws ConsumeServiceException ConsumeServiceException
     */
    InputStream consumeBinary(String serviceAgreementId, DID did, int serviceDefinitionId, int fileIndex, int rangeStart, int rangeEnd) throws ConsumeServiceException;


    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service Agreement between publisher and consumer
     *
     * @param did                 the did of the DDO
     * @param serviceDefinitionId the service definition id
     * @return a Flowable instance over an OrderResult to get the result of the flow in an asynchronous fashion
     * @throws OrderException OrderException
     */
    Flowable<OrderResult> order(DID did, int serviceDefinitionId) throws OrderException;

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service Agreement between publisher and consumer
     *
     * @param did                 the did of the DDO
     * @param serviceDefinitionId the service definition id
     * @return OrderResult
     * @throws OrderException OrderException
     * @throws ServiceException ServiceException
     * @throws EscrowRewardException EscrowRewardException
     */
    OrderResult orderDirect(DID did, int serviceDefinitionId) throws OrderException, ServiceException, EscrowRewardException;

    /**
     * Executes a remote service associated with an asset and serviceAgreementId
     * @param agreementId the agreement id
     * @param did the did
     * @param index the index of the service
     * @param workflowDID the workflow did
     * @return an execution id
     * @throws ServiceException ServiceException
     */
    String execute(String agreementId, DID did, int index, DID workflowDID) throws ServiceException;

    /**
     * Return the owner of the asset.
     *
     * @param did the did
     * @return the ethereum address of the owner/publisher of given asset did
     * @throws Exception Exception
     */
    String owner(DID did) throws Exception;

    /**
     * List of Asset objects published by ownerAddress
     *
     * @param ownerAddress ethereum address of owner/publisher
     * @return list of dids
     * @throws ServiceException ServiceException
     */
    List<DID> ownerAssets(String ownerAddress) throws ServiceException;

    /**
     * List of Asset objects purchased by consumerAddress
     *
     * @param consumerAddress ethereum address of consumer
     * @return list of dids
     * @throws ServiceException ServiceException
     */
    List<DID> consumerAssets(String consumerAddress) throws ServiceException;

    /**
     * Retire this did of Metadata
     *
     * @param did the did
     * @return a flag that indicates if the action was executed correctly
     * @throws DDOException DDOException
     */
    Boolean retire(DID did) throws DDOException;

    /**
     * Validate the asset metadata.
     *
     * @param metadata the metadata of the DDO
     * @return a flag that indicates if the metadata is valid
     * @throws DDOException DDOException
     */
    Boolean validate(AssetMetadata metadata) throws DDOException;


    /**
     * Given a DID, transfer the ownership to a new owner. This function only will work if is called by the DID owner.
     * @param did the did
     * @param newOwnerAddress the address of the new ownership
     * @return  a flag that indicates if the action was executed correctly
     * @throws DDOException DDOException
     */
    Boolean transferOwnership(DID did, String newOwnerAddress) throws DDOException;


    /**
     *  For a existing asset, the owner of the asset delegate to a subject read or access permiss
     * @param did the did
     * @param subjectAddress the address we want to delegate to
     * @return a flag that indicates if the action was executed correctly
     * @throws DDOException DDOException
     */
    Boolean delegatePermissions(DID did, String subjectAddress) throws DDOException;

    /**
     * For a existing asset, the owner of the asset revoke the access grants of a subject.
     * @param did the did
     * @param subjectAddress the address we want to revoke to
     * @return a flag that indicates if the action was executed correctly
     * @throws DDOException DDOException
     */
    Boolean revokePermissions(DID did, String subjectAddress) throws DDOException;


    /**
     * Check if an user has permissions in a specific DID
     * @param did  the did
     * @param subjectAddress the address
     * @return a flag that indicates if the subject address has permissions
     * @throws DDOException DDOException
     */
    Boolean getPermissions(DID did, String subjectAddress) throws DDOException;


}
