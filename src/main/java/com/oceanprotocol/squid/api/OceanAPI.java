/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api;

import com.oceanprotocol.common.web3.KeeperService;
import com.oceanprotocol.keeper.contracts.*;
import com.oceanprotocol.secretstore.core.EvmDto;
import com.oceanprotocol.secretstore.core.SecretStoreDto;
import com.oceanprotocol.squid.api.config.OceanConfig;
import com.oceanprotocol.squid.api.config.OceanConfigFactory;
import com.oceanprotocol.squid.api.helper.OceanInitializationHelper;
import com.oceanprotocol.squid.api.impl.*;
import com.oceanprotocol.squid.exceptions.InitializationException;
import com.oceanprotocol.squid.exceptions.InvalidConfiguration;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.squid.manager.*;
import com.oceanprotocol.squid.models.Account;
import com.typesafe.config.Config;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.Keys;

import java.io.IOException;
import java.net.SocketException;
import java.util.Properties;

/**
 * Class that represents the entry point to initialize and use the API
 */
public class OceanAPI {

    private static final Logger log = LogManager.getLogger(OceanAPI.class);

    private OceanConfig oceanConfig;

    private KeeperService keeperService;
    private AquariusService aquariusService;
    private SecretStoreDto secretStoreDto;
    private EvmDto evmDto;

    private SecretStoreManager secretStoreManager;
    private OceanManager oceanManager;
    private AssetsManager assetsManager;
    private AccountsManager accountsManager;
    private AgreementsManager agreementsManager;
    private ConditionsManager conditionsManager;
    private TemplatesManager templatesManager;

    private OceanToken tokenContract;
    private Dispenser dispenser;
    private DIDRegistry didRegistryContract;
    private EscrowAccessSecretStoreTemplate escrowAccessSecretStoreTemplate;
    private LockRewardCondition lockRewardCondition;
    private AccessSecretStoreCondition accessSecretStoreCondition;
    private EscrowReward escrowReward;
    private TemplateStoreManager templateStoreManagerContract;
    private AgreementStoreManager agreementStoreManagerContract;
    private ConditionStoreManager conditionStoreManager;

    private ComputeExecutionCondition computeExecutionCondition;
    private EscrowComputeExecutionTemplate escrowComputeExecutionTemplate;

    private AccountsAPI accountsAPI;
    private AgreementsAPI agreementsAPI;
    private ConditionsAPI conditionsAPI;
    private TokensAPI tokensAPI;
    private AssetsAPI assetsAPI;
    private SecretStoreAPI secretStoreAPI;
    private TemplatesAPI templatesAPI;

    private Account mainAccount;

    private static OceanAPI oceanAPI = null;


    /**
     * Private constructor
     *
     * @param oceanConfig the object to configure the API
     */
    private OceanAPI(OceanConfig oceanConfig) {
        this.oceanConfig = oceanConfig;
    }

    /**
     * Transform a TypeSafe Config object into a Java's Properties
     *
     * @param config the config object
     * @return a Properties object with the configuration of the API
     */
    private static Properties toProperties(Config config) {
        Properties properties = new Properties();
        config.entrySet().forEach(e -> properties.setProperty(e.getKey(), config.getString(e.getKey())));
        return properties;
    }

