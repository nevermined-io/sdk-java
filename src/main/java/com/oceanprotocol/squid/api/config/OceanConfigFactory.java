/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api.config;


import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.Properties;


/**
 * Factory to get instances of OceanConfig
 */
public class OceanConfigFactory {


    private static final String DEFAULT_KEEPER_URL = "http://localhost:8545";
    private static final BigInteger DEFAULT_KEEPER_GAS_LIMIT = BigInteger.valueOf(4712388l);
    private static final BigInteger DEFAULT_KEEPER_GAS_PRICE = BigInteger.valueOf(100000000000l);
    private static final String DEFAULT_AQUARIUS_URL = "http://localhost:5000";
    private static final String DEFAULT_SECRET_STORE_URL = "http://localhost:12001";
    private static final String DEFAULT_CONSUME_PATH = "/tmp";


    /**
     * Creates an OceanConfig object from a set of properties
     *
     * @param properties configuration values
     * @return an OceanConfig value with all the values set
     */
    public static OceanConfig getOceanConfig(Properties properties) {

        OceanConfig oceanConfig = new OceanConfig();

        properties.getOrDefault(OceanConfig.CONSUME_BASE_PATH, DEFAULT_CONSUME_PATH);

        oceanConfig.setKeeperUrl((String) properties.getOrDefault(OceanConfig.KEEPER_URL, DEFAULT_KEEPER_URL));
        oceanConfig.setKeeperGasLimit(new BigInteger((String) properties.getOrDefault(OceanConfig.KEEPER_GAS_LIMIT, DEFAULT_KEEPER_GAS_LIMIT.toString())));
        oceanConfig.setKeeperGasPrice(new BigInteger((String) properties.getOrDefault(OceanConfig.KEEPER_GAS_PRICE, DEFAULT_KEEPER_GAS_PRICE.toString())));
        oceanConfig.setKeeperTxAttempts(Integer.parseInt(
                (String) properties.getOrDefault(
                        OceanConfig.KEEPER_TX_ATTEMPTS, String.valueOf(TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH))
        ));
        oceanConfig.setKeeperTxSleepDuration(
                Long.parseLong(
                        (String) properties.getOrDefault(OceanConfig.KEEPER_TX_SLEEPDURATION, String.valueOf(TransactionManager.DEFAULT_POLLING_FREQUENCY))
                ));

        oceanConfig.setAquariusUrl((String) properties.getOrDefault(OceanConfig.AQUARIUS_URL, DEFAULT_AQUARIUS_URL));
        oceanConfig.setSecretStoreUrl((String) properties.getOrDefault(OceanConfig.SECRETSTORE_URL, DEFAULT_SECRET_STORE_URL));
        oceanConfig.setProviderAddress((String) properties.getOrDefault(OceanConfig.PROVIDER_ADDRESS, ""));
        oceanConfig.setDidRegistryAddress((String) properties.getOrDefault(OceanConfig.DID_REGISTRY_ADDRESS, ""));
        oceanConfig.setEscrowRewardConditionsAddress((String) properties.getOrDefault(OceanConfig.ESCROWREWARD_CONDITIONS_ADDRESS, ""));
        oceanConfig.setEscrowAccessSecretStoreTemplateAddress((String) properties.getOrDefault(OceanConfig.ESCROW_ACCESS_SS_CONDITIONS_ADDRESS, ""));
        oceanConfig.setLockrewardConditionsAddress((String) properties.getOrDefault(OceanConfig.LOCKREWARD_CONDITIONS_ADDRESS, ""));
        oceanConfig.setAccessSsConditionsAddress((String) properties.getOrDefault(OceanConfig.ACCESS_SS_CONDITIONS_ADDRESS, ""));
        oceanConfig.setAgreementStoreManagerAddress((String) properties.getOrDefault(OceanConfig.AGREEMENT_STORE_MANAGER_ADDRESS, ""));
        oceanConfig.setConditionStoreManagerAddress((String) properties.getOrDefault(OceanConfig.CONDITION_STORE_MANAGER_ADDRESS, ""));
        oceanConfig.setTokenAddress((String) properties.getOrDefault(OceanConfig.TOKEN_ADDRESS, ""));
        oceanConfig.setTemplateStoreManagerAddress((String) properties.getOrDefault(OceanConfig.TEMPLATE_STORE_MANAGER_ADDRESS, ""));
        oceanConfig.setDispenserAddress((String) properties.getOrDefault(OceanConfig.DISPENSER_ADDRESS, ""));
        oceanConfig.setConsumeBasePath((String) properties.getOrDefault(OceanConfig.CONSUME_BASE_PATH, DEFAULT_CONSUME_PATH));
        oceanConfig.setMainAccountAddress((String) properties.getOrDefault(OceanConfig.MAIN_ACCOUNT_ADDRESS, ""));
        oceanConfig.setMainAccountPassword((String) properties.getOrDefault(OceanConfig.MAIN_ACCOUNT_PASSWORD, ""));
        oceanConfig.setMainAccountCredentialsFile((String) properties.getOrDefault(OceanConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, ""));

        oceanConfig.setEscrowComputeExecutionTemplateAddress((String) properties.getOrDefault(OceanConfig.ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS, ""));
        oceanConfig.setComputeExecutionConditionAddress((String) properties.getOrDefault(OceanConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, ""));

        return oceanConfig;

    }
}
