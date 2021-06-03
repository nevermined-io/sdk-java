package io.keyko.nevermined.manager;

import io.keyko.common.exceptions.CryptoException;
import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.helpers.EthereumHelper;
import io.keyko.common.helpers.UrlHelper;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.api.helper.AccountsHelper;
import io.keyko.nevermined.core.sla.handlers.ServiceAgreementHandler;
import io.keyko.nevermined.exceptions.*;
import io.keyko.nevermined.external.GatewayService;
import io.keyko.nevermined.external.GatewayService.AccessTokenResult;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.models.AssetRewards;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.Order;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.asset.OrderResult;
import io.keyko.nevermined.models.gateway.ComputeLogs;
import io.keyko.nevermined.models.gateway.ComputeStatus;
import io.keyko.nevermined.models.gateway.ExecuteService;
import io.keyko.nevermined.models.service.*;
import io.keyko.nevermined.models.service.types.*;
import io.reactivex.Flowable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Handles several operations related with Ocean's flow
 */
public class NeverminedManager extends BaseManager {

    private static final Logger log = LogManager.getLogger(NeverminedManager.class);
    private AgreementsManager agreementsManager;
    private TemplatesManager templatesManager;
    private ConditionsManager conditionsManager;
    private AccountsManager accountsManager;
    private HashMap<String, String> tokenCache;

    protected NeverminedManager(KeeperService keeperService, MetadataApiService metadataApiService) {
        super(keeperService, metadataApiService);
        tokenCache = new HashMap<String, String>();
    }

    /**
     * Given the KeeperService and MetadataApiService, returns a new instance of
     * OceanManager using them as attributes
     *
     * @param keeperService      Keeper Dto
     * @param metadataApiService Provider Dto
     * @return NeverminedManager
     */
    public static NeverminedManager getInstance(KeeperService keeperService, MetadataApiService metadataApiService) {
        return new NeverminedManager(keeperService, metadataApiService);
    }

    public NeverminedManager setAgreementManager(AgreementsManager agreementManager) {
        this.agreementsManager = agreementManager;
        return this;
    }

    public NeverminedManager setTemplatesManager(TemplatesManager templatesManager) {
        this.templatesManager = templatesManager;
        return this;
    }

    public NeverminedManager setConditionsManager(ConditionsManager conditionsManager) {
        this.conditionsManager = conditionsManager;
        return this;
    }

    public NeverminedManager setAccountsManager(AccountsManager accountsManager) {
        this.accountsManager = accountsManager;
        return this;
    }

    /**
     * Given a DDO, returns a DID created using the ddo
     *
     * @param ddo the DDO
     * @return DID
     * @throws DIDFormatException DIDFormatException
     */
    public DID generateDID(DDO ddo) throws DIDFormatException {
        return DID.builder();
    }

    /**
     * Given a DID and a Metadata API url, register on-chain the DID. It allows to
     * resolve DDO's using DID's as input
     *
     * @param did       the did
     * @param url       metadata url
     * @param checksum  calculated hash of the metadata
     * @param providers list of providers addresses to give access
     * @return boolean success
     * @throws DIDRegisterException DIDRegisterException
     */
    public boolean registerDID(DID did, String url, String checksum, List<String> providers)
            throws DIDRegisterException {
        log.debug("Registering DID " + did.getHash() + " into Registry " + didRegistry.getContractAddress());

        try {

            TransactionReceipt receipt = didRegistry.registerAttribute(EncodingHelper.hexStringToBytes(did.getHash()),
                    EncodingHelper.hexStringToBytes(checksum.replace("0x", "")), providers, url).send();

            return receipt.getStatus().equals("0x1");

        } catch (Exception e) {
            throw new DIDRegisterException("Error registering DID " + did.getHash(), e);
        }
    }


    /**
     * Given a DID and a Metadata API url, register on-chain the DID. It allows to
     * resolve DDO's using DID's as input
     *
     * @param did       the did
     * @param url       metadata url
     * @param checksum  calculated hash of the metadata
     * @param providers list of providers addresses to give access
     * @return boolean success
     * @throws DIDRegisterException DIDRegisterException
     */
    public boolean registerMintableDID(DID did, String url, String checksum, List<String> providers, BigInteger cap, BigInteger royalties)
            throws DIDRegisterException {
        log.debug("Registering Mintable DID " + did.getHash() + " into Registry " + didRegistry.getContractAddress());

        try {

            TransactionReceipt receipt = didRegistry.registerMintableDID(
                    EncodingHelper.hexStringToBytes(did.getHash()),
                    EncodingHelper.hexStringToBytes(checksum.replace("0x", "")),
                    providers,
                    url,
                    cap,
                    royalties,
                    EncodingHelper.hexStringToBytes(did.getHash()),
                    ""
            ).send();

            return receipt.getStatus().equals("0x1");

        } catch (Exception e) {
            throw new DIDRegisterException("Error registering DID " + did.getHash(), e);
        }
    }

    /**
     * Given a seed and a public address it hash to generate a DID
     *
     * @param seed      the DID hash
     * @param address   the creator address used to calculate the final DID
     * @return DID the new DID generated
     * @throws DIDRegisterException DIDRegisterException
     */
    public DID hashDID(String seed, String address)
            throws DIDRegisterException {
        log.debug("Hashing DID Seed " + seed);

        try {
            return DID.getFromHash(EncodingHelper.toHexString(
                    didRegistry.hashDID(EncodingHelper.hexStringToBytes(seed), Keys.toChecksumAddress(address))
                            .send()
            ));

        } catch (Exception e) {
            throw new DIDRegisterException("Error Hashing DID ", e);
        }
    }

    private Map<String, Object> buildBasicAccessServiceConfiguration(ProviderConfig providerConfig, AssetRewards assetRewards,
                                                                     String creatorAddress) {

        Map<String, Object> configuration = new HashMap<>();
        configuration.put("providerConfig", providerConfig);
        configuration.put("accessServiceTemplateId", accessTemplate.getContractAddress());
        configuration.put("accessConditionAddress", accessCondition.getContractAddress());
        configuration.put("price", assetRewards.totalPrice);
        configuration.put("rewards", assetRewards.rewards);
        configuration.put("creator", creatorAddress);

        return configuration;

    }

    private Map<String, Object> buildBasicComputingServiceConfiguration(ProviderConfig providerConfig,
                                                                        ComputingService.Provider computingProvider, AssetRewards assetRewards, String creatorAddress) {

        Map<String, Object> configuration = new HashMap<>();
        configuration.put("providerConfig", providerConfig);
        configuration.put("computingProvider", computingProvider);
        configuration.put("computingServiceTemplateId", escrowComputeExecutionTemplate.getContractAddress());
        configuration.put("execComputeConditionAddress", computeExecutionCondition.getContractAddress());
        configuration.put("price", assetRewards.totalPrice);
        configuration.put("rewards", assetRewards.rewards);
        configuration.put("creator", creatorAddress);

        return configuration;

    }

