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

import java.math.BigInteger;
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
            String price = (String)config.get("price");
            String creator = (String)config.get("creator");

            return buildComputingService(providerConfig, computingProvider, computingServiceTemplateId, assetRewards, creator);
        };

    }

    private static ComputingService buildComputingService(ProviderConfig providerConfig, ComputingService.Provider computingProvider, String computingServiceTemplateId, AssetRewards assetRewards, String creator) throws DDOException {

        // Definition of a DEFAULT ServiceAgreement Contract
        ComputingService.ServiceAgreementTemplate serviceAgreementTemplate = new ComputingService.ServiceAgreementTemplate();
        serviceAgreementTemplate.contractName = "EscrowComputeExecutionTemplate";
        serviceAgreementTemplate.fulfillmentOrder = Arrays.asList(
                "lockReward.fulfill",
                "execCompute.fulfill",
                "escrowReward.fulfill");

        // AgreementCreated Event
        Condition.Event executeAgreementEvent = new Condition.Event();
        executeAgreementEvent.name = "AgreementCreated";
        executeAgreementEvent.actorType = "consumer";
        // Handler
        Condition.Handler handler = new Condition.Handler();
        handler.moduleName = "EscrowComputeExecutionTemplate";
        handler.functionName = "fulfillLockRewardCondition";
        handler.version = "0.1";
        executeAgreementEvent.handler = handler;

        Service.ConditionDependency conditionDependency = new Service.ConditionDependency();
        conditionDependency.escrowReward = Service.ConditionDependency.defaultComputeEscrowReward();
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

        // Initializing conditions and adding to Computing service
        /*
        ServiceAgreementHandler sla = new ServiceComputingAgreementHandler();
        try {
            computingService.attributes.main.serviceAgreementTemplate.conditions = sla.initializeConditions(
                    getComputingConditionParams(did, price, escrowRewardAddress, lockRewardConditionAddress, execComputeConditionAddress));
        }catch (InitializeConditionsException  e) {
            throw new DDOException("Error registering Asset.", e);
        }
         */
        return computingService;
    }


    private static ServiceBuilder accessServiceBuilder(AssetRewards assetRewards) {

        return config -> {

            ProviderConfig providerConfig = (ProviderConfig) config.get("providerConfig");
            String accessServiceTemplateId = (String) config.get("accessServiceTemplateId");
            String price = (String)config.get("price");
            String creator = (String)config.get("creator");
            return buildAccessService(providerConfig, accessServiceTemplateId, assetRewards, creator);
        };

    }

    private static AccessService buildAccessService(ProviderConfig providerConfig, String accessServiceTemplateId, AssetRewards assetRewards, String creator) {

        // Definition of a DEFAULT ServiceAgreement Contract
        AccessService.ServiceAgreementTemplate serviceAgreementTemplate = new AccessService.ServiceAgreementTemplate();
        serviceAgreementTemplate.contractName = "EscrowAccessSecretStoreTemplate";

        // AgreementCreated Event
        Condition.Event executeAgreementEvent = new Condition.Event();
        executeAgreementEvent.name = "AgreementCreated";
        executeAgreementEvent.actorType = "consumer";
        // Handler
        Condition.Handler handler = new Condition.Handler();
        handler.moduleName = "escrowAccessSecretStoreTemplate";
        handler.functionName = "fulfillLockRewardCondition";
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

        /*
        // Initializing conditions and adding to Access service
        ServiceAgreementHandler sla = new ServiceAccessAgreementHandler();
        try {
            accessService.attributes.main.serviceAgreementTemplate.conditions = sla.initializeConditions(
                    getAccessConditionParams(did, price, escrowRewardAddress, lockRewardConditionAddress, accessSecretStoreConditionAddress));
        }catch (InitializeConditionsException  e) {
            throw new DDOException("Error registering Asset.", e);
        }
         */
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
        params.put("parameter.did", ddo.getDid().getDid());
        params.put("parameter.assetId", ddo.getDid().getHash());
        params.put("parameter.price", assetRewards.totalPrice);

//        Condition escrowRewardCondition = null;
//        BigInteger totalPrice = BigInteger.ZERO;

        ServiceAgreementHandler sla = null;
        List<Condition> conditions;

        if (service instanceof AccessService) {
            sla = new ServiceAccessAgreementHandler();
//            escrowRewardCondition = ddo.getAccessService().getConditionbyName(Condition.ConditionTypes.escrowReward.name());
        } else if (service instanceof ComputingService) {
            sla = new ServiceComputingAgreementHandler();
//            escrowRewardCondition = ddo.getComputeService().getConditionbyName(Condition.ConditionTypes.escrowReward.name());
        }   else    {
            throw new DDOException("Unrecognized service");
        }

//        final List<String> amounts = (List<String>) escrowRewardCondition.getParameterByName("_amounts").value;
//        amounts.forEach(_amount -> { totalPrice.add(new BigInteger(_amount));});


        try {
            conditions = sla.initializeConditions(params, assetRewards);

        } catch (InitializeConditionsException e) {
            throw new DDOException("Unable the initialize Conditions", e);
        }
        return conditions;
    }

}
