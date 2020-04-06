/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api;

import com.oceanprotocol.squid.exceptions.*;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.aquarius.SearchResult;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.asset.OrderResult;
import com.oceanprotocol.squid.models.service.ProviderConfig;
import com.oceanprotocol.squid.models.service.types.ComputingService;
import io.reactivex.Flowable;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Exposes the Public API related with Assets
 */
public interface AssetsAPI {

    /**
     * Creates a new DDO, registering it on-chain through DidRegistry contract and off-chain in Aquarius
     *
     * @param metadata       the metadata of the DDO
     * @param providerConfig the endpoints of the DDO's services
     * @param threshold      the secret store threshold
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO create(AssetMetadata metadata, ProviderConfig providerConfig, int threshold) throws DDOException;

    /**
     * Creates a new DDO, registering it on-chain through DidRegistry contract and off-chain in Aquarius
     *
     * @param metadata       the metadata of the DDO
     * @param providerConfig the endpoints of the DDO's services
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO create(AssetMetadata metadata, ProviderConfig providerConfig) throws DDOException;



    /**
     * Creates a new ComputingService DDO, registering it on-chain through DidRegistry contract and off-chain in Aquarius
     *
     * @param metadata       the metadata of the DDO
     * @param providerConfig the endpoints of the DDO's services
     * @param computingProvider the computing provider configuration
     * @param threshold      the secret store threshold
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO createComputingService(AssetMetadata metadata, ProviderConfig providerConfig, ComputingService.Provider computingProvider, int threshold) throws DDOException;

    /**
     * Creates a new ComputingService DDO, registering it on-chain through DidRegistry contract and off-chain in Aquarius
     *
     * @param metadata       the metadata of the DDO
     * @param providerConfig the endpoints of the DDO's services
     * @param computingProvider the computing provider configuration
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO createComputingService(AssetMetadata metadata, ProviderConfig providerConfig, ComputingService.Provider computingProvider) throws DDOException;


    /**
     * Gets a DDO from a DID
     *
     * @param did the DID to resolve
     * @return an instance of the DDO represented by the DID
     * @throws EthereumException EthereumException
     * @throws DDOException      DDOException
     */
    public DDO resolve(DID did) throws EthereumException, DDOException;

    /**
     * Gets the list of the files that belongs to a DDO
     * @param did the DID to resolve
     * @return a list of the Files
     * @throws DDOException EncryptionException
     */
    public List<AssetMetadata.File> getMetadataFiles(DID did) throws DDOException;

    /**
     * Gets all the DDO that match the search criteria
     *
     * @param text the criteria
     * @return a List with all the DDOs found
     * @throws DDOException DDOException
     */
    public SearchResult search(String text) throws DDOException;

    /**
     * Gets all the DDOs that match the search criteria
     *
     * @param text   the criteria
     * @param offset parameter to paginate
     * @param page   parameter to paginate
     * @return a List with all the DDOs found
     * @throws DDOException DDOException
     */
    public SearchResult search(String text, int offset, int page) throws DDOException;

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
    public SearchResult query(Map<String, Object> params, int offset, int page, int sort) throws DDOException;

    /**
     * Gets all the DDOs that match the parameters of the query
     *
     * @param params the criteria
     * @return a List with all the DDOs found
     * @throws DDOException DDOException
     */
    public SearchResult query(Map<String, Object> params) throws DDOException;

