package io.keyko.nevermined.api.impl;

import io.keyko.nevermined.api.AssetsAPI;
import io.keyko.nevermined.exceptions.*;
import io.keyko.nevermined.external.GatewayService;
import io.keyko.nevermined.manager.AgreementsManager;
import io.keyko.nevermined.manager.AssetsManager;
import io.keyko.nevermined.manager.NeverminedManager;
import io.keyko.nevermined.models.AssetRewards;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.asset.OrderResult;
import io.keyko.nevermined.models.gateway.ComputeLogs;
import io.keyko.nevermined.models.gateway.ComputeStatus;
import io.keyko.nevermined.models.metadata.SearchResult;
import io.keyko.nevermined.models.service.AuthConfig;
import io.keyko.nevermined.models.service.ProviderConfig;
import io.keyko.nevermined.models.service.Service;
import io.keyko.nevermined.models.service.types.ComputingService;
import io.reactivex.Flowable;

import java.io.InputStream;
import java.math.BigInteger;
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
    public DDO create(AssetMetadata metadata, ProviderConfig providerConfig, AuthConfig authConfig, AssetRewards assetRewards) throws DDOException {
        return neverminedManager.registerAccessServiceAsset(metadata, providerConfig, authConfig, assetRewards);
    }

    @Override
    public DDO create(AssetMetadata metadata, ProviderConfig providerConfig, AuthConfig authConfig) throws DDOException {
        final AssetRewards assetRewards = new AssetRewards(neverminedManager.getMainAccount().getAddress(), metadata.attributes.main.price);
        return neverminedManager.registerAccessServiceAsset(metadata, providerConfig, authConfig, assetRewards);
    }

    @Override
    public DDO create(AssetMetadata metadata, ProviderConfig providerConfig) throws DDOException {
        return this.create(metadata, providerConfig, new AuthConfig(providerConfig.getGatewayUrl()));
    }

    @Override
    public DDO create(AssetMetadata metadata, ProviderConfig providerConfig, AssetRewards assetRewards) throws DDOException {
        return this.create(metadata, providerConfig, new AuthConfig(providerConfig.getGatewayUrl()), assetRewards);
    }

    @Override
    public DDO createMintableDID(AssetMetadata metadata, ProviderConfig providerConfig, AssetRewards assetRewards, BigInteger cap, BigInteger royalties) throws DDOException {
        return neverminedManager.registerAccessServiceAsset(metadata, providerConfig, new AuthConfig(providerConfig.getGatewayUrl()), assetRewards, cap, royalties);
    }


    @Override
    public DDO createComputeService(AssetMetadata metadata, ProviderConfig providerConfig) throws DDOException {
        final AssetRewards assetRewards = new AssetRewards(neverminedManager.getMainAccount().getAddress(), metadata.attributes.main.price);
        return neverminedManager.registerComputeService(metadata, providerConfig, new ComputingService.Provider(), assetRewards);
    }

    @Override
    public DDO createComputeService(AssetMetadata metadata, ProviderConfig providerConfig, ComputingService.Provider computingProvider, AssetRewards assetRewards) throws DDOException {
        return neverminedManager.registerComputeService(metadata, providerConfig, computingProvider, assetRewards);
    }

    @Override
    public List<ComputeLogs> getComputeLogs(String serviceAgreementId, String executionId,
                                            ProviderConfig providerConfig) throws ServiceException {
        return neverminedManager.getComputeLogs(serviceAgreementId, executionId, providerConfig);
    }

    @Override
    public ComputeStatus getComputeStatus(String serviceAgreementId, String executionId,
                                          ProviderConfig providerConfig) throws ServiceException {
        return neverminedManager.getComputeStatus(serviceAgreementId, executionId, providerConfig);
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
        } catch (Exception e) {
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
    public Boolean download(String serviceAgreementId, DID did, int serviceIndex, String basePath) throws DownloadServiceException {
        return neverminedManager.access(serviceAgreementId, did, serviceIndex, basePath);
    }

    @Override
    public Boolean download(String serviceAgreementId, DID did, int serviceIndex, int fileIndex, String basePath) throws DownloadServiceException {
        return neverminedManager.access(serviceAgreementId, did, serviceIndex, fileIndex, basePath);
    }


    @Override
    public InputStream downloadBinary(String serviceAgreementId, DID did, int serviceIndex) throws DownloadServiceException {
        return this.downloadBinary(serviceAgreementId, did, serviceIndex, 0);
    }

    @Override
    public InputStream downloadBinary(String serviceAgreementId, DID did, int serviceIndex, int fileIndex) throws DownloadServiceException {
        return neverminedManager.consumeBinary(serviceAgreementId, did, serviceIndex,  fileIndex);
    }

    @Override
    public InputStream downloadBinary(String serviceAgreementId, DID did, int serviceIndex, int fileIndex, int rangeStart, int rangeEnd) throws DownloadServiceException {
        return neverminedManager.consumeBinary(serviceAgreementId, did, serviceIndex, fileIndex, true, rangeStart, rangeEnd);
    }


    public Boolean ownerDownload(DID did, int serviceIndex, String basePath) throws ServiceException, DownloadServiceException {
        return neverminedManager.downloadAssetByOwner(did, serviceIndex, basePath);
    }

    public Boolean ownerDownload(DID did, int serviceIndex, String basePath, int fileIndex) throws ServiceException, DownloadServiceException {
        return neverminedManager.downloadAssetByOwner(did, serviceIndex, basePath, fileIndex);
    }

    @Override
    public Flowable<OrderResult> purchaseOrder(DID did, int serviceIndex) throws OrderException {
        return neverminedManager.purchaseAssetFlowable(did, serviceIndex);
    }

    public OrderResult order(DID did) throws OrderException, ServiceException, EscrowPaymentException {
        return neverminedManager.purchaseAssetDirect(did);
    }

    public OrderResult order(DID did, Service.ServiceTypes serviceTypes) throws OrderException, ServiceException, EscrowPaymentException {
        return neverminedManager.purchaseAssetDirect(did, serviceTypes);
    }

    public OrderResult order(DID did, int serviceIndex, Service.ServiceTypes serviceTypes) throws OrderException, ServiceException, EscrowPaymentException {
        return neverminedManager.purchaseAssetDirect(did, serviceIndex, serviceTypes);
    }


    public OrderResult order(DID did, int serviceIndex) throws OrderException, ServiceException, EscrowPaymentException {
        return neverminedManager.purchaseAssetDirect(did, serviceIndex);
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
    public GatewayService.ServiceExecutionResult execute(String agreementId, DID did, int serviceIndex, DID workflowDID) throws ServiceException {
        return neverminedManager.executeComputeService(agreementId, did, serviceIndex, workflowDID);
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

    @Override
    public Boolean addProvider(DID did, String providerAddress) throws EthereumException {
        return assetsManager.addProvider(did, providerAddress);
    }

    @Override
    public Boolean removeProvider(DID did, String providerAddress) throws EthereumException {
        return assetsManager.removeProvider(did, providerAddress);
    }

    @Override
    public List<String> listProviders(DID did) throws EthereumException {
        return assetsManager.listProviders(did);
    }


}
