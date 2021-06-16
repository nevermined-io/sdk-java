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
        neverminedConfig.setEscrowPaymentConditionAddress((String) properties.getOrDefault(NeverminedConfig.ESCROWPAYMENT_CONDITIONS_ADDRESS, ""));
        neverminedConfig.setAccessTemplateAddress((String) properties.getOrDefault(NeverminedConfig.ACCESS_TEMPLATE_ADDRESS, ""));
        neverminedConfig.setNFTSalesTemplateAddress((String) properties.getOrDefault(NeverminedConfig.NFT_SALES_TEMPLATE_ADDRESS, ""));
        neverminedConfig.setNFTAccessTemplateAddress((String) properties.getOrDefault(NeverminedConfig.NFT_ACCESS_TEMPLATE_ADDRESS, ""));
        neverminedConfig.setDIDSalesTemplateAddress((String) properties.getOrDefault(NeverminedConfig.DID_SALES_TEMPLATE_ADDRESS, ""));
        neverminedConfig.setLockPaymentConditionsAddress((String) properties.getOrDefault(NeverminedConfig.LOCKPAYMENT_CONDITIONS_ADDRESS, ""));
        neverminedConfig.setAccessConditionsAddress((String) properties.getOrDefault(NeverminedConfig.ACCESS_CONDITION_ADDRESS, ""));
        neverminedConfig.setNFTAccessConditionAddress((String) properties.getOrDefault(NeverminedConfig.NFT_ACCESS_CONDITION_ADDRESS, ""));
        neverminedConfig.setNFTHolderConditionAddress((String) properties.getOrDefault(NeverminedConfig.NFT_HOLDER_CONDITION_ADDRESS, ""));
        neverminedConfig.setTransferNFTConditionAddress((String) properties.getOrDefault(NeverminedConfig.TRANSFER_NFT_CONDITION_ADDRESS, ""));
        neverminedConfig.setTransferDIDConditionAddress((String) properties.getOrDefault(NeverminedConfig.TRANSFER_DID_CONDITION_ADDRESS, ""));
        neverminedConfig.setAgreementStoreManagerAddress((String) properties.getOrDefault(NeverminedConfig.AGREEMENT_STORE_MANAGER_ADDRESS, ""));
        neverminedConfig.setConditionStoreManagerAddress((String) properties.getOrDefault(NeverminedConfig.CONDITION_STORE_MANAGER_ADDRESS, ""));
        neverminedConfig.setTokenAddress((String) properties.getOrDefault(NeverminedConfig.NEVERMINED_TOKEN_ADDRESS, ""));
        neverminedConfig.setTemplateStoreManagerAddress((String) properties.getOrDefault(NeverminedConfig.TEMPLATE_STORE_MANAGER_ADDRESS, ""));
        neverminedConfig.setDispenserAddress((String) properties.getOrDefault(NeverminedConfig.DISPENSER_ADDRESS, ""));
        neverminedConfig.setConsumeBasePath((String) properties.getOrDefault(NeverminedConfig.CONSUME_BASE_PATH, DEFAULT_CONSUME_PATH));
        neverminedConfig.setMainAccountAddress((String) properties.getOrDefault(NeverminedConfig.MAIN_ACCOUNT_ADDRESS, ""));
        neverminedConfig.setMainAccountPassword((String) properties.getOrDefault(NeverminedConfig.MAIN_ACCOUNT_PASSWORD, ""));
        neverminedConfig.setMainAccountCredentialsFile((String) properties.getOrDefault(NeverminedConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, ""));

        neverminedConfig.setEscrowComputeExecutionTemplateAddress((String) properties.getOrDefault(NeverminedConfig.ESCROW_COMPUTE_EXECUTION_TEMPLATE_ADDRESS, ""));
        neverminedConfig.setComputeExecutionConditionAddress((String) properties.getOrDefault(NeverminedConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, ""));

        return neverminedConfig;

    }
}
