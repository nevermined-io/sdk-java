package com.oceanprotocol.squid.models.service;

import com.oceanprotocol.squid.exceptions.DDOException;
import com.oceanprotocol.squid.exceptions.ServiceException;
import com.oceanprotocol.squid.models.service.types.AccessService;
import com.oceanprotocol.squid.models.service.types.ComputingService;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public interface ServiceBuilder {

    Service buildService( Map<String, Object> serviceConfiguration) throws DDOException;

    static ServiceBuilder getServiceBuilder(Service.ServiceTypes serviceType) throws ServiceException {

        switch (serviceType) {
            case access: return accessServiceBuilder();
            case compute: return computingServiceBuilder();
            default: throw new ServiceException("Invalid Service definition");

        }

    }

    private static ServiceBuilder computingServiceBuilder() {

        return config -> {

            ProviderConfig providerConfig = (ProviderConfig) config.get("providerConfig");
            ComputingService.Provider computingProvider = (ComputingService.Provider) config.get("computingProvider");
            String computingServiceTemplateId = (String) config.get("computingServiceTemplateId");
            String price = (String)config.get("price");
            String creator = (String)config.get("creator");
            return buildComputingService(providerConfig, computingProvider, computingServiceTemplateId, price, creator);
        };

    }

    private static ComputingService buildComputingService(ProviderConfig providerConfig, ComputingService.Provider computingProvider, String computingServiceTemplateId, String price, String creator) throws DDOException {

        // Definition of a DEFAULT ServiceAgreement Contract
        ComputingService.ServiceAgreementTemplate serviceAgreementTemplate = new ComputingService.ServiceAgreementTemplate();
        serviceAgreementTemplate.contractName = "EscrowExecComputeTemplate";

        // AgreementCreated Event
        Condition.Event executeAgreementEvent = new Condition.Event();
        executeAgreementEvent.name = "AgreementCreated";
        executeAgreementEvent.actorType = "consumer";
        // Handler
        Condition.Handler handler = new Condition.Handler();
        handler.moduleName = "escrowExecComputeTemplate";
        handler.functionName = "fulfillLockRewardCondition";
        handler.version = "0.1";
        executeAgreementEvent.handler = handler;

        serviceAgreementTemplate.events = Arrays.asList(executeAgreementEvent);

        // The templateId of the AccessService is the address of the escrowAccessSecretStoreTemplate contract
        ComputingService computingService = new ComputingService(providerConfig.getAccessEndpoint(),
                Service.DEFAULT_COMPUTING_INDEX,
                serviceAgreementTemplate,
                computingServiceTemplateId);

        computingService.attributes.main.provider = computingProvider;

        computingService.attributes.main.name = "dataAssetAccessServiceAgreement";
        computingService.attributes.main.price = price;
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


    private static ServiceBuilder accessServiceBuilder() {

        return config -> {

            ProviderConfig providerConfig = (ProviderConfig) config.get("providerConfig");
            String accessServiceTemplateId = (String) config.get("accessServiceTemplateId");
            String price = (String)config.get("price");
            String creator = (String)config.get("creator");
            return buildAccessService(providerConfig, accessServiceTemplateId, price, creator);
        };

    }

    private static AccessService buildAccessService(ProviderConfig providerConfig, String accessServiceTemplateId, String price, String creator) {

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
        accessService.attributes.main.price = price;
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
     * Gets the Access ConditionStatusMap Params of a DDO
     *
     * @param did   the did
     * @param price the price
     * @param escrowRewardAddress the address of the EscrowReward Condition
     * @param accessSecretStoreConditionAddress the address of the accessSecretStore condition
     * @param lockRewardConditionAddress the address of the lockReward Condition
     * @return a Map with the params of the Access ConditionStatusMap
     */
    public static  Map<String, Object> getAccessConditionParams(String did, String price,  String escrowRewardAddress, String lockRewardConditionAddress, String accessSecretStoreConditionAddress) {
        Map<String, Object> params = new HashMap<>();
        params.put("parameter.did", did);
        params.put("parameter.price", price);

        params.put("contract.EscrowReward.address", escrowRewardAddress);
        params.put("contract.LockRewardCondition.address", lockRewardConditionAddress);
        params.put("contract.AccessSecretStoreCondition.address", accessSecretStoreConditionAddress);

        params.put("parameter.assetId", did.replace("did:op:", ""));

        return params;
    }

    /**
     * Gets the Comnputing ConditionStatusMap Params of a DDO
     *
     * @param did   the did
     * @param price the price
     * @param escrowRewardAddress the address of the EscrowReward Condition
     * @param lockRewardConditionAddress the address of the lockReward Condition
     * @param execComputeConditionAddress the address of the execCoompute condition
     * @return a Map with the params of the Access ConditionStatusMap
     */
    public static  Map<String, Object> getComputingConditionParams(String did, String price,  String escrowRewardAddress, String lockRewardConditionAddress, String execComputeConditionAddress) {
        Map<String, Object> params = new HashMap<>();
        params.put("parameter.did", did);
        params.put("parameter.price", price);

        params.put("contract.EscrowReward.address", escrowRewardAddress);
        params.put("contract.LockRewardCondition.address", lockRewardConditionAddress);
        params.put("contract.ExecComputeCondition.address", execComputeConditionAddress);

        params.put("parameter.assetId", did.replace("did:op:", ""));

        return params;
    }
}