    /**
     * Creates a new DDO with an AccessService
     *
     * @param metadata       the metadata
     * @param providerConfig the service Endpoints
     * @param assetsRewards  rewards associated to the asset
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO registerAccessServiceAsset(AssetMetadata metadata, ProviderConfig providerConfig, AssetRewards assetsRewards) throws DDOException {

        return registerAccessServiceAsset(metadata, providerConfig,
                new AuthConfig(providerConfig.getGatewayUrl(), AuthorizationService.AuthTypes.PSK_RSA), assetsRewards);
    }

    /**
     * Creates a new DDO with an AccessService
     *
     * @param metadata       the metadata
     * @param providerConfig the service Endpoints
     * @param authConfig     Authorization config
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO registerAccessServiceAsset(AssetMetadata metadata, ProviderConfig providerConfig, AuthConfig authConfig) throws DDOException {
        final AssetRewards assetRewards = new AssetRewards(mainAccount.address, metadata.attributes.main.price);
        return registerAccessServiceAsset(metadata, providerConfig,
                new AuthConfig(providerConfig.getGatewayUrl(), AuthorizationService.AuthTypes.PSK_RSA), assetRewards);
    }

    /**
     * Creates a new DDO with an AccessService
     *
     * @param metadata       the metadata
     * @param providerConfig the service Endpoints
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO registerAccessServiceAsset(AssetMetadata metadata, ProviderConfig providerConfig) throws DDOException {
        final AssetRewards assetRewards = new AssetRewards(mainAccount.address, metadata.attributes.main.price);
        return registerAccessServiceAsset(metadata, providerConfig,
                new AuthConfig(providerConfig.getGatewayUrl(), AuthorizationService.AuthTypes.PSK_RSA), assetRewards);
    }

    /**
     * Creates a new DDO with an AccessService
     *
     * @param metadata       the metadata
     * @param providerConfig the service Endpoints
     * @param authConfig     auth configuration
     * @param assetRewards   asset rewards distribution
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO registerAccessServiceAsset(AssetMetadata metadata, ProviderConfig providerConfig, AuthConfig authConfig, AssetRewards assetRewards)
            throws DDOException {
        return registerAccessServiceAsset(metadata, providerConfig, authConfig, assetRewards, BigInteger.valueOf(-1), BigInteger.ZERO);
    }

    /**
     * Creates a new DDO with an AccessService
     *
     * @param metadata       the metadata
     * @param providerConfig the service Endpoints
     * @param authConfig     auth configuration
     * @param assetRewards   asset rewards distribution
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO registerAccessServiceAsset(AssetMetadata metadata, ProviderConfig providerConfig, AuthConfig authConfig, AssetRewards assetRewards, BigInteger cap, BigInteger royalties)
            throws DDOException {

        try {
            Map<String, Object> configuration = buildBasicAccessServiceConfiguration(providerConfig,
                    assetRewards, getMainAccount().address);
            Service accessService = ServiceBuilder.getServiceBuilder(Service.ServiceTypes.ACCESS, assetRewards)
                    .buildService(configuration);

            return registerAsset(metadata, providerConfig, accessService, authConfig, assetRewards, cap, royalties);

        } catch (ServiceException e) {
            throw new DDOException("Error registering Asset.", e);
        }
    }

    /**
     * Creates a new DDO with a ComputeService
     *
     * @param metadata          the metadata
     * @param providerConfig    the service Endpoints
     * @param computingProvider the data relative to the provider
     * @param assetRewards   asset rewards distribution
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO registerComputeService(AssetMetadata metadata, ProviderConfig providerConfig,
                                      ComputingService.Provider computingProvider, AssetRewards assetRewards) throws DDOException {

        try {

            Map<String, Object> configuration = buildBasicComputingServiceConfiguration(providerConfig,
                    computingProvider, assetRewards, getMainAccount().address);
            Service computingService = ServiceBuilder.getServiceBuilder(Service.ServiceTypes.COMPUTE, assetRewards)
                    .buildService(configuration);

            computingService.serviceEndpoint = providerConfig.getExecuteEndpoint();
            return registerAsset(metadata, providerConfig, computingService,
                    new AuthConfig(providerConfig.getGatewayUrl()), assetRewards);

        } catch (ServiceException e) {
            throw new DDOException("Error registering Asset.", e);
        }

    }

    /**
     * Creates a new DDO, registering it on-chain through DidRegistry contract and
     * off-chain in Metadata Api
     *
     * @param metadata       the metadata
     * @param providerConfig the service Endpoints
     * @param service        the service
     * @param authConfig     auth configuration
     * @param assetRewards   asset rewards distribution
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    private DDO registerAsset(AssetMetadata metadata, ProviderConfig providerConfig, Service service,
                              AuthConfig authConfig, AssetRewards assetRewards) throws DDOException {
        return registerAsset(metadata, providerConfig, service, authConfig, assetRewards, BigInteger.valueOf(-1), BigInteger.ZERO);
    }


        /**
         * Creates a new DDO, registering it on-chain through DidRegistry contract and
         * off-chain in Metadata Api
         *
         * @param metadata       the metadata
         * @param providerConfig the service Endpoints
         * @param service        the service
         * @param authConfig     auth configuration
         * @param assetRewards   asset rewards distribution
         * @return an instance of the DDO created
         * @throws DDOException DDOException
         */
    private DDO registerAsset(AssetMetadata metadata, ProviderConfig providerConfig, Service service,
                              AuthConfig authConfig, AssetRewards assetRewards,
                              BigInteger cap, BigInteger royalties) throws DDOException {

        try {

            // Definition of service endpoints
            String metadataEndpoint;
            if (providerConfig.getMetadataEndpoint() == null)
                metadataEndpoint = getMetadataApiService().getDdoEndpoint() + "/{did}";
            else
                metadataEndpoint = providerConfig.getMetadataEndpoint();

            // Initialization of services supported for this asset
            MetadataService metadataService = new MetadataService(metadata, metadataEndpoint,
                    Service.DEFAULT_METADATA_INDEX);

            ProvenanceService provenanceService = new ProvenanceService(providerConfig.getMetadataEndpoint(),
                    Service.DEFAULT_PROVENANCE_INDEX);

            AuthorizationService authorizationService = null;
            if (null != authConfig) {
                if (authConfig.getService().equals(AuthorizationService.AuthTypes.SECRET_STORE))
                    authorizationService = AuthorizationService.buildSecretStoreAuthService(
                            providerConfig.getSecretStoreEndpoint(), Service.DEFAULT_AUTHORIZATION_INDEX,
                            authConfig.getThreshold());
                else if (authConfig.getService().equals(AuthorizationService.AuthTypes.PSK_ECDSA))
                    authorizationService = AuthorizationService.buildECDSAAuthService(providerConfig.getGatewayUrl(),
                            Service.DEFAULT_AUTHORIZATION_INDEX);
                else if (authConfig.getService().equals(AuthorizationService.AuthTypes.PSK_RSA))
                    authorizationService = AuthorizationService.buildRSAAuthService(providerConfig.getGatewayUrl(),
                            Service.DEFAULT_AUTHORIZATION_INDEX);
            }

            // Initializing DDO
            DDO ddo = this.buildDDO(metadataService, getMainAccount().address);

            // Adding services to DDO
            ddo.addService(service);
            ddo.addService(provenanceService);

            if (authorizationService != null)
                ddo.addService(authorizationService);

            // Generating the DDO.proof, checksums and calculating DID
            ddo = ddo.integrityBuilder(getKeeperService().getCredentials());

            // Add authentication
            ddo.addAuthentication(ddo.id);

            if (service instanceof AccessService || service instanceof ComputingService) {
                if (authConfig.getService().equals(AuthorizationService.AuthTypes.SECRET_STORE))
                    ddo.secretStoreLocalEncryptFiles(getSecretStoreManager(), authConfig);
                else if (authConfig.getService().equals(AuthorizationService.AuthTypes.PSK_ECDSA)
                        || authConfig.getService().equals(AuthorizationService.AuthTypes.PSK_RSA))
                    ddo.gatewayEncryptFiles(authConfig);
            }

            List<Condition> conditions= ServiceBuilder.getGenericConditionParams(ddo, service, config, assetRewards);

            Service theService = ddo.getService(service.index);
            theService.attributes.serviceAgreementTemplate.conditions = conditions;

            // Substitution of the did token in the url. The ddo will be registered using
            // the complete metadata url
            metadataEndpoint = UrlHelper.parseDDOUrl(metadataEndpoint, ddo.getDID().toString());

            // Registering DID
            boolean success;
            if (cap.compareTo(BigInteger.ZERO) >= 0) {
                success = registerMintableDID(ddo.fetchDIDSeed(), metadataEndpoint, ddo.getDID().getHash(), providerConfig.getProviderAddresses(), cap, royalties);
            }   else {
                success = registerDID(ddo.fetchDIDSeed(), metadataEndpoint, ddo.getDID().getHash(), providerConfig.getProviderAddresses());
            }

            if (!success)
                throw new DIDRegisterException("Error registering DID on-chain");
            // Storing DDO
            return getMetadataApiService().createDDO(ddo);

        } catch (DDOException | DIDRegisterException | IOException | CipherException | ServiceException | DIDFormatException e) {
            throw new DDOException("Error registering Asset.", e);
        }

    }

