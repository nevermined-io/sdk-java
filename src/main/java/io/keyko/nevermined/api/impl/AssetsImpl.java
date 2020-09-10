package io.keyko.nevermined.api.impl;

import io.keyko.nevermined.api.AssetsAPI;
import io.keyko.nevermined.exceptions.*;
import io.keyko.nevermined.external.GatewayService;
import io.keyko.nevermined.manager.AgreementsManager;
import io.keyko.nevermined.manager.AssetsManager;
import io.keyko.nevermined.manager.NeverminedManager;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.asset.OrderResult;
import io.keyko.nevermined.models.metadata.SearchResult;
import io.keyko.nevermined.models.service.AuthConfig;
import io.keyko.nevermined.models.service.ProviderConfig;
import io.keyko.nevermined.models.service.Service;
import io.keyko.nevermined.models.service.types.ComputingService;
import io.reactivex.Flowable;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Implementation of AssetsAPI
 */
public class AssetsImpl implements AssetsAPI {

    private NeverminedManager neverminedManager;
    private AssetsManager assetsManager;
    private AgreementsManager agreementsManager;

    private static final int DEFAULT_OFFSET = 20;
    private static final int DEFAULT_PAGE = 1;

    /**
     * Constructor
     *
     * @param neverminedManager  the neverminedManager
     * @param assetsManager the assetsManager
     * @param agreementsManager the agreements Manager
     */
    public AssetsImpl(NeverminedManager neverminedManager, AssetsManager assetsManager, AgreementsManager agreementsManager) {

        this.neverminedManager = neverminedManager;
        this.assetsManager = assetsManager;
        this.agreementsManager = agreementsManager;
    }


    @Override
    public DDO create(AssetMetadata metadata, ProviderConfig providerConfig, AuthConfig authConfig) throws DDOException {
        return neverminedManager.registerAccessServiceAsset(metadata, providerConfig, authConfig);
    }

    @Override
    public DDO create(AssetMetadata metadata, ProviderConfig providerConfig) throws DDOException {
        return this.create(metadata, providerConfig, new AuthConfig(providerConfig.getGatewayUrl()));
    }

    @Override
    public DDO createComputeService(AssetMetadata metadata, ProviderConfig providerConfig) throws DDOException {
        return neverminedManager.registerComputeService(metadata, providerConfig, new ComputingService.Provider());
    }

    @Override
    public DDO createComputeService(AssetMetadata metadata, ProviderConfig providerConfig, ComputingService.Provider computingProvider) throws DDOException {
        return neverminedManager.registerComputeService(metadata, providerConfig, computingProvider);
    }

    @Override
    public DDO resolve(DID did) throws DDOException {
        return neverminedManager.resolveDID(did);
    }

    @Override
    public List<AssetMetadata.File> getMetadataFiles(DID did) throws DDOException {

        try {

            DDO ddo = this.resolve(did);
            return neverminedManager.getDecriptedSecretStoreMetadataFiles(ddo);

        }catch (Exception e){
            throw new DDOException("Error trying to get the files of the DDO", e);
        }

    }

    @Override
    public SearchResult search(String text) throws DDOException {
        return this.search(text, DEFAULT_OFFSET, DEFAULT_PAGE);
    }

    @Override
    public SearchResult search(String text, int offset, int page) throws DDOException {
        return assetsManager.searchAssets(text, offset, page);
    }

    @Override
    public SearchResult query(Map<String, Object> params, int offset, int page, int sort) throws DDOException {
        return assetsManager.searchAssets(params, offset, page, sort);
    }

    @Override
    public SearchResult query(Map<String, Object> params) throws DDOException {
        return this.query(params, DEFAULT_OFFSET, DEFAULT_PAGE, 1);
    }

    @Override
    public Boolean consume(String serviceAgreementId, DID did, int serviceDefinitionId, String basePath) throws ConsumeServiceException {
        return neverminedManager.access(serviceAgreementId, did, serviceDefinitionId, basePath);
    }

