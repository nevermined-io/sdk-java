package io.keyko.ocean.api;

import io.keyko.common.web3.KeeperService;
import io.keyko.ocean.keeper.contracts.*;
import com.oceanprotocol.secretstore.core.EvmDto;
import com.oceanprotocol.secretstore.core.SecretStoreDto;
import com.typesafe.config.Config;
import io.keyko.ocean.api.config.OceanConfig;
import io.keyko.ocean.api.config.OceanConfigFactory;
import io.keyko.ocean.api.helper.OceanInitializationHelper;
import io.keyko.ocean.api.impl.*;
import io.keyko.ocean.exceptions.InitializationException;
import io.keyko.ocean.exceptions.InvalidConfiguration;
import io.keyko.ocean.external.AquariusService;
import io.keyko.ocean.manager.*;
import io.keyko.ocean.models.Account;
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
    private LockRewardCondition lockRewardCondition;
    private AccessSecretStoreCondition accessSecretStoreCondition;
    private EscrowReward escrowReward;
    private TemplateStoreManager templateStoreManagerContract;
    private AgreementStoreManager agreementStoreManagerContract;
    private ConditionStoreManager conditionStoreManager;

    private ComputeExecutionCondition computeExecutionCondition;

    private AccountsAPI accountsAPI;
    private AgreementsAPI agreementsAPI;
    private ConditionsAPI conditionsAPI;
    private TokensAPI tokensAPI;
    private AssetsAPI assetsAPI;
    private SecretStoreAPI secretStoreAPI;
    private TemplatesAPI templatesAPI;

    private Account mainAccount;

//    private static OceanAPI oceanAPI = null;


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

        OceanAPI api = new OceanAPI(oceanConfig);

        api.mainAccount = new Account(Keys.toChecksumAddress(oceanConfig.getMainAccountAddress()), oceanConfig.getMainAccountPassword());

        OceanInitializationHelper oceanInitializationHelper = new OceanInitializationHelper(oceanConfig);

        try {
            api.oceanConfig = oceanConfig;
            api.aquariusService = oceanInitializationHelper.getAquarius();
            api.keeperService = oceanInitializationHelper.getKeeper();
            api.secretStoreDto = oceanInitializationHelper.getSecretStoreDto();
            api.evmDto = oceanInitializationHelper.getEvmDto();
            api.secretStoreManager = oceanInitializationHelper.getSecretStoreManager(api.secretStoreDto, api.evmDto);

            api.didRegistryContract = oceanInitializationHelper.loadDIDRegistryContract(api.keeperService);
            api.lockRewardCondition = oceanInitializationHelper.loadLockRewardCondition(api.keeperService);
            api.accessSecretStoreCondition = oceanInitializationHelper.loadAccessSecretStoreCondition(api.keeperService);
            api.escrowReward = oceanInitializationHelper.loadEscrowReward(api.keeperService);
            api.dispenser = oceanInitializationHelper.loadDispenserContract(api.keeperService);
            api.tokenContract = oceanInitializationHelper.loadOceanTokenContract(api.keeperService);
            api.templateStoreManagerContract = oceanInitializationHelper.loadTemplateStoreManagerContract(api.keeperService);
            api.agreementStoreManagerContract = oceanInitializationHelper.loadAgreementStoreManager(api.keeperService);
            api.conditionStoreManager = oceanInitializationHelper.loadConditionStoreManager(api.keeperService);
            api.computeExecutionCondition = oceanInitializationHelper.loadComputeExecutionCondition(api.keeperService);

            api.agreementsManager = oceanInitializationHelper.getAgreementsManager(api.keeperService, api.aquariusService);
            api.agreementsManager
                    .setConditionStoreManagerContract(api.conditionStoreManager)
                    .setAgreementStoreManagerContract(api.agreementStoreManagerContract)
                    .setLockRewardCondition(api.lockRewardCondition)
                    .setAccessSecretStoreCondition(api.accessSecretStoreCondition)
                    .setEscrowReward(api.escrowReward)
                    .setComputeExecutionCondition(api.computeExecutionCondition);

            api.templatesManager = oceanInitializationHelper.getTemplatesManager(api.keeperService, api.aquariusService);
            api.templatesManager.setMainAccount(api.mainAccount);
            api.templatesManager.setTemplateStoreManagerContract(api.templateStoreManagerContract);

            api.oceanManager = oceanInitializationHelper.getOceanManager(api.keeperService, api.aquariusService);
            api.oceanManager
                    .setAgreementManager(api.agreementsManager)
                    .setTemplatesManager(api.templatesManager)
                    .setSecretStoreManager(api.secretStoreManager)
                    .setDidRegistryContract(api.didRegistryContract)
                    .setLockRewardCondition(api.lockRewardCondition)
                    .setEscrowReward(api.escrowReward)
                    .setAccessSecretStoreCondition(api.accessSecretStoreCondition)
                    .setTokenContract(api.tokenContract)
                    .setTemplateStoreManagerContract(api.templateStoreManagerContract)
                    .setAgreementStoreManagerContract(api.agreementStoreManagerContract)
                    .setConditionStoreManagerContract(api.conditionStoreManager)
                    .setComputeExecutionCondition(api.computeExecutionCondition)
                    .setMainAccount(api.mainAccount)
                    .setEvmDto(api.evmDto);

            api.accountsManager = oceanInitializationHelper.getAccountsManager(api.keeperService, api.aquariusService);
            api.accountsManager
                    .setTokenContract(api.tokenContract)
                    .setDispenserContract(api.dispenser)
                    .setMainAccount(api.mainAccount);

            api.conditionsManager = oceanInitializationHelper.getConditionsManager(api.keeperService, api.aquariusService);
            api.conditionsManager
                    .setTokenContract(api.tokenContract)
                    .setConditionStoreManagerContract(api.conditionStoreManager)
                    .setAgreementStoreManagerContract(api.agreementStoreManagerContract)
                    .setLockRewardCondition(api.lockRewardCondition)
                    .setAccessSecretStoreCondition(api.accessSecretStoreCondition)
                    .setEscrowReward(api.escrowReward)
                    .setComputeExecutionCondition(api.computeExecutionCondition);

            api.assetsManager = oceanInitializationHelper.getAssetsManager(api.keeperService, api.aquariusService);
            api.assetsManager
                    .setMainAccount(api.mainAccount)
                    .setDidRegistryContract(api.didRegistryContract);

            api.accountsAPI = new AccountsImpl(api.accountsManager);
            api.agreementsAPI = new AgreementsImpl(api.agreementsManager, api.oceanManager);
            api.conditionsAPI = new ConditionsImpl(api.conditionsManager);
            api.tokensAPI = new TokensImpl(api.accountsManager);
            api.secretStoreAPI = new SecretStoreImpl(api.secretStoreManager);
            api.assetsAPI = new AssetsImpl(api.oceanManager, api.assetsManager, api.agreementsManager);
            api.templatesAPI = new TemplatesImpl(api.templatesManager);

            return api;
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
        templatesManager.setTemplateStoreManagerContract(
                contract);

    }
}