    public boolean isConditionFulfilled(String serviceAgreementId, Condition.ConditionTypes conditionType)
            throws Exception {
        final int maxRetries = 5;
        final long sleepTime = 500l;
        int iteration = 0;

        while (iteration < maxRetries) {
            AgreementStatus status = agreementsManager.getStatus(serviceAgreementId);
            BigInteger conditionStatus = status.conditions.get(0).conditions.get(conditionType.toString());
            log.debug("Condition check[" + conditionType.toString() + "] :" + conditionStatus);
            if (conditionStatus.equals(Condition.ConditionStatus.Fulfilled.getStatus())) // Condition is fullfilled
                return true;
            iteration++;
            Thread.sleep(sleepTime);
        }
        return false;
    }

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service
     * Agreement between publisher and consumer
     *
     * @param did the did
     * @return true if the asset was purchased successfully, if not false
     * @throws OrderException        OrderException
     * @throws ServiceException      ServiceException
     * @throws EscrowPaymentException EscrowPaymentException
     */
    public OrderResult purchaseAssetDirect(DID did) throws OrderException, ServiceException, EscrowPaymentException {
        return purchaseAssetDirect(did, -1, Service.ServiceTypes.ACCESS);
    }

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service
     * Agreement between publisher and consumer
     *
     * @param did          the did
     * @param serviceIndex the index of the service
     * @return true if the asset was purchased successfully, if not false
     * @throws OrderException        OrderException
     * @throws ServiceException      ServiceException
     * @throws EscrowPaymentException EscrowPaymentException
     */
    public OrderResult purchaseAssetDirect(DID did, int serviceIndex)
            throws OrderException, ServiceException, EscrowPaymentException {
        return purchaseAssetDirect(did, serviceIndex, null);
    }

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service
     * Agreement between publisher and consumer
     *
     * @param did         the did
     * @param serviceType Service to purchase
     * @return true if the asset was purchased successfully, if not false
     * @throws OrderException        OrderException
     * @throws ServiceException      ServiceException
     * @throws EscrowPaymentException EscrowPaymentException
     */
    public OrderResult purchaseAssetDirect(DID did, Service.ServiceTypes serviceType)
            throws OrderException, ServiceException, EscrowPaymentException {
        return purchaseAssetDirect(did, -1, serviceType);
    }

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service
     * Agreement between publisher and consumer
     *
     * @param did          the did
     * @param serviceIndex the index of the service
     * @param serviceType  Service to purchase
     * @return true if the asset was purchased successfully, if not false
     * @throws OrderException        OrderException
     * @throws ServiceException      ServiceException
     */
    public OrderResult purchaseAssetDirect(DID did, int serviceIndex, Service.ServiceTypes serviceType)
            throws OrderException, ServiceException {

        String serviceAgreementId = ServiceAgreementHandler.generateSlaId();
        OrderResult orderResult;
        DDO ddo;
        // Checking if DDO is already there and serviceIndex is included
        try {
            ddo = resolveDID(did);
        } catch (DDOException e) {
            log.error("Error resolving did[" + did.getHash() + "]: " + e.getMessage());
            throw new OrderException("Error processing Order with DID " + did.getDid(), e);
        }

        Service service;
        if (serviceIndex >= 0) {
            service = ddo.getService(serviceIndex);
        } else if (serviceType.toString().equalsIgnoreCase(Service.ServiceTypes.COMPUTE.toString())) {
            service = ddo.getComputeService();
            serviceIndex = service.index;
        } else {
            service = ddo.getAccessService();
            serviceIndex = service.index;
        }

        try {
            // Step 1. We initialize the Service Agreement
            final boolean isInitialized = initializeServiceAgreementDirect(ddo, serviceIndex, serviceAgreementId);
            if (!isInitialized) {
                throw new ServiceAgreementException(serviceAgreementId, "Service Agreement not Initialized");
            }
        } catch (ServiceAgreementException e) {
            String msg = "Error processing Order with DID " + did.getDid() + "and ServiceAgreementID "
                    + serviceAgreementId;
            log.error(msg + ": " + e.getMessage());
            throw new OrderException(msg, e);
        }

        final String eventServiceAgreementId = EthereumHelper.add0x(serviceAgreementId);

        try {
            log.debug("Service Agreement " + serviceAgreementId + " initialized successfully");
            final BigInteger totalPrice = service.fetchTotalPrice();
            final String tokenAddress = conditionsManager.getTokenAddress(
                    service.fetchConditionValue("_tokenAddress"));
            BigInteger balance;
            if (!tokenAddress.equals(AccountsHelper.ZERO_ADDRESS))  {
                balance = tokenContract.balanceOf(getMainAccount().address).send();
                tokenApprove(tokenContract, lockCondition.getContractAddress(), totalPrice.toString());
            }   else    {
                balance = accountsManager.getEthAccountBalance(getMainAccount().address);
            }

            if (balance.compareTo(totalPrice) < 0) {
                log.warn("Consumer account does not have sufficient token balance to fulfill the "
                        + "LockPaymentCondition. Do `requestTokens` using the `dispenser` contract then try this again.");
                log.warn("token balance is: " + balance + " price is: " + totalPrice);
                throw new LockPaymentFulfillException(
                        "LockPaymentCondition.fulfill will fail due to insufficient token balance in the consumer account.");
            }
        } catch (TokenApproveException | LockPaymentFulfillException e) {
            String msg = "Error approving token";
            log.error(msg + ": " + e.getMessage());
            throw new OrderException(msg, e);
        } catch (Exception e) {
            String msg = "Token Transaction error";
            log.error(msg + ": " + e.getMessage());
            throw new OrderException(msg, e);
        }

        try {
            // Step 2. We fulfill the Lock Payment (we make the payment)
            this.fulfillLockPaymentCondition(eventServiceAgreementId, serviceIndex);
            final boolean isFulfilled = isConditionFulfilled(serviceAgreementId, Condition.ConditionTypes.lockPayment);
            orderResult = new OrderResult(serviceAgreementId, isFulfilled, false, serviceIndex);

        } catch (LockPaymentFulfillException e) {
            this.fulfillEscrowPaymentCondition(serviceAgreementId, serviceIndex);
            return new OrderResult(serviceAgreementId, false, true);
        } catch (Exception e) {
            this.fulfillEscrowPaymentCondition(serviceAgreementId, serviceIndex);
            return new OrderResult(serviceAgreementId, false, true);
        }
        return orderResult;

    }

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service
     * Agreement between publisher and consumer
     *
     * @param did          the did
     * @param serviceIndex the index of the service
     * @return a Flowable instance over an OrderResult to get the result of the flow
     *         in an asynchronous fashion
     * @throws OrderException OrderException
     */
    public Flowable<OrderResult> purchaseAssetFlowable(DID did, int serviceIndex) throws OrderException {

        String serviceAgreementId = ServiceAgreementHandler.generateSlaId();

        DDO ddo;
        // Checking if DDO is already there and serviceIndex is included
        try {

            ddo = resolveDID(did);
        } catch (DDOException e) {
            log.error("Error resolving did[" + did.getHash() + "]: " + e.getMessage());
            throw new OrderException("Error processing Order with DID " + did.getDid(), e);
        }

        try {

            Service service = ddo.getService(serviceIndex);

            return this.initializeServiceAgreementFlowable(ddo, serviceIndex, serviceAgreementId).firstOrError()
                    .toFlowable().switchMap(eventServiceAgreementId -> {
                        if (eventServiceAgreementId.isEmpty())
                            return Flowable.empty();
                        else {
                            log.debug("Received AgreementCreated Event with Id: " + eventServiceAgreementId);
                            String price = ddo.getMetadataService().attributes.main.price;
                            tokenApprove(this.tokenContract, lockCondition.getContractAddress(), price);
                            BigInteger balance = this.tokenContract.balanceOf(getMainAccount().address).send();
                            if (balance.compareTo(new BigInteger(price)) < 0) {
                                log.warn("Consumer account does not have sufficient token balance to fulfill the "
                                        + "LockPaymentCondition. Do `requestTokens` using the `dispenser` contract then try this again.");
                                log.info("token balance is: " + balance + " price is: " + price);
                                throw new Exception(
                                        "LockPaymentCondition.fulfill will fail due to insufficient token balance in the consumer account.");
                            }
                            this.fulfillLockPaymentCondition(eventServiceAgreementId, serviceIndex);
                            Flowable<String> conditionFulilledEvent = null;

                            if (service.type.equals(Service.ServiceTypes.ACCESS.toString()))
                                conditionFulilledEvent = ServiceAgreementHandler
                                        .listenForFulfilledEvent(accessCondition, serviceAgreementId);
                            else if (service.type.equals(Service.ServiceTypes.COMPUTE.toString()))
                                conditionFulilledEvent = ServiceAgreementHandler
                                        .listenForFulfilledEvent(computeExecutionCondition, serviceAgreementId);
                            else
                                throw new ServiceAgreementException(serviceAgreementId, "Service type not supported");

                            return conditionFulilledEvent;
                        }
                    }).map(event -> new OrderResult(serviceAgreementId, true, false, serviceIndex))
                    // TODO timout of the condition
                    .timeout(120, TimeUnit.SECONDS).onErrorReturn(throwable -> {

                        if (throwable instanceof TimeoutException) {
                            // If we get a timeout listening for a Condition Fulfilled Event,
                            // we must perform a refund executing escrowPayment.fulfill
                            this.fulfillEscrowPaymentCondition(serviceAgreementId, serviceIndex);
                            return new OrderResult(serviceAgreementId, false, true);
                        }

                        String msg = "There was a problem executing the Service Agreement " + serviceAgreementId;
                        throw new ServiceAgreementException(serviceAgreementId, msg, throwable);
                    });

        } catch (ServiceException | ServiceAgreementException e) {
            String msg = "Error processing Order with DID " + did.getDid() + "and ServiceAgreementID "
                    + serviceAgreementId;
            log.error(msg + ": " + e.getMessage());
            throw new OrderException(msg, e);
        }

    }

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service
     * Agreement between publisher and consumer
     *
     * @param did          the did
     * @param serviceIndex the service index in the ddo to download
     * @param basePath     path where we want to download the asset files
     * @return true if the asset was purchased successfully, if not false
     * @throws ServiceException        ServiceException
     * @throws DownloadServiceException DownloadServiceException
     */
    public boolean downloadAssetByOwner(DID did, int serviceIndex, String basePath)
            throws ServiceException, DownloadServiceException {
        return downloadAssetByOwner(did, serviceIndex, basePath, 0);
    }

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service
     * Agreement between publisher and consumer
     *
     * @param did          the did
     * @param serviceIndex the service index in the ddo to download
     * @param basePath     path where we want to download the asset files
     * @param fileIndex    index of the file inside the files definition in metadata
     * @return true if the asset was purchased successfully, if not false
     * @throws ServiceException        ServiceException
     * @throws DownloadServiceException DownloadServiceException
     */
    public boolean downloadAssetByOwner(DID did, int serviceIndex, String basePath, int fileIndex)
            throws ServiceException, DownloadServiceException {

        Service service;
        DDO ddo;
        // Checking if DDO is already there and serviceIndex is included
        try {
            ddo = resolveDID(did);
        } catch (DDOException e) {
            log.error("Error resolving did[" + did.getHash() + "]: " + e.getMessage());
            throw new DownloadServiceException("Error resolving did " + did.getDid(), e);
        }

        if (serviceIndex >= 0) {
            service = ddo.getService(serviceIndex);
        } else {
            service = ddo.getAccessService();
            serviceIndex = service.index;
        }

        Map<String, Object> consumeData = fetchAssetDataBeforeConsume(did, serviceIndex);
        // For direct access by owners we replace the /access URI by /download
        String serviceEndpoint = ((String) consumeData.get("serviceEndpoint")).replace("/access", "/download");
        List<AssetMetadata.File> files = (List<AssetMetadata.File>) consumeData.get("files");
        String checkConsumerAddress = Keys.toChecksumAddress(getMainAccount().address);

        // Get Access Token
        String accessToken = getDownloadAccessToken(serviceEndpoint, did);

        // getConsumeData returns a list with only one file in case of consuming by
        // index
        for (AssetMetadata.File file : files) {

            try {
                String destinationPath = buildDestinationPath(basePath, did, fileIndex, file);
                GatewayService.downloadToPathByOwner(serviceEndpoint, checkConsumerAddress, did.getDid(), file.index,
                        accessToken, destinationPath, false, 0, 0);

            } catch (IOException e) {
                String msg = "Error downloading asset by owner with DID " + did.getDid();

                log.error(msg + ": " + e.getMessage());
                throw new DownloadServiceException(msg, e);
            }
        }
        return true;
    }

