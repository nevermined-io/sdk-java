package io.keyko.nevermined.api.config;


import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.Properties;


/**
 * Factory to get instances of NeverminedConfig
 */
public class NeverminedConfigFactory {


    private static final String DEFAULT_KEEPER_URL = "http://localhost:8545";
    private static final BigInteger DEFAULT_KEEPER_GAS_LIMIT = BigInteger.valueOf(4712388l);
    private static final BigInteger DEFAULT_KEEPER_GAS_PRICE = BigInteger.valueOf(100000000000l);
    private static final String DEFAULT_METADATA_URL = "http://localhost:5000";
    private static final String DEFAULT_SECRET_STORE_URL = "http://localhost:12001";
    private static final String DEFAULT_FAUCET_URL = "http://localhost:3001";
    private static final String DEFAULT_CONSUME_PATH = "/tmp";


    /**
     * Creates an NeverminedConfig object from a set of properties
     *
     * @param properties configuration values
     * @return an NeverminedConfig value with all the values set
     */
    public static NeverminedConfig getNeverminedConfig(Properties properties) {

        NeverminedConfig neverminedConfig = new NeverminedConfig();

        properties.getOrDefault(NeverminedConfig.CONSUME_BASE_PATH, DEFAULT_CONSUME_PATH);

        neverminedConfig.setKeeperUrl((String) properties.getOrDefault(NeverminedConfig.KEEPER_URL, DEFAULT_KEEPER_URL));
        neverminedConfig.setKeeperGasLimit(new BigInteger((String) properties.getOrDefault(NeverminedConfig.KEEPER_GAS_LIMIT, DEFAULT_KEEPER_GAS_LIMIT.toString())));
        neverminedConfig.setKeeperGasPrice(new BigInteger((String) properties.getOrDefault(NeverminedConfig.KEEPER_GAS_PRICE, DEFAULT_KEEPER_GAS_PRICE.toString())));
        neverminedConfig.setKeeperTxAttempts(Integer.parseInt(
                (String) properties.getOrDefault(
                        NeverminedConfig.KEEPER_TX_ATTEMPTS, String.valueOf(TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH))
        ));
        neverminedConfig.setKeeperTxSleepDuration(
                Long.parseLong(
                        (String) properties.getOrDefault(NeverminedConfig.KEEPER_TX_SLEEPDURATION, String.valueOf(TransactionManager.DEFAULT_POLLING_FREQUENCY))
                ));

        neverminedConfig.setMetadataUrl((String) properties.getOrDefault(NeverminedConfig.METADATA_URL, DEFAULT_METADATA_URL));
        neverminedConfig.setSecretStoreUrl((String) properties.getOrDefault(NeverminedConfig.SECRETSTORE_URL, DEFAULT_SECRET_STORE_URL));
        neverminedConfig.setFaucetUrl((String) properties.getOrDefault(NeverminedConfig.FAUCET_URL, DEFAULT_FAUCET_URL));
        neverminedConfig.setProviderAddress((String) properties.getOrDefault(NeverminedConfig.PROVIDER_ADDRESS, ""));
        neverminedConfig.setDidRegistryAddress((String) properties.getOrDefault(NeverminedConfig.DID_REGISTRY_ADDRESS, ""));
        neverminedConfig.setEscrowRewardConditionsAddress((String) properties.getOrDefault(NeverminedConfig.ESCROWREWARD_CONDITIONS_ADDRESS, ""));
        neverminedConfig.setEscrowAccessSecretStoreTemplateAddress((String) properties.getOrDefault(NeverminedConfig.ESCROW_ACCESS_SS_CONDITIONS_ADDRESS, ""));
        neverminedConfig.setLockrewardConditionsAddress((String) properties.getOrDefault(NeverminedConfig.LOCKREWARD_CONDITIONS_ADDRESS, ""));
        neverminedConfig.setAccessSsConditionsAddress((String) properties.getOrDefault(NeverminedConfig.ACCESS_SS_CONDITIONS_ADDRESS, ""));
        neverminedConfig.setAgreementStoreManagerAddress((String) properties.getOrDefault(NeverminedConfig.AGREEMENT_STORE_MANAGER_ADDRESS, ""));
        neverminedConfig.setConditionStoreManagerAddress((String) properties.getOrDefault(NeverminedConfig.CONDITION_STORE_MANAGER_ADDRESS, ""));
        neverminedConfig.setTokenAddress((String) properties.getOrDefault(NeverminedConfig.TOKEN_ADDRESS, ""));
        neverminedConfig.setTemplateStoreManagerAddress((String) properties.getOrDefault(NeverminedConfig.TEMPLATE_STORE_MANAGER_ADDRESS, ""));
        neverminedConfig.setDispenserAddress((String) properties.getOrDefault(NeverminedConfig.DISPENSER_ADDRESS, ""));
        neverminedConfig.setConsumeBasePath((String) properties.getOrDefault(NeverminedConfig.CONSUME_BASE_PATH, DEFAULT_CONSUME_PATH));
        neverminedConfig.setMainAccountAddress((String) properties.getOrDefault(NeverminedConfig.MAIN_ACCOUNT_ADDRESS, ""));
        neverminedConfig.setMainAccountPassword((String) properties.getOrDefault(NeverminedConfig.MAIN_ACCOUNT_PASSWORD, ""));
        neverminedConfig.setMainAccountCredentialsFile((String) properties.getOrDefault(NeverminedConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, ""));

        neverminedConfig.setEscrowComputeExecutionTemplateAddress((String) properties.getOrDefault(NeverminedConfig.ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS, ""));
        neverminedConfig.setComputeExecutionConditionAddress((String) properties.getOrDefault(NeverminedConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, ""));

        return neverminedConfig;

    }
}
