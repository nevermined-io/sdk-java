package io.keyko.nevermined.models.service;

import com.typesafe.config.Config;
import io.keyko.nevermined.core.sla.handlers.*;
import io.keyko.nevermined.exceptions.DDOException;
import io.keyko.nevermined.exceptions.InitializeConditionsException;
import io.keyko.nevermined.exceptions.ServiceException;
import io.keyko.nevermined.models.AssetRewards;
import io.keyko.nevermined.models.service.types.*;

import java.util.*;

public interface ServiceBuilder {

    Service buildService( Map<String, Object> serviceConfiguration) throws DDOException;

    static ServiceBuilder getServiceBuilder(Service.ServiceTypes serviceType, AssetRewards assetRewards) throws ServiceException {

        switch (serviceType) {
            case ACCESS: return accessServiceBuilder(assetRewards);
            case COMPUTE: return computingServiceBuilder(assetRewards);
            default: throw new ServiceException("Invalid Service definition");

        }

    }

    private static ServiceBuilder computingServiceBuilder(AssetRewards assetRewards) {

        return config -> {

            ProviderConfig providerConfig = (ProviderConfig) config.get("providerConfig");
            ComputingService.Provider computingProvider = (ComputingService.Provider) config.get("computingProvider");
            String computingServiceTemplateId = (String) config.get("computingServiceTemplateId");
            String creator = (String)config.get("creator");

            return buildComputingService(providerConfig, computingProvider, computingServiceTemplateId, assetRewards, creator);
        };

    }

    static ComputingService buildComputingService(ProviderConfig providerConfig, ComputingService.Provider computingProvider, String templateId, AssetRewards assetRewards, String creator) {

        // Definition of a DEFAULT ServiceAgreement Contract
        ComputingService.ServiceAgreementTemplate serviceAgreementTemplate = new ComputingService.ServiceAgreementTemplate();
        serviceAgreementTemplate.contractName = "EscrowComputeExecutionTemplate";
        serviceAgreementTemplate.fulfillmentOrder = Arrays.asList(
                "lockPayment.fulfill",
                "execCompute.fulfill",
                "escrowPayment.fulfill");

        // AgreementCreated Event
        Condition.Event executeAgreementEvent = new Condition.Event();
        executeAgreementEvent.name = "AgreementCreated";
        executeAgreementEvent.actorType = "consumer";
        // Handler
        Condition.Handler handler = new Condition.Handler();
        handler.moduleName = "EscrowComputeExecutionTemplate";
        handler.functionName = "fulfillLockPaymentCondition";
        handler.version = "0.1";
        executeAgreementEvent.handler = handler;

        Service.ConditionDependency conditionDependency = new Service.ConditionDependency();
        conditionDependency.escrowPayment = Service.ConditionDependency.defaultComputeEscrowPaymentCondition();
        conditionDependency.access = null;
        serviceAgreementTemplate.conditionDependency = conditionDependency;
        serviceAgreementTemplate.events = Arrays.asList(executeAgreementEvent);

        // The templateId of the AccessService is the address of the escrowAccessSecretStoreTemplate contract
        ComputingService computingService = new ComputingService(providerConfig.getAccessEndpoint(),
                Service.DEFAULT_COMPUTE_INDEX,
                serviceAgreementTemplate,
                templateId);

        computingService.attributes.main.provider = computingProvider;

        computingService.attributes.main.name = "dataAssetComputeServiceAgreement";
        computingService.attributes.main.price = assetRewards.totalPrice;
        computingService.attributes.main.creator = creator;
        computingService.attributes.main.datePublished = new Date();

        return computingService;
    }


    private static ServiceBuilder accessServiceBuilder(AssetRewards assetRewards) {

        return config -> {

            ProviderConfig providerConfig = (ProviderConfig) config.get("providerConfig");
            String accessServiceTemplateId = (String) config.get("accessServiceTemplateId");
            String creator = (String)config.get("creator");
            return buildAccessService(providerConfig, accessServiceTemplateId, assetRewards, creator);
        };

    }

    static AccessService buildAccessService(ProviderConfig providerConfig, String templateId, AssetRewards assetRewards, String creator) {

        // Definition of a DEFAULT ServiceAgreement Contract
        AccessService.ServiceAgreementTemplate serviceAgreementTemplate = new AccessService.ServiceAgreementTemplate();
        serviceAgreementTemplate.contractName = "AccessTemplate";

        // AgreementCreated Event
        Condition.Event executeAgreementEvent = new Condition.Event();
        executeAgreementEvent.name = "AgreementCreated";
        executeAgreementEvent.actorType = "consumer";
        // Handler
        Condition.Handler handler = new Condition.Handler();
        handler.moduleName = "escrowAccessSecretStoreTemplate";
        handler.functionName = "fulfillLockPaymentCondition";
        handler.version = "0.1";
        executeAgreementEvent.handler = handler;

        serviceAgreementTemplate.events = Arrays.asList(executeAgreementEvent);

        // The templateId of the AccessService is the address of the escrowAccessSecretStoreTemplate contract
        AccessService accessService = new AccessService(providerConfig.getAccessEndpoint(),
                Service.DEFAULT_ACCESS_INDEX,
                serviceAgreementTemplate,
                templateId);
        accessService.attributes.main.name = "dataAssetAccessServiceAgreement";
        accessService.attributes.main.price = assetRewards.totalPrice;
        accessService.attributes.main.creator = creator;
        accessService.attributes.main.datePublished = new Date();

        return accessService;
    }