    public List<byte[]> generateServiceConditionsId(String serviceAgreementId, String consumerAddress, DDO ddo,
                                                    int serviceIndex) throws ServiceAgreementException, ServiceException {

        Service service = ddo.getService(serviceIndex);

        final List<String> conditionIds = conditionsManager.generateAgreementConditionIds(
                service.fetchServiceType(),
                serviceAgreementId,
                Keys.toChecksumAddress(consumerAddress),
                ddo,
                serviceIndex);
        return Service.transformConditionIdsToByte(conditionIds);
//
//        Map<String, String> conditionsAddresses = new HashMap<>();
//        conditionsAddresses.put("escrowPaymentAddress", escrowCondition.getContractAddress());
//        conditionsAddresses.put("lockPaymentConditionAddress", lockCondition.getContractAddress());
//
//        if (service.type.equals(Service.ServiceTypes.ACCESS.toString())) {
//            conditionsAddresses.put("accessConditionAddress",
//                    accessCondition.getContractAddress());
//            service = (AccessService) service;
//        } else if (service.type.equals(Service.ServiceTypes.COMPUTE.toString())) {
//            conditionsAddresses.put("computeExecutionConditionAddress", computeExecutionCondition.getContractAddress());
//            service = (ComputingService) service;
//        } else
//            throw new ServiceAgreementException(serviceAgreementId, "Service type not supported");
//
//        List<byte[]> conditionsId;
//
//        try {
//
//            conditionsId = service.generateByteConditionIds(serviceAgreementId, conditionsAddresses, ddo.proof.creator,
//                    Keys.toChecksumAddress(consumerAddress));
//        } catch (Exception e) {
//            throw new ServiceAgreementException(serviceAgreementId, "Exception generating conditions id", e);
//        }
//
//        return conditionsId;

    }

