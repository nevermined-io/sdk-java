package io.keyko.nevermined.manager;

import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.helpers.EthereumHelper;
import io.keyko.common.helpers.UrlHelper;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.core.sla.functions.FulfillEscrowReward;
import io.keyko.nevermined.core.sla.functions.FulfillLockReward;
import io.keyko.nevermined.core.sla.handlers.ServiceAccessAgreementHandler;
import io.keyko.nevermined.core.sla.handlers.ServiceAgreementHandler;
import io.keyko.nevermined.core.sla.handlers.ServiceComputingAgreementHandler;
import io.keyko.nevermined.exceptions.*;
import io.keyko.nevermined.external.GatewayService;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.Order;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.asset.OrderResult;
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
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Handles several operations related with Ocean's flow
 */
public class NeverminedManager extends BaseManager {

    private static final Logger log = LogManager.getLogger(NeverminedManager.class);
    private AgreementsManager agreementsManager;
    private TemplatesManager templatesManager;

    protected NeverminedManager(KeeperService keeperService, MetadataApiService metadataApiService) {
        super(keeperService, metadataApiService);
    }

    /**
     * Given the KeeperService and MetadataApiService, returns a new instance of OceanManager
     * using them as attributes
     *
     * @param keeperService   Keeper Dto
     * @param metadataApiService Provider Dto
     * @return NeverminedManager
     */
    public static NeverminedManager getInstance(KeeperService keeperService, MetadataApiService metadataApiService) {
        return new NeverminedManager(keeperService, metadataApiService);
    }

    public NeverminedManager setAgreementManager(AgreementsManager agreementManager){
        this.agreementsManager = agreementManager;
        return this;
    }