    private static void setRxUndeliverableExceptionHandler() {

        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if ((e instanceof IOException) || (e instanceof SocketException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {

                // that's likely a bug in the application
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);

                // .handleException(Thread.currentThread(), e);
                return;
            }
            if (e instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
                //        .handleException(Thread.currentThread(), e);
                return;
            }

            log.warn("Undeliverable exception received:  " + e.getMessage());
        });
    }

    /**
     * Build an Instance of Ocean API from a Properties object
     *
     * @param properties values of the configuration
     * @return an Initialized OceanAPI object
     * @throws InitializationException InitializationException
     * @throws InvalidConfiguration    InvalidConfiguration
     */
    public static OceanAPI getInstance(Properties properties) throws InitializationException, InvalidConfiguration {

        setRxUndeliverableExceptionHandler();

        OceanConfig oceanConfig = OceanConfigFactory.getOceanConfig(properties);
        OceanConfig.OceanConfigValidation validation = OceanConfig.validate(oceanConfig);

        if (!validation.isValid()) {
            String msg = "Error Initializing Ocean API. Configuration not valid " + validation.errorsToString();
            log.error(msg);
            throw new InvalidConfiguration(msg);
        }

        oceanAPI = new OceanAPI(oceanConfig);

        oceanAPI.mainAccount = new Account(Keys.toChecksumAddress(oceanConfig.getMainAccountAddress()), oceanConfig.getMainAccountPassword());

        OceanInitializationHelper oceanInitializationHelper = new OceanInitializationHelper(oceanConfig);

        try {
            oceanAPI.oceanConfig = oceanConfig;
            oceanAPI.aquariusService = oceanInitializationHelper.getAquarius();
            oceanAPI.keeperService = oceanInitializationHelper.getKeeper();
            oceanAPI.secretStoreDto = oceanInitializationHelper.getSecretStoreDto();
            oceanAPI.evmDto = oceanInitializationHelper.getEvmDto();
            oceanAPI.secretStoreManager = oceanInitializationHelper.getSecretStoreManager(oceanAPI.secretStoreDto, oceanAPI.evmDto);

            oceanAPI.didRegistryContract = oceanInitializationHelper.loadDIDRegistryContract(oceanAPI.keeperService);
            oceanAPI.escrowAccessSecretStoreTemplate = oceanInitializationHelper.loadEscrowAccessSecretStoreTemplate(oceanAPI.keeperService);
            oceanAPI.lockRewardCondition = oceanInitializationHelper.loadLockRewardCondition(oceanAPI.keeperService);
            oceanAPI.accessSecretStoreCondition = oceanInitializationHelper.loadAccessSecretStoreCondition(oceanAPI.keeperService);
            oceanAPI.escrowReward = oceanInitializationHelper.loadEscrowReward(oceanAPI.keeperService);
            oceanAPI.dispenser = oceanInitializationHelper.loadDispenserContract(oceanAPI.keeperService);
            oceanAPI.tokenContract = oceanInitializationHelper.loadOceanTokenContract(oceanAPI.keeperService);
            oceanAPI.templateStoreManagerContract = oceanInitializationHelper.loadTemplateStoreManagerContract(oceanAPI.keeperService);
            oceanAPI.agreementStoreManagerContract = oceanInitializationHelper.loadAgreementStoreManager(oceanAPI.keeperService);
            oceanAPI.conditionStoreManager = oceanInitializationHelper.loadConditionStoreManager(oceanAPI.keeperService);
            oceanAPI.computeExecutionCondition = oceanInitializationHelper.loadComputeExecutionCondition(oceanAPI.keeperService);
            oceanAPI.escrowComputeExecutionTemplate = oceanInitializationHelper.loadEscrowComputeExecutionTemplate(oceanAPI.keeperService);

            oceanAPI.agreementsManager = oceanInitializationHelper.getAgreementsManager(oceanAPI.keeperService, oceanAPI.aquariusService);
            oceanAPI.agreementsManager
                    .setConditionStoreManagerContract(oceanAPI.conditionStoreManager)
                    .setEscrowAccessSecretStoreTemplate(oceanAPI.escrowAccessSecretStoreTemplate)
                    .setAgreementStoreManagerContract(oceanAPI.agreementStoreManagerContract)
                    .setLockRewardCondition(oceanAPI.lockRewardCondition)
                    .setAccessSecretStoreCondition(oceanAPI.accessSecretStoreCondition)
                    .setEscrowReward(oceanAPI.escrowReward)
                    .setComputeExecutionCondition(oceanAPI.computeExecutionCondition)
                    .setEscrowComputeExecutionTemplate(oceanAPI.escrowComputeExecutionTemplate);

            oceanAPI.templatesManager = oceanInitializationHelper.getTemplatesManager(oceanAPI.keeperService, oceanAPI.aquariusService);
            oceanAPI.templatesManager.setMainAccount(oceanAPI.mainAccount);
            oceanAPI.templatesManager.setTemplateStoreManagerContract(oceanAPI.templateStoreManagerContract);

            oceanAPI.oceanManager = oceanInitializationHelper.getOceanManager(oceanAPI.keeperService, oceanAPI.aquariusService);
            oceanAPI.oceanManager
                    .setAgreementManager(oceanAPI.agreementsManager)
                    .setTemplatesManager(oceanAPI.templatesManager)
                    .setSecretStoreManager(oceanAPI.secretStoreManager)
                    .setDidRegistryContract(oceanAPI.didRegistryContract)
                    .setEscrowAccessSecretStoreTemplate(oceanAPI.escrowAccessSecretStoreTemplate)
                    .setLockRewardCondition(oceanAPI.lockRewardCondition)
                    .setEscrowReward(oceanAPI.escrowReward)
                    .setAccessSecretStoreCondition(oceanAPI.accessSecretStoreCondition)
                    .setTokenContract(oceanAPI.tokenContract)
                    .setTemplateStoreManagerContract(oceanAPI.templateStoreManagerContract)
                    .setAgreementStoreManagerContract(oceanAPI.agreementStoreManagerContract)
                    .setConditionStoreManagerContract(oceanAPI.conditionStoreManager)
                    .setComputeExecutionCondition(oceanAPI.computeExecutionCondition)
                    .setEscrowComputeExecutionTemplate(oceanAPI.escrowComputeExecutionTemplate)
                    .setMainAccount(oceanAPI.mainAccount)
                    .setEvmDto(oceanAPI.evmDto);

            oceanAPI.accountsManager = oceanInitializationHelper.getAccountsManager(oceanAPI.keeperService, oceanAPI.aquariusService);
            oceanAPI.accountsManager
                    .setTokenContract(oceanAPI.tokenContract)
                    .setDispenserContract(oceanAPI.dispenser)
                    .setMainAccount(oceanAPI.mainAccount);

            oceanAPI.conditionsManager = oceanInitializationHelper.getConditionsManager(oceanAPI.keeperService, oceanAPI.aquariusService);
            oceanAPI.conditionsManager
                    .setTokenContract(oceanAPI.tokenContract)
                    .setConditionStoreManagerContract(oceanAPI.conditionStoreManager)
                    .setEscrowAccessSecretStoreTemplate(oceanAPI.escrowAccessSecretStoreTemplate)
                    .setAgreementStoreManagerContract(oceanAPI.agreementStoreManagerContract)
                    .setLockRewardCondition(oceanAPI.lockRewardCondition)
                    .setAccessSecretStoreCondition(oceanAPI.accessSecretStoreCondition)
                    .setEscrowReward(oceanAPI.escrowReward)
                    .setComputeExecutionCondition(oceanAPI.computeExecutionCondition)
                    .setEscrowComputeExecutionTemplate(oceanAPI.escrowComputeExecutionTemplate);

            oceanAPI.assetsManager = oceanInitializationHelper.getAssetsManager(oceanAPI.keeperService, oceanAPI.aquariusService);
            oceanAPI.assetsManager
                    .setMainAccount(oceanAPI.mainAccount)
                    .setDidRegistryContract(oceanAPI.didRegistryContract);

            oceanAPI.accountsAPI = new AccountsImpl(oceanAPI.accountsManager);
            oceanAPI.agreementsAPI = new AgreementsImpl(oceanAPI.agreementsManager, oceanAPI.oceanManager);
            oceanAPI.conditionsAPI = new ConditionsImpl(oceanAPI.conditionsManager);
            oceanAPI.tokensAPI = new TokensImpl(oceanAPI.accountsManager);
            oceanAPI.secretStoreAPI = new SecretStoreImpl(oceanAPI.secretStoreManager);
            oceanAPI.assetsAPI = new AssetsImpl(oceanAPI.oceanManager, oceanAPI.assetsManager, oceanAPI.agreementsManager);
            oceanAPI.templatesAPI = new TemplatesImpl(oceanAPI.templatesManager);

            return oceanAPI;
        } catch (Exception e) {
            String msg = "Error Initializing Ocean API";
            log.error(msg + ": " + e.getMessage());
            throw new InitializationException(msg, e);
        }
    }

    /**
     * Build an Instance of Ocean API from a TypeSafe Config object
     *
     * @param config the config object
     * @return an Initialized OceanAPI object
     * @throws InitializationException InitializationException
     * @throws InvalidConfiguration    InvalidConfiguration
     */
    public static OceanAPI getInstance(Config config) throws InitializationException, InvalidConfiguration {
        return OceanAPI.getInstance(OceanAPI.toProperties(config));
    }

    /**
     * Gets the account used to initialized the API
     *
     * @return the account used to initialized the API
     */
    public Account getMainAccount() {
        return this.mainAccount;
    }

    /**
     * Gets the AccountsAPI
     *
     * @return an instance of an Implementation class of AccountsAPI
     */
    public AccountsAPI getAccountsAPI() {
        return this.accountsAPI;
    }

    /**
     * Gets the AgreementsAPI
     *
     * @return an instance of an Implementation class of AgreementsAPI
     */
    public AgreementsAPI getAgreementsAPI() {
        return this.agreementsAPI;
    }

    /**
     * Gets the ConditionsAPI
     *
     * @return an instance of an Implementation class of ConditionsAPI
     */
    public ConditionsAPI getConditionsAPI() {
        return this.conditionsAPI;
    }

    /**
     * Gets the TokensAPI
     *
     * @return an instance of an Implementation class of TokensAPI
     */
    public TokensAPI getTokensAPI() {
        return this.tokensAPI;
    }


    /**
     * Gets the AssetsAPI
     *
     * @return an instance of an Implementation class of AssetsAPI
     */
    public AssetsAPI getAssetsAPI() {
        return this.assetsAPI;
    }

    /**
     * Gets the SecretStoreAPI
     *
     * @return an instance of an Implementation class of SecretStoreAPI
     */
    public SecretStoreAPI getSecretStoreAPI() {
        return this.secretStoreAPI;
    }

    /**
     * Gets the TemplatesAPI
     *
     * @return an instance of an Implementation class of TemplatesAPI
     */
    public TemplatesAPI getTemplatesAPI() {
        return this.templatesAPI;
    }

    // TODO: Review an alternative to introduce a cleaner dependency injection

    /**
     * Allows to overwrite the TemplateStoreManager contract instance
     *
     * @param contract TemplateStoreManager
     */
    public void setTemplateStoreManagerContract(TemplateStoreManager contract) {
        oceanAPI.templatesManager.setTemplateStoreManagerContract(
                contract);

    }
}