    @Override
    public Boolean consume(String serviceAgreementId, DID did, int serviceDefinitionId, int fileIndex, String basePath) throws ConsumeServiceException {
        return neverminedManager.access(serviceAgreementId, did, serviceDefinitionId, fileIndex, basePath);
    }


    @Override
    public InputStream consumeBinary(String serviceAgreementId, DID did, int serviceDefinitionId) throws ConsumeServiceException{
        return this.consumeBinary(serviceAgreementId, did, serviceDefinitionId, 0);
    }

    @Override
    public InputStream consumeBinary(String serviceAgreementId, DID did, int serviceDefinitionId, int fileIndex) throws ConsumeServiceException{
        return neverminedManager.consumeBinary(serviceAgreementId, did, serviceDefinitionId,  fileIndex);
    }

    @Override
    public InputStream consumeBinary(String serviceAgreementId, DID did, int serviceDefinitionId, int fileIndex, int rangeStart, int rangeEnd) throws ConsumeServiceException{
        return neverminedManager.consumeBinary(serviceAgreementId, did, serviceDefinitionId, fileIndex, true, rangeStart, rangeEnd);
    }

    @Override
    public Flowable<OrderResult> order(DID did, int serviceDefinitionId) throws OrderException {
        return neverminedManager.purchaseAssetFlowable(did, serviceDefinitionId);
    }

    public OrderResult orderDirect(DID did) throws OrderException, ServiceException, EscrowRewardException {
        return neverminedManager.purchaseAssetDirect(did);
    }

    public OrderResult orderDirect(DID did, Service.ServiceTypes serviceTypes) throws OrderException, ServiceException, EscrowRewardException {
        return neverminedManager.purchaseAssetDirect(did, serviceTypes);
    }

    public OrderResult orderDirect(DID did, int serviceDefinitionId, Service.ServiceTypes serviceTypes) throws OrderException, ServiceException, EscrowRewardException {
        return neverminedManager.purchaseAssetDirect(did, serviceDefinitionId, serviceTypes);
    }


    public OrderResult orderDirect(DID did, int serviceDefinitionId) throws OrderException, ServiceException, EscrowRewardException {
        return neverminedManager.purchaseAssetDirect(did, serviceDefinitionId);
    }

    @Override
    public Boolean retire(DID did) throws DDOException {
        return assetsManager.deleteAsset(did);
    }

    @Override
    public List<DID> ownerAssets(String ownerAddress) throws ServiceException {
        return assetsManager.getOwnerAssets(ownerAddress);
    }

    @Override
    public List<DID> consumerAssets(String consumerAddress) throws ServiceException {
        return agreementsManager.getConsumerAssets(consumerAddress);
    }

    @Override
    public GatewayService.ServiceExecutionResult execute(String agreementId, DID did, int index, DID workflowDID) throws ServiceException {
        return neverminedManager.executeComputeService(agreementId, did, index, workflowDID);
    }

    @Override
    public String owner(DID did) throws Exception {
        return assetsManager.getDIDOwner(did);
    }

    @Override
    public Boolean validate(AssetMetadata metadata) throws DDOException {
        return assetsManager.validateMetadata(metadata);
    }

    @Override
    public Boolean transferOwnership(DID did, String newOwnerAddress) throws DDOException {
        return assetsManager.transferOwnership(did, newOwnerAddress);
    }

    @Override
    public Boolean delegatePermissions(DID did, String subjectAddress) throws DDOException {
        return assetsManager.grantPermission(did, subjectAddress);
    }

    @Override
    public Boolean revokePermissions(DID did, String subjectAddress) throws DDOException {
        return assetsManager.revokePermission(did, subjectAddress);
    }

    @Override
    public Boolean getPermissions(DID did, String subjectAddress) throws DDOException {
        return assetsManager.getPermission(did, subjectAddress);
    }
}