    /**
     * Initialize a new ServiceExecutionAgreement between a publisher and a consumer
     *
     * @param ddo                the ddo
     * @param serviceIndex       the service index
     * @param serviceAgreementId the service agreement id
     * @return true if the agreement was initialized correctly, if not false
     * @throws ServiceException          ServiceException
     * @throws ServiceAgreementException ServiceAgreementException
     */
    private boolean initializeServiceAgreement(DDO ddo, int serviceIndex, String serviceAgreementId)
            throws ServiceException, ServiceAgreementException {

        Service service = ddo.getService(serviceIndex);

        Boolean isTemplateApproved;
        try {
            isTemplateApproved = templatesManager.isTemplateApproved(service.templateId);
        } catch (EthereumException e) {
            String msg = "Error creating Service Agreement: " + serviceAgreementId + ". Error verifying template "
                    + service.templateId;
            log.error(msg + ": " + e.getMessage());
            throw new ServiceAgreementException(serviceAgreementId, msg, e);
        }

        if (!isTemplateApproved)
            throw new ServiceAgreementException(serviceAgreementId,
                    "The template " + service.templateId + " is not approved");

        Boolean result = false;

        try {
            List<byte[]> conditionsId = generateServiceConditionsId(serviceAgreementId,
                    getMainAccount().getAddress(), ddo, serviceIndex);

            if (service.type.equals(Service.ServiceTypes.ACCESS.toString()))
                result = this.agreementsManager.createAccessAgreement(serviceAgreementId, ddo, conditionsId,
                        Keys.toChecksumAddress(getMainAccount().getAddress()), service);
            else if (service.type.equals(Service.ServiceTypes.COMPUTE.toString()))
                result = this.agreementsManager.createComputeAgreement(serviceAgreementId, ddo, conditionsId,
                        Keys.toChecksumAddress(getMainAccount().getAddress()), service);
            else
                throw new ServiceAgreementException(serviceAgreementId, "Service type not supported");

            if (!result)
                return checkAgreementStatus(serviceAgreementId);

        } catch (Exception e) {
            String msg = "Error creating Service Agreement: " + serviceAgreementId;
            log.error(msg + ": " + e.getMessage());
            throw new ServiceAgreementException(serviceAgreementId, msg, e);
        }
        return false;
    }

    /**
     * Initialize a new ServiceExecutionAgreement between a publisher and a consumer
     *
     * @param ddo                the ddo
     * @param serviceIndex       the service index
     * @param serviceAgreementId the service agreement id
     * @return true if the agreement was initialized correctly, if not false
     * @throws ServiceException          ServiceException
     * @throws ServiceAgreementException ServiceAgreementException
     */
    protected boolean initializeServiceAgreementDirect(DDO ddo, int serviceIndex, String serviceAgreementId)
            throws ServiceException, ServiceAgreementException {

        boolean initializationStatus = initializeServiceAgreement(ddo, serviceIndex, serviceAgreementId);
        if (!initializationStatus)
            return checkAgreementStatus(serviceAgreementId);
        return false;
    }

    /**
     * Initialize a new ServiceExecutionAgreement between a publisher and a consumer
     * return a flowable to listen contract initialization events
     *
     * @param ddo                the ddo
     * @param serviceIndex       the service index
     * @param serviceAgreementId the service agreement id
     * @return a Flowable over an AgreementInitializedEventResponse
     * @throws ServiceException          ServiceException
     * @throws ServiceAgreementException ServiceAgreementException
     */
    protected Flowable<String> initializeServiceAgreementFlowable(DDO ddo, int serviceIndex, String serviceAgreementId)
            throws ServiceException, ServiceAgreementException {

        boolean initializationStatus = initializeServiceAgreement(ddo, serviceIndex, serviceAgreementId);
        boolean isInitialized = false;
        if (!initializationStatus)
            isInitialized = checkAgreementStatus(serviceAgreementId);

        if (!isInitialized)
            throw new ServiceAgreementException(serviceAgreementId, "Service Agreement not initialized correctly");

        Service service = ddo.getService(serviceIndex);

        // 4. Listening of events
        Flowable<String> executeAgreementFlowable = null;

        if (service.type.equals(Service.ServiceTypes.ACCESS.toString()))
            executeAgreementFlowable = ServiceAgreementHandler.listenExecuteAgreement(accessTemplate,
                    serviceAgreementId);
        else if (service.type.equals(Service.ServiceTypes.COMPUTE.toString()))
            executeAgreementFlowable = ServiceAgreementHandler.listenExecuteAgreement(escrowComputeExecutionTemplate,
                    serviceAgreementId);
        else
            throw new ServiceAgreementException(serviceAgreementId, "Service type not supported");

        return executeAgreementFlowable;
    }