    /**
     *  Downloads a single file of an Asset previously ordered through a Service Agreement
     * @param serviceAgreementId the service agreement id of the asset
     * @param did the did
     * @param serviceDefinitionId the service definition id
     * @param index of the file inside the files definition in metadata
     * @param basePath  the path where the asset will be downloaded
     * @param threshold secret store threshold to decrypt the urls of the asset
     * @return a flag that indicates if the consume flow was executed correctly
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public Boolean consume(String serviceAgreementId, DID did, int serviceDefinitionId, Integer index, String basePath, int threshold) throws ConsumeServiceException;

    /**
     *  Downloads a single file of an Asset previously ordered through a Service Agreement
     * @param serviceAgreementId the service agreement id of the asset
     * @param did the did
     * @param serviceDefinitionId the service definition id
     * @param index of the file inside the files definition in metadata
     * @param basePath the path where the asset will be downloaded
     * @return a flag that indicates if the consume flow was executed correctly
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public Boolean consume(String serviceAgreementId, DID did, int serviceDefinitionId, Integer index, String basePath) throws ConsumeServiceException;

    /**
     * Downloads an Asset previously ordered through a Service Agreement
     *
     * @param serviceAgreementId  the service agreement id of the asset
     * @param did                 the did
     * @param serviceDefinitionId the service definition id
     * @param basePath            the path where the asset will be downloaded
     * @param threshold           secret store threshold to decrypt the urls of the asset
     * @return a flag that indicates if the consume flow was executed correctly
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public Boolean consume(String serviceAgreementId, DID did, int serviceDefinitionId, String basePath, int threshold) throws ConsumeServiceException;

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
    public Boolean consume(String serviceAgreementId, DID did, int serviceDefinitionId, String basePath) throws ConsumeServiceException;


    /**
     * Gets the input stream of one file of the asset
     * @param serviceAgreementId  the service agreement id of the asset
     * @param did                 the did
     * @param serviceDefinitionId the service definition id
     * @param index               the index of the file
     * @return the input stream wit the binary content of the file
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public InputStream consumeBinary(String serviceAgreementId, DID did, int serviceDefinitionId, Integer index) throws ConsumeServiceException;

    /**
     * Gets the input stream of one file of the asset
     * @param serviceAgreementId  the service agreement id of the asset
     * @param did                 the did
     * @param serviceDefinitionId the service definition id
     * @param index               the index of the file
     * @param threshold           secret store threshold to decrypt the urls of the asset
     * @return the input stream wit the binary content of the file
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public InputStream consumeBinary(String serviceAgreementId, DID did, int serviceDefinitionId, Integer index, int threshold) throws ConsumeServiceException;


    /**
     * Gets a range of bytes of the input stream of one file of the asset
     * @param serviceAgreementId  the service agreement id of the asset
     * @param did                 the did
     * @param serviceDefinitionId the service definition id
     * @param index               the index of the file
     * @param rangeStart          the start of the bytes range
     * @param rangeEnd            the end of the bytes range
     * @return                    the input stream wit the binary content of the specified range
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public InputStream consumeBinary(String serviceAgreementId, DID did, int serviceDefinitionId, Integer index, Integer rangeStart, Integer rangeEnd) throws ConsumeServiceException;


    /**
     * Gets a range of bytes of the input stream of one file of the asset
     * @param serviceAgreementId  the service agreement id of the asset
     * @param did                 the did
     * @param serviceDefinitionId the service definition id
     * @param index               the index of the file
     * @param rangeStart          the start of the bytes range
     * @param rangeEnd            the end of the bytes range
     * @param threshold           secret store threshold to decrypt the urls of the asset
     * @return                    the input stream wit the binary content of the specified range
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public InputStream consumeBinary(String serviceAgreementId, DID did, int serviceDefinitionId, Integer index, Integer rangeStart, Integer rangeEnd, int threshold) throws ConsumeServiceException;


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
     * Executes a remote service associated with an asset and serviceAgreementId
     * @param agreementId the agreement id
     * @param did the did
     * @param index the index of the service
     * @param workflowDID the workflow id
     * @return an execution id
     * @throws ServiceException ServiceException
     */
    public String execute(String agreementId, DID did, int index, String workflowDID) throws ServiceException;

    /**
     * Return the owner of the asset.
     *
     * @param did the did
     * @return the ethereum address of the owner/publisher of given asset did
     * @throws Exception Exception
     */
    public String owner(DID did) throws Exception;

    /**
     * List of Asset objects published by ownerAddress
     *
     * @param ownerAddress ethereum address of owner/publisher
     * @return list of dids
     * @throws ServiceException ServiceException
     */
    public List<DID> ownerAssets(String ownerAddress) throws ServiceException;

    /**
     * List of Asset objects purchased by consumerAddress
     *
     * @param consumerAddress ethereum address of consumer
     * @return list of dids
     * @throws ServiceException ServiceException
     */
    public List<DID> consumerAssets(String consumerAddress) throws ServiceException;

    /**
     * Retire this did of Aquarius
     *
     * @param did the did
     * @return a flag that indicates if the action was executed correctly
     * @throws DDOException DDOException
     */
    public Boolean retire(DID did) throws DDOException;

    /**
     * Validate the asset metadata.
     *
     * @param metadata the metadata of the DDO
     * @return a flag that indicates if the metadata is valid
     * @throws DDOException DDOException
     */
    public Boolean validate(AssetMetadata metadata) throws DDOException;


    /**
     * Given a DID, transfer the ownership to a new owner. This function only will work if is called by the DID owner.
     * @param did the did
     * @param newOwnerAddress the address of the new ownership
     * @return  a flag that indicates if the action was executed correctly
     * @throws DDOException DDOException
     */
    public Boolean transferOwnership(DID did, String newOwnerAddress) throws DDOException;


    /**
     *  For a existing asset, the owner of the asset delegate to a subject read or access permiss
     * @param did the did
     * @param subjectAddress the address we want to delegate to
     * @return a flag that indicates if the action was executed correctly
     * @throws DDOException DDOException
     */
    public Boolean delegatePermissions(DID did, String subjectAddress) throws DDOException;

    /**
     * For a existing asset, the owner of the asset revoke the access grants of a subject.
     * @param did the did
     * @param subjectAddress the address we want to revoke to
     * @return a flag that indicates if the action was executed correctly
     * @throws DDOException DDOException
     */
    public Boolean revokePermissions(DID did, String subjectAddress) throws DDOException;


    /**
     * Check if an user has permissions in a specific DID
     * @param did  the did
     * @param subjectAddress the address
     * @return a flag that indicates if the subject address has permissions
     * @throws DDOException DDOException
     */
    public Boolean getPermissions(DID did, String subjectAddress) throws DDOException;


}
