package io.keyko.nevermined.api;

import com.oceanprotocol.secretstore.core.EvmDto;
import com.oceanprotocol.secretstore.core.SecretStoreDto;
import com.typesafe.config.Config;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.api.config.NeverminedConfig;
import io.keyko.nevermined.api.config.NeverminedConfigFactory;
import io.keyko.nevermined.api.helper.InitializationHelper;
import io.keyko.nevermined.api.impl.*;
import io.keyko.nevermined.contracts.*;
import io.keyko.nevermined.exceptions.InitializationException;
import io.keyko.nevermined.exceptions.InvalidConfiguration;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.manager.*;
import io.keyko.nevermined.models.Account;
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
public class NeverminedAPI {

    private static final Logger log = LogManager.getLogger(NeverminedAPI.class);

    private NeverminedConfig neverminedConfig;

    private KeeperService keeperService;
    private MetadataApiService metadataApiService;
    private SecretStoreDto secretStoreDto;
    private EvmDto evmDto;

    private SecretStoreManager secretStoreManager;
    private NeverminedManager neverminedManager;
    private AssetsManager assetsManager;
    private AccountsManager accountsManager;
    private AgreementsManager agreementsManager;
    private ConditionsManager conditionsManager;
    private TemplatesManager templatesManager;

    private NeverminedToken tokenContract;
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

    private static NeverminedAPI neverminedAPI = null;