    private boolean checkAgreementStatus(String serviceAgreementId) throws ServiceAgreementException {

        Boolean result = false;
        try {
            if (!result) {
                int retries = 10;
                int sleepTime = 1000;
                for (int i = 0; i < retries && !result; i++) {
                    log.debug("Checking if the agreement is on-chain...");
                    Agreement agreement = agreementsManager.getAgreement(serviceAgreementId);
                    if (!agreement.templateId.equals("0x0000000000000000000000000000000000000000")) {
                        return true;
                    }
                    Thread.sleep(sleepTime);
                }
            }
        } catch (Exception e) {
            throw new ServiceAgreementException(serviceAgreementId, "There was a problem checking the status", e);
        }

        if (!result)
            throw new ServiceAgreementException(serviceAgreementId, "The create Agreement Transaction has failed");

        return false;
    }

    /**
     * Executes the fulfill of the LockPaymentCondition
     *
     * @param serviceAgreementId service agreement id
     * @param serviceIndex       the index of the service
     * @return indicates if the function was executed correctly
     * @throws ServiceException           ServiceException
     * @throws LockPaymentFulfillException LockPaymentFulfillException
     */
    private boolean fulfillLockPaymentCondition(String serviceAgreementId, int serviceIndex)
            throws ServiceException, LockPaymentFulfillException {

        try {
            return conditionsManager.lockPayment(serviceAgreementId, serviceIndex);
        } catch (Exception e) {
            log.error("Unable to fulfill LockPayment: " + e.getMessage());
            return false;
        }
//        Service service = ddo.getService(serviceIndex);
//        String price = service.attributes.main.price;
//
//        return FulfillLockReward.executeFulfill(lockCondition, serviceAgreementId,
//                this.escrowCondition.getContractAddress(), price);
    }

    /**
     * Executes the fulfill of the EscrowPaymentCondition
     *
     * @param serviceAgreementId service agreement id
     * @param serviceIndex       the index of the service
     * @return a flag that indicates if the function was executed correctly
     * @throws ServiceException      ServiceException
     * @throws EscrowPaymentException EscrowPaymentException
     */
    private boolean fulfillEscrowPaymentCondition(String serviceAgreementId, int serviceIndex)
            throws ServiceException {

        final ConditionsManager conditionsManager = ConditionsManager.getInstance(getKeeperService(), getMetadataApiService());
        try {
            conditionsManager.releaseReward(serviceAgreementId, serviceIndex);
        } catch (Exception e) {
            log.error("Unable to fulfill LockPayment: " + e.getMessage());
            return false;
        }
        return true;

//        Service service = ddo.getService(serviceIndex);
//        String totalPrice = service.attributes.main.price;
//        final Condition escrowRewardCondition = service.getConditionbyName(Condition.ConditionTypes.escrowPayment.name());
//        final Condition.ConditionParameter amountsParameter = escrowRewardCondition.getParameterByName("_amounts");
//        final Condition.ConditionParameter receiversParameter = escrowRewardCondition.getParameterByName("_receivers");
//        String lockRewardConditionId = "";
//        String releaseConditionId = "";
//
//        try {
//
//            lockRewardConditionId = service.buildLockPaymentConditionId(serviceAgreementId, escrowCondition.getContractAddress(),
//                    lockCondition.getContractAddress());
//            String conditionAddress;
//            String conditionName;
//
//            if (service.type.equals(Service.ServiceTypes.ACCESS.toString())) {
//                conditionAddress = accessCondition.getContractAddress();
//                conditionName = "accessSecretStore";
//            } else if (service.type.equals(Service.ServiceTypes.COMPUTE.toString())) {
//                conditionAddress = computeExecutionCondition.getContractAddress();
//                conditionName = "computeExecution";
//            } else
//                throw new ServiceException("Service type not supported");
//
//            releaseConditionId = service.generateReleaseConditionId(serviceAgreementId, getMainAccount().getAddress(),
//                    conditionAddress, conditionName);
//
//        } catch (UnsupportedEncodingException e) {
//            throw new EscrowPaymentException("Error generating the condition Ids ", e);
//        }
//
//        return FulfillEscrowPayment.executeFulfill(escrowCondition, serviceAgreementId,
//                this.lockCondition.getContractAddress(), (List<BigInteger>) amountsParameter.value,
//                (List<String>) receiversParameter.value,
//                lockRewardConditionId, releaseConditionId);
    }

    /**
     * Gets the data needed to download an asset
     *
     * @param did          the did
     * @param serviceIndex the id of the service in the DDO
     * @return a Map with the data needed to consume the asset
     * @throws DownloadServiceException DownloadServiceException
     */
    private Map<String, Object> fetchAssetDataBeforeConsume(DID did, int serviceIndex) throws DownloadServiceException {

        DDO ddo;
        String serviceEndpoint;
        Map<String, Object> data = new HashMap<>();

        try {

            ddo = resolveDID(did);
            serviceEndpoint = ddo.getAccessService(serviceIndex).serviceEndpoint;

            data.put("serviceEndpoint", serviceEndpoint);
            data.put("files", ddo.getMetadataService().attributes.main.files);

        } catch (DDOException | ServiceException e) {
            String msg = "Error getting the data from asset with DID " + did.toString();
            log.error(msg + ": " + e.getMessage());
            throw new DownloadServiceException(msg, e);
        }

        return data;
    }

    /**
     * Downloads an Asset previously ordered through a Service Agreement
     *
     * @param serviceAgreementId the service agreement id
     * @param did                the did
     * @param serviceIndex       the service index in the DDO
     * @param basePath           the path where the asset will be downloaded
     * @return a flag that indicates if the download operation was executed correctly
     * @throws DownloadServiceException DownloadServiceException
     */
    public boolean access(String serviceAgreementId, DID did, int serviceIndex, String basePath)
            throws DownloadServiceException {
        return access(serviceAgreementId, did, serviceIndex, 0, basePath);
    }

    /**
     * Downloads an Asset previously ordered through a Service Agreement
     *
     * @param serviceAgreementId the service agreement id
     * @param did                the did
     * @param serviceIndex       id of the service in the DDO
     * @param fileIndex          of the file inside the files definition in metadata
     * @param basePath           the path where the asset will be downloaded
     * @return a flag that indicates if the download operation was executed correctly
     * @throws DownloadServiceException DownloadServiceException
     */
    public boolean access(String serviceAgreementId, DID did, int serviceIndex, int fileIndex, String basePath)
            throws DownloadServiceException {

        Map<String, Object> consumeData = fetchAssetDataBeforeConsume(did, serviceIndex);
        String serviceEndpoint = (String) consumeData.get("serviceEndpoint");
        List<AssetMetadata.File> files = (List<AssetMetadata.File>) consumeData.get("files");

        String checkConsumerAddress = Keys.toChecksumAddress(getMainAccount().address);
        String agreementId = EthereumHelper.add0x(serviceAgreementId);

        // Get Access Token
        String accessToken = getAccessAccessToken(serviceEndpoint, serviceAgreementId, did);

        for (AssetMetadata.File file : files) {

            // For each url we call to download Gateway endpoint that requires
            // consumerAddress, serviceAgreementId and url as a parameters
            try {
                String destinationPath = buildDestinationPath(basePath, did, fileIndex, file);
                GatewayService.downloadToPath(serviceEndpoint, checkConsumerAddress, agreementId, did.getDid(),
                        fileIndex, accessToken, destinationPath, false, 0, 0);

            } catch (IOException e) {
                String msg = "Error consuming asset with DID " + did.getDid() + " and Service Agreement "
                        + serviceAgreementId;

                log.error(msg + ": " + e.getMessage());
                throw new DownloadServiceException(msg, e);
            }

        }

        return true;
    }

