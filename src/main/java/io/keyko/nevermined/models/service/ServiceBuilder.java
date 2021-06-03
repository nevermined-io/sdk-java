package io.keyko.nevermined.models.service;

import com.typesafe.config.Config;
import io.keyko.nevermined.core.sla.handlers.ServiceAccessAgreementHandler;
import io.keyko.nevermined.core.sla.handlers.ServiceAgreementHandler;
import io.keyko.nevermined.core.sla.handlers.ServiceComputingAgreementHandler;
import io.keyko.nevermined.exceptions.DDOException;
import io.keyko.nevermined.exceptions.InitializeConditionsException;
import io.keyko.nevermined.exceptions.ServiceException;
import io.keyko.nevermined.models.AssetRewards;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.service.types.AccessService;
import io.keyko.nevermined.models.service.types.ComputingService;

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

    private static ComputingService buildComputingService(ProviderConfig providerConfig, ComputingService.Provider computingProvider, String computingServiceTemplateId, AssetRewards assetRewards, String creator) {

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
        conditionDependency.escrowReward = Service.ConditionDependency.defaultComputeEscrowPaymentCondition();
        conditionDependency.accessSecretStore = null;
        serviceAgreementTemplate.conditionDependency = conditionDependency;
        serviceAgreementTemplate.events = Arrays.asList(executeAgreementEvent);

        // The templateId of the AccessService is the address of the escrowAccessSecretStoreTemplate contract
        ComputingService computingService = new ComputingService(providerConfig.getAccessEndpoint(),
                Service.DEFAULT_COMPUTE_INDEX,
                serviceAgreementTemplate,
                computingServiceTemplateId);

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

    private static AccessService buildAccessService(ProviderConfig providerConfig, String accessServiceTemplateId, AssetRewards assetRewards, String creator) {

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
                accessServiceTemplateId);
        accessService.attributes.main.name = "dataAssetAccessServiceAgreement";
        accessService.attributes.main.price = assetRewards.totalPrice;
        accessService.attributes.main.creator = creator;
        accessService.attributes.main.datePublished = new Date();

        return accessService;
    }

    /**
     * Gets the ConditionStatusMap Params of a DDO
     *
     * @param ddo       the ddo to parse
     * @param service   the service
     * @param config    the config object including the contract addresses
     * @return a list of Conditions
     */
    static List<Condition> getGenericConditionParams(DDO ddo, Service service, Config config, AssetRewards assetRewards) throws DDOException {
        Map<String, Object> params = new HashMap<>();
        final HashMap<String, HashMap<String, String>> contractNames = (HashMap<String, HashMap<String, String>>) config.getAnyRef("contract");
        for (String name: contractNames.keySet())    {
            String configToken = "contract." + name + ".address";
            params.put(configToken, config.getString(configToken));
        }
        params.put("parameter.did", ddo.getDID().getDid());
        params.put("parameter.assetId", ddo.getDID().getHash());
        params.put("parameter.price", assetRewards.totalPrice);
        params.put("parameter.tokenAddress", assetRewards.tokenAddress);

        ServiceAgreementHandler sla;
        List<Condition> conditions;

        if (service instanceof AccessService) {
            sla = new ServiceAccessAgreementHandler();
        } else if (service instanceof ComputingService) {
            sla = new ServiceComputingAgreementHandler();
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
