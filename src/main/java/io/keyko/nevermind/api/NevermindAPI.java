package io.keyko.nevermind.api;

import com.oceanprotocol.secretstore.core.EvmDto;
import com.oceanprotocol.secretstore.core.SecretStoreDto;
import com.typesafe.config.Config;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermind.api.config.NevermindConfig;
import io.keyko.nevermind.api.config.NevermindConfigFactory;
import io.keyko.nevermind.api.helper.InitializationHelper;
import io.keyko.nevermind.api.impl.*;
import io.keyko.nevermind.contracts.*;
import io.keyko.nevermind.exceptions.InitializationException;
import io.keyko.nevermind.exceptions.InvalidConfiguration;
import io.keyko.nevermind.external.MetadataService;
import io.keyko.nevermind.manager.*;
import io.keyko.nevermind.models.Account;
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
public class NevermindAPI {

    private static final Logger log = LogManager.getLogger(NevermindAPI.class);

    private NevermindConfig nevermindConfig;

    private KeeperService keeperService;
    private MetadataService metadataService;
    private SecretStoreDto secretStoreDto;
    private EvmDto evmDto;

    private SecretStoreManager secretStoreManager;
    private NevermindManager nevermindManager;
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

    private static NevermindAPI nevermindAPI = null;


    /**
     * Private constructor
     *
     * @param nevermindConfig the object to configure the API
     */
    private NevermindAPI(NevermindConfig nevermindConfig) {
        this.nevermindConfig = nevermindConfig;
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
     * @return an Initialized NevermindAPI object
     * @throws InitializationException InitializationException
     * @throws InvalidConfiguration    InvalidConfiguration
     */
    public static NevermindAPI getInstance(Properties properties) throws InitializationException, InvalidConfiguration {

        setRxUndeliverableExceptionHandler();

        NevermindConfig nevermindConfig = NevermindConfigFactory.getNevermindConfig(properties);
        NevermindConfig.NevermindConfigValidation validation = NevermindConfig.validate(nevermindConfig);

        if (!validation.isValid()) {
            String msg = "Error Initializing Ocean API. Configuration not valid " + validation.errorsToString();
            log.error(msg);
            throw new InvalidConfiguration(msg);
        }

        nevermindAPI = new NevermindAPI(nevermindConfig);

        nevermindAPI.mainAccount = new Account(Keys.toChecksumAddress(nevermindConfig.getMainAccountAddress()), nevermindConfig.getMainAccountPassword());

        InitializationHelper initializationHelper = new InitializationHelper(nevermindConfig);

        try {
            nevermindAPI.nevermindConfig = nevermindConfig;
            nevermindAPI.metadataService = initializationHelper.getMetadataService();
            nevermindAPI.keeperService = initializationHelper.getKeeper();
            nevermindAPI.secretStoreDto = initializationHelper.getSecretStoreDto();
            nevermindAPI.evmDto = initializationHelper.getEvmDto();
            nevermindAPI.secretStoreManager = initializationHelper.getSecretStoreManager(nevermindAPI.secretStoreDto, nevermindAPI.evmDto);

            nevermindAPI.didRegistryContract = initializationHelper.loadDIDRegistryContract(nevermindAPI.keeperService);
            nevermindAPI.escrowAccessSecretStoreTemplate = initializationHelper.loadEscrowAccessSecretStoreTemplate(nevermindAPI.keeperService);
            nevermindAPI.lockRewardCondition = initializationHelper.loadLockRewardCondition(nevermindAPI.keeperService);
            nevermindAPI.accessSecretStoreCondition = initializationHelper.loadAccessSecretStoreCondition(nevermindAPI.keeperService);
            nevermindAPI.escrowReward = initializationHelper.loadEscrowReward(nevermindAPI.keeperService);
            nevermindAPI.dispenser = initializationHelper.loadDispenserContract(nevermindAPI.keeperService);
            nevermindAPI.tokenContract = initializationHelper.loadOceanTokenContract(nevermindAPI.keeperService);
            nevermindAPI.templateStoreManagerContract = initializationHelper.loadTemplateStoreManagerContract(nevermindAPI.keeperService);
            nevermindAPI.agreementStoreManagerContract = initializationHelper.loadAgreementStoreManager(nevermindAPI.keeperService);
            nevermindAPI.conditionStoreManager = initializationHelper.loadConditionStoreManager(nevermindAPI.keeperService);
            nevermindAPI.computeExecutionCondition = initializationHelper.loadComputeExecutionCondition(nevermindAPI.keeperService);
            nevermindAPI.escrowComputeExecutionTemplate = initializationHelper.loadEscrowComputeExecutionTemplate(nevermindAPI.keeperService);

            nevermindAPI.agreementsManager = initializationHelper.getAgreementsManager(nevermindAPI.keeperService, nevermindAPI.metadataService);
            nevermindAPI.agreementsManager
                    .setConditionStoreManagerContract(nevermindAPI.conditionStoreManager)
                    .setEscrowAccessSecretStoreTemplate(nevermindAPI.escrowAccessSecretStoreTemplate)
                    .setAgreementStoreManagerContract(nevermindAPI.agreementStoreManagerContract)
                    .setLockRewardCondition(nevermindAPI.lockRewardCondition)
                    .setAccessSecretStoreCondition(nevermindAPI.accessSecretStoreCondition)
                    .setEscrowReward(nevermindAPI.escrowReward)
                    .setComputeExecutionCondition(nevermindAPI.computeExecutionCondition)
                    .setEscrowComputeExecutionTemplate(nevermindAPI.escrowComputeExecutionTemplate);

            nevermindAPI.templatesManager = initializationHelper.getTemplatesManager(nevermindAPI.keeperService, nevermindAPI.metadataService);
            nevermindAPI.templatesManager.setMainAccount(nevermindAPI.mainAccount);
            nevermindAPI.templatesManager.setTemplateStoreManagerContract(nevermindAPI.templateStoreManagerContract);

            nevermindAPI.nevermindManager = initializationHelper.getOceanManager(nevermindAPI.keeperService, nevermindAPI.metadataService);
            nevermindAPI.nevermindManager
                    .setAgreementManager(nevermindAPI.agreementsManager)
                    .setTemplatesManager(nevermindAPI.templatesManager)
                    .setSecretStoreManager(nevermindAPI.secretStoreManager)
                    .setDidRegistryContract(nevermindAPI.didRegistryContract)
                    .setEscrowAccessSecretStoreTemplate(nevermindAPI.escrowAccessSecretStoreTemplate)
                    .setLockRewardCondition(nevermindAPI.lockRewardCondition)
                    .setEscrowReward(nevermindAPI.escrowReward)
                    .setAccessSecretStoreCondition(nevermindAPI.accessSecretStoreCondition)
                    .setTokenContract(nevermindAPI.tokenContract)
                    .setTemplateStoreManagerContract(nevermindAPI.templateStoreManagerContract)
                    .setAgreementStoreManagerContract(nevermindAPI.agreementStoreManagerContract)
                    .setConditionStoreManagerContract(nevermindAPI.conditionStoreManager)
                    .setComputeExecutionCondition(nevermindAPI.computeExecutionCondition)
                    .setEscrowComputeExecutionTemplate(nevermindAPI.escrowComputeExecutionTemplate)
                    .setMainAccount(nevermindAPI.mainAccount)
                    .setEvmDto(nevermindAPI.evmDto);

            nevermindAPI.accountsManager = initializationHelper.getAccountsManager(nevermindAPI.keeperService, nevermindAPI.metadataService);
            nevermindAPI.accountsManager
                    .setTokenContract(nevermindAPI.tokenContract)
                    .setDispenserContract(nevermindAPI.dispenser)
                    .setMainAccount(nevermindAPI.mainAccount);

            nevermindAPI.conditionsManager = initializationHelper.getConditionsManager(nevermindAPI.keeperService, nevermindAPI.metadataService);
            nevermindAPI.conditionsManager
                    .setTokenContract(nevermindAPI.tokenContract)
                    .setConditionStoreManagerContract(nevermindAPI.conditionStoreManager)
                    .setEscrowAccessSecretStoreTemplate(nevermindAPI.escrowAccessSecretStoreTemplate)
                    .setAgreementStoreManagerContract(nevermindAPI.agreementStoreManagerContract)
                    .setLockRewardCondition(nevermindAPI.lockRewardCondition)
                    .setAccessSecretStoreCondition(nevermindAPI.accessSecretStoreCondition)
                    .setEscrowReward(nevermindAPI.escrowReward)
                    .setComputeExecutionCondition(nevermindAPI.computeExecutionCondition)
                    .setEscrowComputeExecutionTemplate(nevermindAPI.escrowComputeExecutionTemplate);

            nevermindAPI.assetsManager = initializationHelper.getAssetsManager(nevermindAPI.keeperService, nevermindAPI.metadataService);
            nevermindAPI.assetsManager
                    .setMainAccount(nevermindAPI.mainAccount)
                    .setDidRegistryContract(nevermindAPI.didRegistryContract);

            nevermindAPI.accountsAPI = new AccountsImpl(nevermindAPI.accountsManager);
            nevermindAPI.agreementsAPI = new AgreementsImpl(nevermindAPI.agreementsManager, nevermindAPI.nevermindManager);
            nevermindAPI.conditionsAPI = new ConditionsImpl(nevermindAPI.conditionsManager);
            nevermindAPI.tokensAPI = new TokensImpl(nevermindAPI.accountsManager);
            nevermindAPI.secretStoreAPI = new SecretStoreImpl(nevermindAPI.secretStoreManager);
            nevermindAPI.assetsAPI = new AssetsImpl(nevermindAPI.nevermindManager, nevermindAPI.assetsManager, nevermindAPI.agreementsManager);
            nevermindAPI.templatesAPI = new TemplatesImpl(nevermindAPI.templatesManager);

            return nevermindAPI;
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
     * @return an Initialized NevermindAPI object
     * @throws InitializationException InitializationException
     * @throws InvalidConfiguration    InvalidConfiguration
     */
    public static NevermindAPI getInstance(Config config) throws InitializationException, InvalidConfiguration {
        return NevermindAPI.getInstance(NevermindAPI.toProperties(config));
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
        nevermindAPI.templatesManager.setTemplateStoreManagerContract(
                contract);

    }
}