    /**
     * Constructs the final path where the file should be downloaded
     *
     * @param basePath  the path where the asset will be downloaded
     * @param did       the did
     * @param fileIndex index of the file inside the files definition in metadata
     * @param file      the asset metadata file
     * @return the destination path to download the file
     */
    private String buildDestinationPath(String basePath, DID did, int fileIndex, AssetMetadata.File file) {
        String destinationPath = basePath + File.separator + "datafile." + did.getHash() + "." + fileIndex
                + File.separator;
        if (null != file.name && !file.name.isEmpty())
            destinationPath = destinationPath + file.name;
        else
            destinationPath = destinationPath + fileIndex;

        return destinationPath;
    }

    /**
     * Downloads a single file of an Asset previously ordered through a Service
     * Agreement
     *
     * @param serviceAgreementId the service agreement id
     * @param did                the did
     * @param serviceIndex       the id of the service index in the DDO
     * @param fileIndex          of the file inside the files definition in metadata
     * @return an InputStream that represents the binary content
     * @throws DownloadServiceException DownloadServiceException
     */
    public InputStream consumeBinary(String serviceAgreementId, DID did, int serviceIndex, int fileIndex)
            throws DownloadServiceException {
        return consumeBinary(serviceAgreementId, did, serviceIndex, fileIndex, false, 0, 0);
    }

    /**
     * Downloads a single file of an Asset previously ordered through a Service
     * Agreement. It could be a request by range of bytes
     *
     * @param serviceAgreementId the service agreement id
     * @param did                the did
     * @param serviceIndex       id of the service in the DDO
     * @param fileIndex          of the file inside the files definition in metadata
     * @param isRangeRequest     indicates if is a request by range of bytes
     * @param rangeStart         the start of the bytes range
     * @param rangeEnd           the end of the bytes range
     * @return an InputStream that represents the binary content
     * @throws DownloadServiceException DownloadServiceException
     */
    public InputStream consumeBinary(String serviceAgreementId, DID did, int serviceIndex, int fileIndex,
                                     Boolean isRangeRequest, Integer rangeStart, Integer rangeEnd) throws DownloadServiceException {

        Map<String, Object> consumeData = fetchAssetDataBeforeConsume(did, serviceIndex);
        String serviceEndpoint = (String) consumeData.get("serviceEndpoint");
        String checkConsumerAddress = Keys.toChecksumAddress(getMainAccount().address);
        String agreementId = EthereumHelper.add0x(serviceAgreementId);

        // Get Access Token
        String accessToken = getAccessAccessToken(serviceEndpoint, serviceAgreementId, did);

        try {
            return GatewayService.downloadUrl(serviceEndpoint, checkConsumerAddress, agreementId, did.getDid(),
                    fileIndex, accessToken, isRangeRequest, rangeStart, rangeEnd);

        } catch (IOException e) {
            String msg = "Error consuming asset with DID " + did.getDid() + " and Service Agreement "
                    + serviceAgreementId;

            log.error(msg + ": " + e.getMessage());
            throw new DownloadServiceException(msg, e);
        }
    }

    /**
     * Executes a remote service associated with an asset and serviceAgreementId
     *
     * @param serviceAgreementId the agreement id
     * @param did         the did
     * @param serviceIndex       the index of the service
     * @param workflowDID the workflow id
     * @return an execution id
     * @throws ServiceException ServiceException
     */
    public GatewayService.ServiceExecutionResult executeComputeService(String serviceAgreementId, DID did, int serviceIndex,
                                                                       DID workflowDID) throws ServiceException {

        DDO ddo;
        try {
            ddo = resolveDID(did);
        } catch (DDOException e) {
            String msg = "There was an error resolving the DID: ";
            log.error(msg + e.getMessage());
            throw new ServiceException(msg, e);
        }
        Service service = ddo.getService(serviceIndex);
        String checkConsumerAddress = Keys.toChecksumAddress(getMainAccount().address);
        String serviceEndpoint = service.serviceEndpoint;

        // Get Access Token
        String accessToken = getExecuteAccessToken(serviceEndpoint, serviceAgreementId, workflowDID);

        ExecuteService executeService = new ExecuteService(serviceAgreementId, workflowDID.did, checkConsumerAddress, accessToken);
        GatewayService.ServiceExecutionResult result = GatewayService.initializeServiceExecution(serviceEndpoint, executeService);
        if (!result.getOk()) {
            String msg = "There was a problem initializing the execution of the service. HTTP Code: " + result.getCode();
            log.error(msg);
            throw new ServiceException(msg);
        }

        return result;
    }

    /**
     * Generates the service endpoint and signature and calls the gateway to get the logs of a compute job
     *
     * @param serviceAgreementId The agreement id for the compute to the data
     * @param executionId The id of the compute job
     * @param providerConfig The configuration of the provider.
     * @return A list of log lines.
     * @throws ServiceException Service Exception
     */
    public List<ComputeLogs> getComputeLogs(String serviceAgreementId, String executionId,
                                            ProviderConfig providerConfig) throws ServiceException {
        String serviceEndpoint = providerConfig.getAccessEndpoint()
                .replace("/access", "/compute/logs/");
        serviceEndpoint += serviceAgreementId + "/" + executionId;

        // Get Access Token
        String accessToken = getComputeAccessToken(serviceEndpoint, serviceAgreementId, executionId);

        return GatewayService.getComputeLogs(serviceEndpoint, Keys.toChecksumAddress(getMainAccount().getAddress()), accessToken);
    }

    /**
     * Generates the service endpoint and signature and calls the gateway to get the status of a compute job
     *
     * @param serviceAgreementId The agreement id for the compute to the data
     * @param executionId The id of the compute job
     * @param providerConfig The configuration of the provider.
     * @return The current status of the compute job.
     * @throws ServiceException Service Exception
     */
    public ComputeStatus getComputeStatus(String serviceAgreementId, String executionId,
                                          ProviderConfig providerConfig) throws ServiceException {
        String serviceEndpoint = providerConfig.getAccessEndpoint()
                .replace("/access", "/compute/status/");
        serviceEndpoint += serviceAgreementId + "/" + executionId;

        // Get Access Token
        String accessToken = getComputeAccessToken(serviceEndpoint, serviceAgreementId, executionId);

        return GatewayService.getComputeStatus(serviceEndpoint, Keys.toChecksumAddress(getMainAccount().getAddress()), accessToken);
    }