    /**
     * Private constructor
     *
     * @param neverminedConfig the object to configure the API
     */
    private NeverminedAPI(NeverminedConfig neverminedConfig) {
        this.neverminedConfig = neverminedConfig;
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
     * Build an Instance of Nevermined API from a Properties object
     *
     * @param properties values of the configuration
     * @return an Initialized NeverminedAPI object
     * @throws InitializationException InitializationException
     * @throws InvalidConfiguration    InvalidConfiguration
     */
    public static NeverminedAPI getInstance(Properties properties) throws InitializationException, InvalidConfiguration {

        setRxUndeliverableExceptionHandler();

        NeverminedConfig neverminedConfig = NeverminedConfigFactory.getNeverminedConfig(properties);
        NeverminedConfig.NeverminedConfigValidation validation = NeverminedConfig.validate(neverminedConfig);

        if (!validation.isValid()) {
            String msg = "Error Initializing Nevermined API. Configuration not valid " + validation.errorsToString();
            log.error(msg);
            throw new InvalidConfiguration(msg);
        }

        neverminedAPI = new NeverminedAPI(neverminedConfig);

        neverminedAPI.mainAccount = new Account(Keys.toChecksumAddress(neverminedConfig.getMainAccountAddress()), neverminedConfig.getMainAccountPassword());

        InitializationHelper initializationHelper = new InitializationHelper(neverminedConfig);

        try {
            neverminedAPI.neverminedConfig = neverminedConfig;
            neverminedAPI.metadataApiService = initializationHelper.getMetadataService();
            neverminedAPI.keeperService = initializationHelper.getKeeper();
            neverminedAPI.secretStoreDto = initializationHelper.getSecretStoreDto();
            neverminedAPI.evmDto = initializationHelper.getEvmDto();
            neverminedAPI.secretStoreManager = initializationHelper.getSecretStoreManager(neverminedAPI.secretStoreDto, neverminedAPI.evmDto);

            neverminedAPI.didRegistryContract = initializationHelper.loadDIDRegistryContract(neverminedAPI.keeperService);
            neverminedAPI.escrowAccessSecretStoreTemplate = initializationHelper.loadEscrowAccessSecretStoreTemplate(neverminedAPI.keeperService);
            neverminedAPI.lockRewardCondition = initializationHelper.loadLockRewardCondition(neverminedAPI.keeperService);
            neverminedAPI.accessSecretStoreCondition = initializationHelper.loadAccessSecretStoreCondition(neverminedAPI.keeperService);
            neverminedAPI.escrowReward = initializationHelper.loadEscrowReward(neverminedAPI.keeperService);
            neverminedAPI.dispenser = initializationHelper.loadDispenserContract(neverminedAPI.keeperService);
            neverminedAPI.tokenContract = initializationHelper.loadNeverminedTokenContract(neverminedAPI.keeperService);
            neverminedAPI.templateStoreManagerContract = initializationHelper.loadTemplateStoreManagerContract(neverminedAPI.keeperService);
            neverminedAPI.agreementStoreManagerContract = initializationHelper.loadAgreementStoreManager(neverminedAPI.keeperService);
            neverminedAPI.conditionStoreManager = initializationHelper.loadConditionStoreManager(neverminedAPI.keeperService);
            neverminedAPI.computeExecutionCondition = initializationHelper.loadComputeExecutionCondition(neverminedAPI.keeperService);
            neverminedAPI.escrowComputeExecutionTemplate = initializationHelper.loadEscrowComputeExecutionTemplate(neverminedAPI.keeperService);

            neverminedAPI.agreementsManager = initializationHelper.getAgreementsManager(neverminedAPI.keeperService, neverminedAPI.metadataApiService);
            neverminedAPI.agreementsManager
                    .setConditionStoreManagerContract(neverminedAPI.conditionStoreManager)
                    .setEscrowAccessSecretStoreTemplate(neverminedAPI.escrowAccessSecretStoreTemplate)
                    .setAgreementStoreManagerContract(neverminedAPI.agreementStoreManagerContract)
                    .setLockRewardCondition(neverminedAPI.lockRewardCondition)
                    .setAccessSecretStoreCondition(neverminedAPI.accessSecretStoreCondition)
                    .setEscrowReward(neverminedAPI.escrowReward)
                    .setComputeExecutionCondition(neverminedAPI.computeExecutionCondition)
                    .setEscrowComputeExecutionTemplate(neverminedAPI.escrowComputeExecutionTemplate);

            neverminedAPI.templatesManager = initializationHelper.getTemplatesManager(neverminedAPI.keeperService, neverminedAPI.metadataApiService);
            neverminedAPI.templatesManager.setMainAccount(neverminedAPI.mainAccount);
            neverminedAPI.templatesManager.setTemplateStoreManagerContract(neverminedAPI.templateStoreManagerContract);

            neverminedAPI.neverminedManager = initializationHelper.getNeverminedManager(neverminedAPI.keeperService, neverminedAPI.metadataApiService);
            neverminedAPI.neverminedManager
                    .setAgreementManager(neverminedAPI.agreementsManager)
                    .setTemplatesManager(neverminedAPI.templatesManager)
                    .setSecretStoreManager(neverminedAPI.secretStoreManager)
                    .setDidRegistryContract(neverminedAPI.didRegistryContract)
                    .setEscrowAccessSecretStoreTemplate(neverminedAPI.escrowAccessSecretStoreTemplate)
                    .setLockRewardCondition(neverminedAPI.lockRewardCondition)
                    .setEscrowReward(neverminedAPI.escrowReward)
                    .setAccessSecretStoreCondition(neverminedAPI.accessSecretStoreCondition)
                    .setTokenContract(neverminedAPI.tokenContract)
                    .setTemplateStoreManagerContract(neverminedAPI.templateStoreManagerContract)
                    .setAgreementStoreManagerContract(neverminedAPI.agreementStoreManagerContract)
                    .setConditionStoreManagerContract(neverminedAPI.conditionStoreManager)
                    .setComputeExecutionCondition(neverminedAPI.computeExecutionCondition)
                    .setEscrowComputeExecutionTemplate(neverminedAPI.escrowComputeExecutionTemplate)
                    .setMainAccount(neverminedAPI.mainAccount)
                    .setEvmDto(neverminedAPI.evmDto);

            neverminedAPI.accountsManager = initializationHelper.getAccountsManager(neverminedAPI.keeperService, neverminedAPI.metadataApiService);
            neverminedAPI.accountsManager
                    .setTokenContract(neverminedAPI.tokenContract)
                    .setDispenserContract(neverminedAPI.dispenser)
                    .setMainAccount(neverminedAPI.mainAccount);

            neverminedAPI.conditionsManager = initializationHelper.getConditionsManager(neverminedAPI.keeperService, neverminedAPI.metadataApiService);
            neverminedAPI.conditionsManager
                    .setTokenContract(neverminedAPI.tokenContract)
                    .setConditionStoreManagerContract(neverminedAPI.conditionStoreManager)
                    .setEscrowAccessSecretStoreTemplate(neverminedAPI.escrowAccessSecretStoreTemplate)
                    .setAgreementStoreManagerContract(neverminedAPI.agreementStoreManagerContract)
                    .setLockRewardCondition(neverminedAPI.lockRewardCondition)
                    .setAccessSecretStoreCondition(neverminedAPI.accessSecretStoreCondition)
                    .setEscrowReward(neverminedAPI.escrowReward)
                    .setComputeExecutionCondition(neverminedAPI.computeExecutionCondition)
                    .setEscrowComputeExecutionTemplate(neverminedAPI.escrowComputeExecutionTemplate);

            neverminedAPI.assetsManager = initializationHelper.getAssetsManager(neverminedAPI.keeperService, neverminedAPI.metadataApiService);
            neverminedAPI.assetsManager
                    .setMainAccount(neverminedAPI.mainAccount)
                    .setDidRegistryContract(neverminedAPI.didRegistryContract);

            neverminedAPI.accountsAPI = new AccountsImpl(neverminedAPI.accountsManager);
            neverminedAPI.agreementsAPI = new AgreementsImpl(neverminedAPI.agreementsManager, neverminedAPI.neverminedManager);
            neverminedAPI.conditionsAPI = new ConditionsImpl(neverminedAPI.conditionsManager);
            neverminedAPI.tokensAPI = new TokensImpl(neverminedAPI.accountsManager);
            neverminedAPI.secretStoreAPI = new SecretStoreImpl(neverminedAPI.secretStoreManager);
            neverminedAPI.assetsAPI = new AssetsImpl(neverminedAPI.neverminedManager, neverminedAPI.assetsManager, neverminedAPI.agreementsManager);
            neverminedAPI.templatesAPI = new TemplatesImpl(neverminedAPI.templatesManager);

            return neverminedAPI;
        } catch (Exception e) {
            String msg = "Error Initializing Nevermined API";
            log.error(msg + ": " + e.getMessage());
            throw new InitializationException(msg, e);
        }
    }

    /**
     * Build an Instance of Nevermined API from a TypeSafe Config object
     *
     * @param config the config object
     * @return an Initialized NeverminedAPI object
     * @throws InitializationException InitializationException
     * @throws InvalidConfiguration    InvalidConfiguration
     */
    public static NeverminedAPI getInstance(Config config) throws InitializationException, InvalidConfiguration {
        return NeverminedAPI.getInstance(NeverminedAPI.toProperties(config));
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
        neverminedAPI.templatesManager.setTemplateStoreManagerContract(
                contract);

    }
}