    public NeverminedManager setTemplatesManager(TemplatesManager templatesManager){
        this.templatesManager = templatesManager;
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
     * Given a DID and a Metadata API url, register on-chain the DID.
     * It allows to resolve DDO's using DID's as input
     *
     * @param did       the did
     * @param url       metadata url
     * @param checksum  calculated hash of the metadata
     * @param providers list of providers addresses to give access
     * @return boolean success
     * @throws DIDRegisterException DIDRegisterException
     */
    public boolean registerDID(DID did, String url, String checksum, List<String> providers) throws DIDRegisterException {
        log.debug("Registering DID " + did.getHash() + " into Registry " + didRegistry.getContractAddress());


        try {

            TransactionReceipt receipt = didRegistry.registerAttribute(
                    EncodingHelper.hexStringToBytes(did.getHash()),
                    EncodingHelper.hexStringToBytes(checksum.replace("0x", "")),
                    providers,
                    url
            ).send();

            return receipt.getStatus().equals("0x1");

        } catch (Exception e) {
            throw new DIDRegisterException("Error registering DID " + did.getHash(), e);
        }
    }


    private  Map<String, Object> buildBasicAccessServiceConfiguration(ProviderConfig providerConfig, String price, String creatorAddress) {

        Map<String, Object> configuration = new HashMap<>();
        configuration.put("providerConfig", providerConfig);
        configuration.put("accessServiceTemplateId", escrowAccessSecretStoreTemplate.getContractAddress());
        configuration.put("accessSecretStoreConditionAddress", accessSecretStoreCondition.getContractAddress());
        configuration.put("price", price);
        configuration.put("creator", creatorAddress);

        return configuration;

    }


    private  Map<String, Object> buildBasicComputingServiceConfiguration(ProviderConfig providerConfig, ComputingService.Provider computingProvider, String price, String creatorAddress) {

        Map<String, Object> configuration = new HashMap<>();
        configuration.put("providerConfig", providerConfig);
        configuration.put("computingProvider", computingProvider);
        configuration.put("computingServiceTemplateId", escrowComputeExecutionTemplate.getContractAddress());
        configuration.put("execComputeConditionAddress", computeExecutionCondition.getContractAddress());
        configuration.put("price", price);
        configuration.put("creator", creatorAddress);

        return configuration;

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
        return registerAccessServiceAsset(
                metadata,
                providerConfig,
                new AuthConfig(providerConfig.getGatewayUrl(), AuthorizationService.AuthTypes.PSK_RSA));
    }

    /**
     * Creates a new DDO with an AccessService
     *
     * @param metadata       the metadata
     * @param providerConfig the service Endpoints
     * @param authConfig      auth configuration
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO registerAccessServiceAsset(AssetMetadata metadata, ProviderConfig providerConfig, AuthConfig authConfig) throws DDOException {

        try {
            Map<String, Object> configuration = buildBasicAccessServiceConfiguration(providerConfig, metadata.attributes.main.price, getMainAccount().address);
            Service accessService = ServiceBuilder
                    .getServiceBuilder(Service.ServiceTypes.ACCESS)
                    .buildService(configuration);

            return registerAsset(metadata, providerConfig, accessService, authConfig);

        } catch (ServiceException e) {
            throw new DDOException("Error registering Asset.", e);
        }

    }


    /**
     * Creates a new DDO with a ComputeService
     *
     * @param metadata       the metadata
     * @param providerConfig the service Endpoints
     * @param computingProvider the data relative to the provider
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO registerComputeService(AssetMetadata metadata, ProviderConfig providerConfig, ComputingService.Provider computingProvider) throws DDOException {

        try {

            Map<String, Object> configuration = buildBasicComputingServiceConfiguration(providerConfig, computingProvider, metadata.attributes.main.price, getMainAccount().address);
            Service computingService = ServiceBuilder
                    .getServiceBuilder(Service.ServiceTypes.COMPUTE)
                    .buildService(configuration);

            return registerAsset(metadata, providerConfig, computingService, new AuthConfig(providerConfig.getGatewayUrl()));

        } catch ( ServiceException e) {
            throw new DDOException("Error registering Asset.", e);
        }

    }

    /**
     * Creates a new DDO, registering it on-chain through DidRegistry contract and off-chain in Metadata Api
     *
     * @param metadata       the metadata
     * @param providerConfig the service Endpoints
     * @param service        the service
     * @param authConfig      auth configuration
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    private DDO registerAsset(AssetMetadata metadata, ProviderConfig providerConfig, Service service, AuthConfig authConfig) throws DDOException {

        try {

            // Definition of service endpoints
            String metadataEndpoint;
            if (providerConfig.getMetadataEndpoint() == null)
                metadataEndpoint = getMetadataApiService().getDdoEndpoint() + "/{did}";
            else
                metadataEndpoint = providerConfig.getMetadataEndpoint();

            // Initialization of services supported for this asset
            MetadataService metadataService = new MetadataService(metadata, metadataEndpoint, Service.DEFAULT_METADATA_INDEX);

            ProvenanceService provenanceService= new ProvenanceService(providerConfig.getMetadataEndpoint(), Service.DEFAULT_PROVENANCE_INDEX);

            AuthorizationService authorizationService = null;
            if (null != authConfig) {
                if (authConfig.getService().equals(AuthorizationService.AuthTypes.SECRET_STORE))
                    authorizationService = AuthorizationService.buildSecretStoreAuthService(
                            providerConfig.getSecretStoreEndpoint(), Service.DEFAULT_AUTHORIZATION_INDEX, authConfig.getThreshold());
                else if (authConfig.getService().equals(AuthorizationService.AuthTypes.PSK_ECDSA))
                    authorizationService = AuthorizationService.buildECDSAAuthService(
                            providerConfig.getGatewayUrl(), Service.DEFAULT_AUTHORIZATION_INDEX);
                else if (authConfig.getService().equals(AuthorizationService.AuthTypes.PSK_RSA))
                    authorizationService = AuthorizationService.buildRSAAuthService(
                            providerConfig.getGatewayUrl(), Service.DEFAULT_AUTHORIZATION_INDEX);
            }

            // Initializing DDO
            DDO ddo = this.buildDDO(metadataService, getMainAccount().address);

            // Adding services to DDO
            ddo.addService(service);
            ddo.addService(provenanceService);

            if (authorizationService != null)
                ddo.addService(authorizationService);

            // Generating the DDO.proof, checksums and calculating DID
            ddo= ddo.integrityBuilder(getKeeperService().getCredentials());

            // Add authentication
            ddo.addAuthentication(ddo.id);

            if (service instanceof AccessService || service instanceof ComputingService)   {
                if (authConfig.getService().equals(AuthorizationService.AuthTypes.SECRET_STORE))
                    ddo.secretStoreLocalEncryptFiles(getSecretStoreManager(), authConfig);
                else if (authConfig.getService().equals(AuthorizationService.AuthTypes.PSK_ECDSA) ||
                        authConfig.getService().equals(AuthorizationService.AuthTypes.PSK_RSA))
                    ddo.gatewayEncryptFiles(authConfig);
            }

            // Initialize conditions
            ServiceAgreementHandler sla = null;
            List<Condition> conditions;
            Map<String, Object> conditionParams = null;

            if (service instanceof AccessService) {
                sla = new ServiceAccessAgreementHandler();
                conditionParams = ServiceBuilder.getAccessConditionParams(ddo.getDid().toString(), metadata.attributes.main.price,
                        escrowReward.getContractAddress(),
                        lockRewardCondition.getContractAddress(),
                        accessSecretStoreCondition.getContractAddress());
            }
            else if (service instanceof ComputingService) {
                sla = new ServiceComputingAgreementHandler();
                conditionParams = ServiceBuilder.getComputingConditionParams(ddo.getDid().toString(), metadata.attributes.main.price,
                        escrowReward.getContractAddress(),
                        lockRewardCondition.getContractAddress(),
                        computeExecutionCondition.getContractAddress());
            }
            try {
                conditions = sla.initializeConditions(conditionParams);
            }catch (InitializeConditionsException e) {
                throw new DDOException("Error registering Asset.", e);
            }

            Service theService = ddo.getService(service.index);
            theService.attributes.serviceAgreementTemplate.conditions = conditions;

            // Substitution of the did token in the url. The ddo will be registered using the complete metadata url
            metadataEndpoint = UrlHelper.parseDDOUrl(metadataEndpoint, ddo.getDid().toString());

            // Registering DID
            registerDID(ddo.getDid(), metadataEndpoint, ddo.getDid().getHash(), providerConfig.getProviderAddresses());

            // Storing DDO
            return getMetadataApiService().createDDO(ddo);

        } catch (DDOException | DIDRegisterException | IOException | CipherException | ServiceException e) {
            throw new DDOException("Error registering Asset.", e);
        }

    }


    public boolean isConditionFulfilled(String serviceAgreementId, Condition.ConditionTypes conditionType) throws Exception {
        final int maxRetries = 5;
        final long sleepTime = 500l;
        int iteration = 0;

        while (iteration < maxRetries)  {
            AgreementStatus status = agreementsManager.getStatus(serviceAgreementId);
            BigInteger conditionStatus = status.conditions.get(0).conditions.get(conditionType.toString());
            log.debug("Condition check[" + conditionType.toString() + "] :" + conditionStatus);
            if (conditionStatus.equals(BigInteger.TWO)) // Condition is fullfilled
                return true;
            iteration++;
            Thread.sleep(sleepTime);
        }
        return false;
    }

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service Agreement between publisher and consumer
     *
     * @param did                 the did
     * @return true if the asset was purchased successfully, if not false
     * @throws OrderException OrderException
     * @throws ServiceException ServiceException
     * @throws EscrowRewardException EscrowRewardException
     */
    public OrderResult purchaseAssetDirect(DID did)
            throws OrderException, ServiceException, EscrowRewardException {
        return purchaseAssetDirect(did, -1, Service.ServiceTypes.ACCESS);
    }

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service Agreement between publisher and consumer
     *
     * @param did                 the did
     * @param serviceIndex the index of the service
     * @return true if the asset was purchased successfully, if not false
     * @throws OrderException OrderException
     * @throws ServiceException ServiceException
     * @throws EscrowRewardException EscrowRewardException
     */
    public OrderResult purchaseAssetDirect(DID did, int serviceIndex)
            throws OrderException, ServiceException, EscrowRewardException {
        return purchaseAssetDirect(did, serviceIndex, null);
    }

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service Agreement between publisher and consumer
     *
     * @param did                 the did
     * @param serviceType Service to purchase
     * @return true if the asset was purchased successfully, if not false
     * @throws OrderException OrderException
     * @throws ServiceException ServiceException
     * @throws EscrowRewardException EscrowRewardException
     */
    public OrderResult purchaseAssetDirect(DID did, Service.ServiceTypes serviceType)
            throws OrderException, ServiceException, EscrowRewardException {
        return purchaseAssetDirect(did, -1, serviceType);
    }

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service Agreement between publisher and consumer
     *
     * @param did                 the did
     * @param serviceIndex the index of the service
     * @param serviceType Service to purchase
     * @return true if the asset was purchased successfully, if not false
     * @throws OrderException OrderException
     * @throws ServiceException ServiceException
     * @throws EscrowRewardException EscrowRewardException
     */
    public OrderResult purchaseAssetDirect(DID did, int serviceIndex, Service.ServiceTypes serviceType)
            throws OrderException, ServiceException, EscrowRewardException {

        String serviceAgreementId = ServiceAgreementHandler.generateSlaId();
        OrderResult orderResult;
        DDO ddo;
        // Checking if DDO is already there and serviceDefinitionId is included
        try {
            ddo = resolveDID(did);
        } catch (DDOException  e) {
            log.error("Error resolving did[" + did.getHash() + "]: " + e.getMessage());
            throw new OrderException("Error processing Order with DID " + did.getDid(), e);
        }

        Service service;
        if (serviceIndex >= 0)
            service = ddo.getService(serviceIndex);
        else if (serviceType.toString().equals(Service.ServiceTypes.COMPUTE)) {
            service = ddo.getComputeService();
            serviceIndex = service.index;
        } else {
            service = ddo.getAccessService();
            serviceIndex = service.index;
        }

        try {
            // Step 1. We initialize the Service Agreement
            final boolean isInitialized = initializeServiceAgreementDirect(ddo, serviceIndex, serviceAgreementId);
            if (!isInitialized)  {
                throw new ServiceAgreementException(serviceAgreementId, "Service Agreement not Initialized");
            }
        } catch (ServiceAgreementException e) {
            String msg = "Error processing Order with DID " + did.getDid() + "and ServiceAgreementID " + serviceAgreementId;
            log.error(msg + ": " + e.getMessage());
            throw new OrderException(msg, e);
        }

        final String eventServiceAgreementId = EthereumHelper.add0x(serviceAgreementId);

        try {
            log.debug("Service Agreement " + serviceAgreementId + " initialized successfully");
            String price = ddo.getMetadataService().attributes.main.price;
            tokenApprove(this.tokenContract, lockRewardCondition.getContractAddress(), price);
            BigInteger balance = this.tokenContract.balanceOf(getMainAccount().address).send();
            if (balance.compareTo(new BigInteger(price)) < 0) {
                log.warn("Consumer account does not have sufficient token balance to fulfill the " +
                        "LockRewardCondition. Do `requestTokens` using the `dispenser` contract then try this again.");
                log.warn("token balance is: " + balance + " price is: " + price);
                throw new LockRewardFulfillException("LockRewardCondition.fulfill will fail due to insufficient token balance in the consumer account.");
            }
        } catch (TokenApproveException | LockRewardFulfillException e) {
            String msg = "Error approving token";
            log.error(msg + ": " + e.getMessage());
            throw new OrderException(msg, e);
        } catch (Exception e) {
            String msg = "Token Transaction error";
            log.error(msg + ": " + e.getMessage());
            throw new OrderException(msg, e);        }

        try {
            // Step 2. We fulfull the Lock Reward (we make the payment)
            this.fulfillLockReward(ddo, serviceIndex, eventServiceAgreementId);
            final boolean isFulfilled = isConditionFulfilled(serviceAgreementId, Condition.ConditionTypes.lockReward);
            orderResult = new OrderResult(serviceAgreementId, isFulfilled, false, serviceIndex);

        } catch (LockRewardFulfillException e) {
            this.fulfillEscrowReward(ddo, serviceIndex, serviceAgreementId);
            return new OrderResult(serviceAgreementId, false, true);
        } catch (Exception e) {
            this.fulfillEscrowReward(ddo, serviceIndex, serviceAgreementId);
            return new OrderResult(serviceAgreementId, false, true);
        }
        return orderResult;

    }

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service Agreement between publisher and consumer
     *
     * @param did                 the did
     * @param serviceIndex the index of the service
     * @return a Flowable instance over an OrderResult to get the result of the flow in an asynchronous fashion
     * @throws OrderException OrderException
     */
    public Flowable<OrderResult> purchaseAssetFlowable(DID did, int serviceIndex)
            throws OrderException {

        String serviceAgreementId = ServiceAgreementHandler.generateSlaId();

        DDO ddo;
        // Checking if DDO is already there and serviceDefinitionId is included
        try {

            ddo = resolveDID(did);
        } catch (DDOException  e) {
            log.error("Error resolving did[" + did.getHash() + "]: " + e.getMessage());
            throw new OrderException("Error processing Order with DID " + did.getDid(), e);
        }

        try {

            Service service = ddo.getService(serviceIndex);

            return this.initializeServiceAgreementFlowable(ddo, serviceIndex, serviceAgreementId)
                    .firstOrError()
                    .toFlowable()
                    .switchMap(eventServiceAgreementId -> {
                        if (eventServiceAgreementId.isEmpty())
                            return Flowable.empty();
                        else {
                            log.debug("Received AgreementCreated Event with Id: " + eventServiceAgreementId);
                            String price = ddo.getMetadataService().attributes.main.price;
                            tokenApprove(this.tokenContract, lockRewardCondition.getContractAddress(), price);
                            BigInteger balance = this.tokenContract.balanceOf(getMainAccount().address).send();
                            if (balance.compareTo(new BigInteger(price)) < 0) {
                                log.warn("Consumer account does not have sufficient token balance to fulfill the " +
                                        "LockRewardCondition. Do `requestTokens` using the `dispenser` contract then try this again.");
                                log.info("token balance is: " + balance + " price is: " + price);
                                throw new Exception("LockRewardCondition.fulfill will fail due to insufficient token balance in the consumer account.");
                            }
                            this.fulfillLockReward(ddo, serviceIndex, eventServiceAgreementId);
                            Flowable<String> conditionFulilledEvent = null;

                            if (service.type.equals(Service.ServiceTypes.ACCESS.toString()))
                                conditionFulilledEvent = ServiceAgreementHandler.listenForFulfilledEvent(accessSecretStoreCondition, serviceAgreementId);
                            else if  (service.type.equals(Service.ServiceTypes.COMPUTE.toString()))
                                conditionFulilledEvent = ServiceAgreementHandler.listenForFulfilledEvent(computeExecutionCondition, serviceAgreementId);
                            else
                                throw new ServiceAgreementException(serviceAgreementId, "Service type not supported");

                            return conditionFulilledEvent;
                        }
                    })
                    .map(event -> new OrderResult(serviceAgreementId, true, false, serviceIndex))
                    // TODO timout of the condition
                    .timeout(120, TimeUnit.SECONDS)
                    .onErrorReturn(throwable -> {

                        if (throwable instanceof TimeoutException) {
                            // If we get a timeout listening for a Condition Fulfilled Event,
                            // we must perform a refund executing escrowReward.fulfill
                            this.fulfillEscrowReward(ddo, serviceIndex, serviceAgreementId);
                            return new OrderResult(serviceAgreementId, false, true);
                        }

                        String msg = "There was a problem executing the Service Agreement " + serviceAgreementId;
                        throw new ServiceAgreementException(serviceAgreementId, msg, throwable);
                    });

        } catch ( ServiceException | ServiceAgreementException e) {
            String msg = "Error processing Order with DID " + did.getDid() + "and ServiceAgreementID " + serviceAgreementId;
            log.error(msg + ": " + e.getMessage());
            throw new OrderException(msg, e);
        }

    }


    public List<byte[]> generateServiceConditionsId(String serviceAgreementId, String consumerAddress, DDO ddo, int serviceIndex) throws ServiceAgreementException, ServiceException {

        Service service = ddo.getService(serviceIndex);

        Map<String, String> conditionsAddresses = new HashMap<>();
        conditionsAddresses.put("escrowRewardAddress", escrowReward.getContractAddress());
        conditionsAddresses.put("lockRewardConditionAddress", lockRewardCondition.getContractAddress());

        if (service.type.equals(Service.ServiceTypes.ACCESS.toString())) {
            conditionsAddresses.put("accessSecretStoreConditionAddress", accessSecretStoreCondition.getContractAddress());
            service = (AccessService)service;
        }
        else if  (service.type.equals(Service.ServiceTypes.COMPUTE.toString()))
        {
            conditionsAddresses.put("computeExecutionConditionAddress", computeExecutionCondition.getContractAddress());
            service = (ComputingService)service;
        }
        else
            throw new ServiceAgreementException(serviceAgreementId, "Service type not supported");

        List<byte[]> conditionsId;

        try {
            conditionsId= service.generateByteConditionIds(serviceAgreementId, conditionsAddresses, ddo.proof.creator, Keys.toChecksumAddress(consumerAddress));
        } catch (Exception e) {
            throw new ServiceAgreementException(serviceAgreementId, "Exception generating conditions id", e);
        }

        return conditionsId;

    }

    /**
     * Initialize a new ServiceExecutionAgreement between a publisher and a consumer
     *
     * @param ddo                 the ddi
     * @param serviceIndex      the service index
     * @param serviceAgreementId  the service agreement id
     * @return true if the agreement was initialized correctly, if not false
     * @throws ServiceException          ServiceException
     * @throws ServiceAgreementException ServiceAgreementException
     */
    private boolean initializeServiceAgreement(DDO ddo, int serviceIndex, String serviceAgreementId)
            throws  ServiceException, ServiceAgreementException {

        Service service = ddo.getService(serviceIndex);

        Boolean isTemplateApproved;
        try {
            isTemplateApproved = templatesManager.isTemplateApproved(service.templateId);
        } catch (EthereumException e) {
            String msg = "Error creating Service Agreement: " + serviceAgreementId + ". Error verifying template " + service.templateId;
            log.error(msg + ": " + e.getMessage());
            throw new ServiceAgreementException(serviceAgreementId, msg, e);
        }

        if (!isTemplateApproved)
            throw new ServiceAgreementException(serviceAgreementId, "The template " + service.templateId + " is not approved");

        Boolean result = false;

        try {
            List<byte[]> conditionsId = generateServiceConditionsId(
                    serviceAgreementId,
                    Keys.toChecksumAddress(getMainAccount().getAddress()),
                    ddo,
                    serviceIndex);

            if (service.type.equals(Service.ServiceTypes.ACCESS.toString()))
                result = this.agreementsManager.createAccessAgreement(serviceAgreementId,
                        ddo,
                        conditionsId,
                        Keys.toChecksumAddress(getMainAccount().getAddress()),
                        service
                );
            else if  (service.type.equals(Service.ServiceTypes.COMPUTE.toString()))
                result = this.agreementsManager.createComputeAgreement(serviceAgreementId,
                        ddo,
                        conditionsId,
                        Keys.toChecksumAddress(getMainAccount().getAddress()),
                        service
                );
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
     * @param ddo                 the ddi
     * @param serviceIndex      the service index
     * @param serviceAgreementId  the service agreement id
     * @return true if the agreement was initialized correctly, if not false
     * @throws ServiceException          ServiceException
     * @throws ServiceAgreementException ServiceAgreementException
     */
    protected boolean initializeServiceAgreementDirect(DDO ddo, int serviceIndex, String serviceAgreementId)
            throws  ServiceException, ServiceAgreementException {

        boolean initializationStatus = initializeServiceAgreement(ddo, serviceIndex, serviceAgreementId);
        if (!initializationStatus)
            return checkAgreementStatus(serviceAgreementId);
        return false;
    }

    /**
     * Initialize a new ServiceExecutionAgreement between a publisher and a consumer return a
     * flowable to listen contract initialization events
     *
     * @param ddo                 the ddi
     * @param serviceIndex      the service index
     * @param serviceAgreementId  the service agreement id
     * @return a Flowable over an AgreementInitializedEventResponse
     * @throws ServiceException          ServiceException
     * @throws ServiceAgreementException ServiceAgreementException
     */
    protected Flowable<String> initializeServiceAgreementFlowable(DDO ddo, int serviceIndex, String serviceAgreementId)
            throws  ServiceException, ServiceAgreementException {

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
            executeAgreementFlowable = ServiceAgreementHandler.listenExecuteAgreement(escrowAccessSecretStoreTemplate, serviceAgreementId);
        else if  (service.type.equals(Service.ServiceTypes.COMPUTE.toString()))
            executeAgreementFlowable = ServiceAgreementHandler.listenExecuteAgreement(escrowComputeExecutionTemplate, serviceAgreementId);
        else
            throw new ServiceAgreementException(serviceAgreementId, "Service type not supported");

        return executeAgreementFlowable;
    }

    private boolean checkAgreementStatus(String serviceAgreementId) throws ServiceAgreementException{

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
        } catch (Exception e){
            throw new ServiceAgreementException(serviceAgreementId, "There was a problem checking the status", e);
        }

        if (!result)
            throw new ServiceAgreementException(serviceAgreementId, "The create Agreement Transaction has failed");

        return false;
    }

    /**
     * Executes the fulfill of the LockRewardCondition
     *
     * @param ddo                 the ddo
     * @param serviceIndex the index of the service
     * @param serviceAgreementId  service agreement id
     * @return a flag that indicates if the function was executed correctly
     * @throws ServiceException           ServiceException
     * @throws LockRewardFulfillException LockRewardFulfillException
     */
    private boolean fulfillLockReward(DDO ddo, int serviceIndex, String serviceAgreementId) throws ServiceException, LockRewardFulfillException {

        Service service = ddo.getService(serviceIndex);
        String price = service.attributes.main.price;

        return FulfillLockReward.executeFulfill(lockRewardCondition, serviceAgreementId, this.escrowReward.getContractAddress(), price);
    }

    /**
     * Executes the fulfill of the EscrowReward
     *
     * @param ddo                 the ddo
     * @param serviceIndex the index of the service
     * @param serviceAgreementId  service agreement id
     * @return a flag that indicates if the function was executed correctly
     * @throws ServiceException      ServiceException
     * @throws EscrowRewardException EscrowRewardException
     */
    private boolean fulfillEscrowReward(DDO ddo, int serviceIndex, String serviceAgreementId) throws ServiceException, EscrowRewardException {

        Service service = ddo.getService(serviceIndex);
        String price = service.attributes.main.price;

        String lockRewardConditionId = "";
        String releaseConditionId = "";

        try {

            lockRewardConditionId = service.generateLockRewardId(serviceAgreementId, escrowReward.getContractAddress(), lockRewardCondition.getContractAddress());
            String conditionAddress;
            String conditionName;

            if (service.type.equals(Service.ServiceTypes.ACCESS.toString())) {
                conditionAddress =  accessSecretStoreCondition.getContractAddress();
                conditionName = "accessSecretStore";
            }
            else if  (service.type.equals(Service.ServiceTypes.COMPUTE.toString())) {
                conditionAddress =  computeExecutionCondition.getContractAddress();
                conditionName = "computeExecution";
            }
            else
                throw new ServiceException("Service type not supported");

            releaseConditionId = service.generateReleaseConditionId(serviceAgreementId, getMainAccount().getAddress(), conditionAddress, conditionName);

        } catch (UnsupportedEncodingException e) {
            throw new EscrowRewardException("Error generating the condition Ids ", e);
        }

        return FulfillEscrowReward.executeFulfill(escrowReward,
                serviceAgreementId,
                this.lockRewardCondition.getContractAddress(),
                price,
                this.getMainAccount().address,
                lockRewardConditionId,
                releaseConditionId);
    }

    /**
     * Gets the data needed to download an asset
     *
     * @param did                 the did
     * @param serviceIndex          the id of the service in the DDO
     * @return a Map with the data needed to consume the asset
     * @throws ConsumeServiceException ConsumeServiceException
     */
    private Map<String, Object> fetchAssetDataBeforeConsume(DID did, int serviceIndex) throws ConsumeServiceException {

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
            throw new ConsumeServiceException(msg, e);
        }

        return data;
    }



    /**
     * Downloads an Asset previously ordered through a Service Agreement
     *
     * @param serviceAgreementId  the service agreement id
     * @param did                 the did
     * @param serviceIndex               the service index in the DDO
     * @param basePath            the path where the asset will be downloaded
     * @return a flag that indicates if the consume operation was executed correctly
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public boolean access(String serviceAgreementId, DID did, int serviceIndex, String basePath) throws ConsumeServiceException {
        return access(serviceAgreementId, did, serviceIndex, 0, basePath);
    }


    /**
     * Downloads an Asset previously ordered through a Service Agreement
     *
     * @param serviceAgreementId  the service agreement id
     * @param did                 the did
     * @param serviceIndex        id of the service in the DDO
     * @param fileIndex               of the file inside the files definition in metadata
     * @param basePath            the path where the asset will be downloaded
     * @return a flag that indicates if the consume operation was executed correctly
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public boolean access(String serviceAgreementId, DID did, int serviceIndex, int fileIndex, String basePath) throws ConsumeServiceException {


        Map<String, Object> consumeData = fetchAssetDataBeforeConsume(did, serviceIndex);
        String serviceEndpoint = (String) consumeData.get("serviceEndpoint");
        List<AssetMetadata.File> files = (List<AssetMetadata.File>) consumeData.get("files");

        String checkConsumerAddress = Keys.toChecksumAddress(getMainAccount().address);
        String agreementId = EthereumHelper.add0x(serviceAgreementId);
        String signature;

        try {
            signature = generateSignature(agreementId);
        } catch (IOException | CipherException e) {
            final String msg = "Unable to generate service agreement signature";
            log.error(msg + ": " + e.getMessage());
            throw new ConsumeServiceException(msg, e);
        }

        for (AssetMetadata.File file : files) {

            // For each url we call to consume Gateway endpoint that requires consumerAddress, serviceAgreementId and url as a parameters
            try {
                String destinationPath = basePath + File.separator + did.getHash() + File.separator;
                if (null != file.name && !file.name.isEmpty())
                    destinationPath = destinationPath + file.name;
                else
                    destinationPath = destinationPath + fileIndex;

                GatewayService.downloadToPath(serviceEndpoint, checkConsumerAddress, agreementId, did.getDid(),
                        fileIndex, signature, destinationPath, false, 0, 0);

            } catch (IOException e) {
                String msg = "Error consuming asset with DID " + did.getDid() + " and Service Agreement " + serviceAgreementId;

                log.error(msg + ": " + e.getMessage());
                throw new ConsumeServiceException(msg, e);
            }

        }

        return true;
    }

    public String generateSignature(String message) throws IOException, CipherException {
        return EncodingHelper.signatureToString(
            EthereumHelper.signMessage(message, getKeeperService().getCredentials()));
    }

    /**
     * Downloads a single file of an Asset previously ordered through a Service Agreement
     *
     * @param serviceAgreementId  the service agreement id
     * @param did                 the did
     * @param serviceIndex        the id of the service index in the DDO
     * @param fileIndex               of the file inside the files definition in metadata
     * @return an InputStream that represents the binary content
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public InputStream consumeBinary(String serviceAgreementId, DID did, int serviceIndex, int fileIndex) throws ConsumeServiceException {
        return consumeBinary(serviceAgreementId, did, serviceIndex, fileIndex, false, 0, 0);
    }

    /**
     * Downloads a single file of an Asset previously ordered through a Service Agreement. It could be a request by range of bytes
     *
     * @param serviceAgreementId  the service agreement id
     * @param did                 the did
     * @param serviceIndex        id of the service in the DDO
     * @param fileIndex               of the file inside the files definition in metadata
     * @param isRangeRequest      indicates if is a request by range of bytes
     * @param rangeStart          the start of the bytes range
     * @param rangeEnd            the end of the bytes range
     * @return an InputStream that represents the binary content
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public InputStream consumeBinary(String serviceAgreementId, DID did, int serviceIndex, int fileIndex,
                                     Boolean isRangeRequest, Integer rangeStart, Integer rangeEnd) throws ConsumeServiceException {


        Map<String, Object> consumeData = fetchAssetDataBeforeConsume(did, serviceIndex);
        String serviceEndpoint = (String) consumeData.get("serviceEndpoint");
        List<AssetMetadata.File> files = (List<AssetMetadata.File>) consumeData.get("files");

        String checkConsumerAddress = Keys.toChecksumAddress(getMainAccount().address);
        String agreementId = EthereumHelper.add0x(serviceAgreementId);

        //  getConsumeData returns a list with only one file in case of consuming by index

        try {

            String signature = generateSignature(agreementId);
            log.info("Signature: " + signature);
            return GatewayService.downloadUrl(serviceEndpoint, checkConsumerAddress, agreementId,
                    did.getDid(), fileIndex, signature, isRangeRequest, rangeStart, rangeEnd);

        } catch (IOException | CipherException e) {
            String msg = "Error consuming asset with DID " + did.getDid() + " and Service Agreement " + serviceAgreementId;

            log.error(msg + ": " + e.getMessage());
            throw new ConsumeServiceException(msg, e);
        }

    }

    /**
     * Executes a remote service associated with an asset and serviceAgreementId
     * @param agreementId the agreement id
     * @param did the did
     * @param index the index of the service
     * @param workflowDID the workflow id
     * @return an execution id
     * @throws ServiceException ServiceException
     */
    public GatewayService.ServiceExecutionResult executeComputeService(String agreementId, DID did, int index, DID workflowDID) throws ServiceException {

        DDO ddo;

        try {
            ddo = resolveDID(did);

            Service service = ddo.getService(index);
            String checkConsumerAddress = Keys.toChecksumAddress(getMainAccount().address);

            String signature;
            try {
                signature = generateSignature(agreementId);
            } catch (IOException | CipherException e) {
                final String msg = "Unable to generate service agreement signature";
                log.error(msg + ": " + e.getMessage());
                throw new ServiceException(msg, e);
            }
//            String hash =  Hash.sha3(EthereumHelper.add0x(agreementId));
//            String signature = EthereumHelper.ethSignMessage(this.getKeeperService().getWeb3(), hash, getMainAccount().address, getMainAccount().password);

            ExecuteService executeService = new ExecuteService(agreementId, workflowDID.did, checkConsumerAddress, signature);
            GatewayService.ServiceExecutionResult result = GatewayService.initializeServiceExecution(service.serviceEndpoint, executeService);
            if (!result.getOk())
                throw new ServiceException("There was a problem initializing the execution of the service. HTTP Code: " + result.getCode());

            return result;

        } catch (DDOException e) {
            throw new ServiceException("There was an error resolving the DID ", e);
        }

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