    /**
     * Get the Access Token for the Download service.
     *
     * It first tries to retrieve it from the cache. If not available it:
     *  - generates a grant token
     *  - makes a call to the gateway to fetch the access token
     *  - caches the token
     *
     * @param serviceEndpoint The endpoint of the service.
     * @param did The did.
     * @return String The Access Token.
     * @throws DownloadServiceException DownloadServiceException
     */
    private String getDownloadAccessToken(String serviceEndpoint, DID did) throws DownloadServiceException {
        // Check if token is cached
        String cacheKey = getCacheKey(serviceEndpoint, did.getDid());
        if (tokenCache.containsKey(cacheKey)) {
            return tokenCache.get(cacheKey);
        }

        // Generate Grant Token
        String grantToken;
        try {
            grantToken = generateDownloadGrantToken(did);
        } catch (CryptoException | IOException | CipherException e) {
            String msg = "Error generating grant token: ";
            log.error(msg + e.getMessage());
            throw new DownloadServiceException(msg, e);
        }
        log.debug("Grant Token: " + grantToken);

        // Request Access Token
        AccessTokenResult result;
        try {
            result = GatewayService.getAccessToken(serviceEndpoint, grantToken);
        } catch (ServiceException e) {
            String msg = "Error requesting access token: ";
            log.error(msg + e.getMessage());
            throw new DownloadServiceException(msg, e);
        }

        if (!result.getOk()) {
            String msg = "Error requesting the access token: " + result.getMsg();
            log.error(msg);
            throw new DownloadServiceException(msg);
        }
        log.debug("Access Token: " + result.getAccessToken());

        tokenCache.put(cacheKey, result.getAccessToken());
        return result.getAccessToken();
    }

    /**
     * Get the Access Token for the Access service.
     *
     * It first tries to retrieve it from the cache. If not available it:
     *  - generates a grant token
     *  - makes a call to the gateway to fetch the access token
     *  - caches the token
     *
     * @param serviceEndpoint The service endpoint.
     * @param serviceAgreementId The Service Agreement Id.
     * @param did The did.
     * @return String The Access Token.
     * @throws DownloadServiceException DownloadServiceException
     */
    private String getAccessAccessToken(String serviceEndpoint, String serviceAgreementId, DID did)
            throws DownloadServiceException {

        // Check if token is cached
        String cacheKey = getCacheKey(serviceEndpoint, serviceAgreementId, did.getDid());
        if (tokenCache.containsKey(cacheKey)) {
            return tokenCache.get(cacheKey);
        }

        // Generate Grant Token
        String grantToken;
        try {
            grantToken = generateAccessGrantToken(serviceAgreementId, did);
        } catch (CryptoException | IOException | CipherException e) {
            String msg = "Error generating grant token: ";
            log.error(msg + e.getMessage());
            throw new DownloadServiceException(msg, e);
        }
        log.debug("Grant Token: " + grantToken);

        // Request Access Token
        AccessTokenResult result;
        try {
            result = GatewayService.getAccessToken(serviceEndpoint, grantToken);
        } catch (ServiceException e) {
            String msg = "Error requesting access token: ";
            log.error(msg + e.getMessage());
            throw new DownloadServiceException(msg, e);
        }

        if (!result.getOk()) {
            String msg = "Error requesting the access token: " + result.getMsg();
            log.error(msg);
            throw new DownloadServiceException(msg);
        }
        log.debug("Access Token: " + result.getAccessToken());

        tokenCache.put(cacheKey, result.getAccessToken());
        return result.getAccessToken();
    }

    /**
     * Get the Access Token for the Execute service.
     *
     * It first tries to retrieve it from the cache. If not available it:
     *  - generates a grant token
     *  - makes a call to the gateway to fetch the access token
     *  - caches the token
     *
     * @param serviceEndpoint The service endpoint.
     * @param serviceAgreementId The Service Agreement Id.
     * @param workflowDID The workflow did.
     * @return String The Access Token.
     * @throws ServiceException ServiceException
     */
    private String getExecuteAccessToken(String serviceEndpoint, String serviceAgreementId, DID workflowDID)
            throws ServiceException {

        // Check if token is cached
        String cacheKey = getCacheKey(serviceEndpoint, serviceAgreementId, workflowDID.getDid());
        if (tokenCache.containsKey(cacheKey)) {
            return tokenCache.get(cacheKey);
        }

        // Generate Grant Token
        String grantToken;
        try {
            grantToken = generateExecuteGrantToken(serviceAgreementId, workflowDID);
        } catch (CryptoException | IOException | CipherException e) {
            String msg = "Error generating grant token: ";
            log.error(msg + e.getMessage());
            throw new ServiceException(msg, e);
        }
        log.debug("Grant Token: " + grantToken);

        // Request Access Token
        AccessTokenResult result;
        try {
            result = GatewayService.getAccessToken(serviceEndpoint, grantToken);
        } catch (ServiceException e) {
            String msg = "Error requesting access token: ";
            log.error(msg + e.getMessage());
            throw new ServiceException(msg, e);
        }

        if (!result.getOk()) {
            String msg = "Error requesting the access token: " + result.getMsg();
            log.error(msg);
            throw new ServiceException(msg);
        }
        log.debug("Access Token: " + result.getAccessToken());

        tokenCache.put(cacheKey, result.getAccessToken());
        return result.getAccessToken();
    }

    /**
     * Get the Access Token for the Compute service.
     *
     * It first tries to retrieve it from the cache. If not available it:
     *  - generates a grant token
     *  - makes a call to the gateway to fetch the access token
     *  - caches the token
     *
     * @param serviceEndpoint The service endpoint.
     * @param serviceAgreementId The Service Agreement Id.
     * @param executionId The execution Id.
     * @return String The Access Token.
     * @throws ServiceException ServiceException
     */
    private String getComputeAccessToken(String serviceEndpoint, String serviceAgreementId, String executionId)
            throws ServiceException {

        // Check if token is cached
        String cacheKey = getCacheKey(serviceEndpoint, serviceAgreementId, executionId);
        if (tokenCache.containsKey(cacheKey)) {
            return tokenCache.get(cacheKey);
        }

        // Generate Grant Token
        String grantToken;
        try {
            grantToken = generateComputeGrantToken(serviceAgreementId, executionId);
        } catch (CryptoException | IOException | CipherException e) {
            String msg = "Error generating grant token: ";
            log.error(msg + e.getMessage());
            throw new ServiceException(msg, e);
        }
        log.debug("Grant Token: " + grantToken);

        // Request Access Token
        AccessTokenResult result;
        try {
            result = GatewayService.getAccessToken(serviceEndpoint, grantToken);
        } catch (ServiceException e) {
            String msg = "Error requesting access token: ";
            log.error(msg + e.getMessage());
            throw new ServiceException(msg, e);
        }

        if (!result.getOk()) {
            String msg = "Error requesting the access token: " + result.getMsg();
            log.error(msg);
            throw new ServiceException(msg);
        }
        log.debug("Access Token: " + result.getAccessToken());

        tokenCache.put(cacheKey, result.getAccessToken());
        return result.getAccessToken();
    }

    /**
     * Generates a unique cache key based of the request arguments.
     *
     * This key will be used to search the token cache.
     *
     * @param args List of arguments.
     * @return String The cache key.
     */
    private String getCacheKey(String ... args) {
        return String.join("", args);
    }

    // TODO: to be implemented
    public Order getOrder(String orderId) {
        return null;
    }

    // TODO: to be implemented
    public List<AssetMetadata> searchOrders() {
        return new ArrayList<>();
    }




}