    static NFTAccessService buildNFTAccessService(ProviderConfig providerConfig, String templateId, AssetRewards assetRewards, String creator) {

        // Definition of a DEFAULT ServiceAgreement Contract
        NFTAccessService.ServiceAgreementTemplate serviceAgreementTemplate = new NFTAccessService.ServiceAgreementTemplate();
        serviceAgreementTemplate.contractName = "NFTAccessTemplate";
        serviceAgreementTemplate.fulfillmentOrder = Arrays.asList(
                "nftHolder.fulfill",
                "nftAccess.fulfill");

        // AgreementCreated Event
        Condition.Event executeAgreementEvent = new Condition.Event();
        executeAgreementEvent.name = "AgreementCreated";
        executeAgreementEvent.actorType = "consumer";
        // Handler
        Condition.Handler handler = new Condition.Handler();
        handler.moduleName = "nftAccessTemplate";
        handler.functionName = "fulfillNFTHolderCondition";
        handler.version = "0.1";
        executeAgreementEvent.handler = handler;

        serviceAgreementTemplate.events = Arrays.asList(executeAgreementEvent);

        // The templateId of the service is the address of the Template contract
        NFTAccessService _service = new NFTAccessService(providerConfig.getAccessEndpoint(),
                Service.DEFAULT_NFT_ACCESS_INDEX,
                serviceAgreementTemplate,
                templateId);
        _service.attributes.main.name = "nftAccessAgreement";
        _service.attributes.main.price = assetRewards.totalPrice;
        _service.attributes.main.creator = creator;
        _service.attributes.main.datePublished = new Date();

        return _service;
    }

    static NFTSalesService buildNFTSalesService(ProviderConfig providerConfig, String templateId, AssetRewards assetRewards, String creator) {

        // Definition of a DEFAULT ServiceAgreement Contract
        NFTSalesService.ServiceAgreementTemplate serviceAgreementTemplate = new NFTSalesService.ServiceAgreementTemplate();
        serviceAgreementTemplate.contractName = "NFTSalesService";
        serviceAgreementTemplate.fulfillmentOrder = Arrays.asList(
                "lockPayment.fulfill",
                "transferNFT.fulfill",
                "escrowPayment.fulfill");

        // AgreementCreated Event
        Condition.Event executeAgreementEvent = new Condition.Event();
        executeAgreementEvent.name = "AgreementCreated";
        executeAgreementEvent.actorType = "consumer";
        // Handler
        Condition.Handler handler = new Condition.Handler();
        handler.moduleName = "nftSalesTemplate";
        handler.functionName = "fulfillLockPaymentCondition";
        handler.version = "0.1";
        executeAgreementEvent.handler = handler;

        serviceAgreementTemplate.events = Arrays.asList(executeAgreementEvent);

        // The templateId of the service is the address of the Template contract
        NFTSalesService _service = new NFTSalesService(providerConfig.getAccessEndpoint(),
                Service.DEFAULT_NFT_SALES_INDEX,
                serviceAgreementTemplate,
                templateId);
        _service.attributes.main.name = "nftSalesAgreement";
        _service.attributes.main.price = assetRewards.totalPrice;
        _service.attributes.main.creator = creator;
        _service.attributes.main.datePublished = new Date();

        return _service;
    }

    /**
     * Gets the ConditionStatusMap Params of a DDO
     *
     * @param service   the service
     * @param config    the config object including the contract addresses
     * @param assetRewards the asset rewards configuration
     * @return a list of Conditions
     */
    static List<Condition> getGenericConditionParams(Service service, Config config, AssetRewards assetRewards) throws DDOException {
        return getGenericConditionParams(service, config, assetRewards, new HashMap<>());
    }

    /**
     * Gets the ConditionStatusMap Params of a DDO
     *
     * @param service   the service
     * @param config    the config object including the contract addresses
     * @param assetRewards the asset rewards configuration
     * @param additionalOptions the config object including the contract addresses
     * @return a list of Conditions
     */
    static List<Condition> getGenericConditionParams(Service service, Config config, AssetRewards assetRewards, Map<String, String> additionalOptions) throws DDOException {
        Map<String, Object> params = new HashMap<>();
        additionalOptions.entrySet().forEach(
                e -> params.put(e.getKey(), e.getValue())
        );

        final HashMap<String, HashMap<String, String>> contractNames = (HashMap<String, HashMap<String, String>>) config.getAnyRef("contract");
        for (String name: contractNames.keySet())    {
            String configToken = "contract." + name + ".address";
            params.put(configToken, config.getString(configToken));
        }

        params.put("parameter.price", assetRewards.totalPrice);
        params.put("parameter.tokenAddress", assetRewards.tokenAddress);

        ServiceAgreementHandler sla;
        List<Condition> conditions;

        if (service instanceof AccessService) {
            sla = new ServiceAccessAgreementHandler();
        } else if (service instanceof ComputingService) {
            sla = new ServiceComputingAgreementHandler();
        } else if (service instanceof DIDSalesService) {
            sla = new ServiceDIDSalesAgreementHandler();
        } else if (service instanceof NFTSalesService) {
            sla = new ServiceNFTSalesAgreementHandler();
        } else if (service instanceof NFTAccessService) {
            sla = new ServiceNFTAccessAgreementHandler();
        }   else    {
            throw new DDOException("Unrecognized service");
        }

        try {
            conditions = sla.initializeConditions(params, assetRewards);

        } catch (InitializeConditionsException e) {
            throw new DDOException("Unable the initialize Conditions", e);
        }
        return conditions;
    }

}
