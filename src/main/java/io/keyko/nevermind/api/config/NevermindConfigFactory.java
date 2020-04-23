package io.keyko.nevermind.api.config;


import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.Properties;


/**
 * Factory to get instances of NevermindConfig
 */
public class NevermindConfigFactory {


    private static final String DEFAULT_KEEPER_URL = "http://localhost:8545";
    private static final BigInteger DEFAULT_KEEPER_GAS_LIMIT = BigInteger.valueOf(4712388l);
    private static final BigInteger DEFAULT_KEEPER_GAS_PRICE = BigInteger.valueOf(100000000000l);
    private static final String DEFAULT_METADATA_URL = "http://localhost:5000";
    private static final String DEFAULT_SECRET_STORE_URL = "http://localhost:12001";
    private static final String DEFAULT_CONSUME_PATH = "/tmp";


    /**
     * Creates an NevermindConfig object from a set of properties
     *
     * @param properties configuration values
     * @return an NevermindConfig value with all the values set
     */
    public static NevermindConfig getNevermindConfig(Properties properties) {

        NevermindConfig nevermindConfig = new NevermindConfig();

        properties.getOrDefault(NevermindConfig.CONSUME_BASE_PATH, DEFAULT_CONSUME_PATH);

        nevermindConfig.setKeeperUrl((String) properties.getOrDefault(NevermindConfig.KEEPER_URL, DEFAULT_KEEPER_URL));
        nevermindConfig.setKeeperGasLimit(new BigInteger((String) properties.getOrDefault(NevermindConfig.KEEPER_GAS_LIMIT, DEFAULT_KEEPER_GAS_LIMIT.toString())));
        nevermindConfig.setKeeperGasPrice(new BigInteger((String) properties.getOrDefault(NevermindConfig.KEEPER_GAS_PRICE, DEFAULT_KEEPER_GAS_PRICE.toString())));
        nevermindConfig.setKeeperTxAttempts(Integer.parseInt(
                (String) properties.getOrDefault(
                        NevermindConfig.KEEPER_TX_ATTEMPTS, String.valueOf(TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH))
        ));
        nevermindConfig.setKeeperTxSleepDuration(
                Long.parseLong(
                        (String) properties.getOrDefault(NevermindConfig.KEEPER_TX_SLEEPDURATION, String.valueOf(TransactionManager.DEFAULT_POLLING_FREQUENCY))
                ));

        nevermindConfig.setMetadataUrl((String) properties.getOrDefault(NevermindConfig.METADATA_URL, DEFAULT_METADATA_URL));
        nevermindConfig.setSecretStoreUrl((String) properties.getOrDefault(NevermindConfig.SECRETSTORE_URL, DEFAULT_SECRET_STORE_URL));
        nevermindConfig.setProviderAddress((String) properties.getOrDefault(NevermindConfig.PROVIDER_ADDRESS, ""));
        nevermindConfig.setDidRegistryAddress((String) properties.getOrDefault(NevermindConfig.DID_REGISTRY_ADDRESS, ""));
        nevermindConfig.setEscrowRewardConditionsAddress((String) properties.getOrDefault(NevermindConfig.ESCROWREWARD_CONDITIONS_ADDRESS, ""));
        nevermindConfig.setEscrowAccessSecretStoreTemplateAddress((String) properties.getOrDefault(NevermindConfig.ESCROW_ACCESS_SS_CONDITIONS_ADDRESS, ""));
        nevermindConfig.setLockrewardConditionsAddress((String) properties.getOrDefault(NevermindConfig.LOCKREWARD_CONDITIONS_ADDRESS, ""));
        nevermindConfig.setAccessSsConditionsAddress((String) properties.getOrDefault(NevermindConfig.ACCESS_SS_CONDITIONS_ADDRESS, ""));
        nevermindConfig.setAgreementStoreManagerAddress((String) properties.getOrDefault(NevermindConfig.AGREEMENT_STORE_MANAGER_ADDRESS, ""));
        nevermindConfig.setConditionStoreManagerAddress((String) properties.getOrDefault(NevermindConfig.CONDITION_STORE_MANAGER_ADDRESS, ""));
        nevermindConfig.setTokenAddress((String) properties.getOrDefault(NevermindConfig.TOKEN_ADDRESS, ""));
        nevermindConfig.setTemplateStoreManagerAddress((String) properties.getOrDefault(NevermindConfig.TEMPLATE_STORE_MANAGER_ADDRESS, ""));
        nevermindConfig.setDispenserAddress((String) properties.getOrDefault(NevermindConfig.DISPENSER_ADDRESS, ""));
        nevermindConfig.setConsumeBasePath((String) properties.getOrDefault(NevermindConfig.CONSUME_BASE_PATH, DEFAULT_CONSUME_PATH));
        nevermindConfig.setMainAccountAddress((String) properties.getOrDefault(NevermindConfig.MAIN_ACCOUNT_ADDRESS, ""));
        nevermindConfig.setMainAccountPassword((String) properties.getOrDefault(NevermindConfig.MAIN_ACCOUNT_PASSWORD, ""));
        nevermindConfig.setMainAccountCredentialsFile((String) properties.getOrDefault(NevermindConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, ""));

        nevermindConfig.setEscrowComputeExecutionTemplateAddress((String) properties.getOrDefault(NevermindConfig.ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS, ""));
        nevermindConfig.setComputeExecutionConditionAddress((String) properties.getOrDefault(NevermindConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, ""));

        return nevermindConfig;

    }
}
