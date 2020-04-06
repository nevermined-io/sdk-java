/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api.config;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that keeps all the configurations to initialize the API
 */
public class OceanConfig {

    public static final String KEEPER_URL = "keeper.url";
    public static final String KEEPER_GAS_LIMIT = "keeper.gasLimit";
    public static final String KEEPER_GAS_PRICE = "keeper.gasPrice";
    public static final String KEEPER_TX_ATTEMPTS = "keeper.tx.attempts";
    public static final String KEEPER_TX_SLEEPDURATION = "keeper.tx.sleepDuration";
    public static final String AQUARIUS_URL = "aquarius.url";
    public static final String SECRETSTORE_URL = "secretstore.url";
    public static final String PROVIDER_ADDRESS = "provider.address";
    public static final String MAIN_ACCOUNT_ADDRESS = "account.main.address";
    public static final String MAIN_ACCOUNT_PASSWORD = "account.main.password";
    public static final String MAIN_ACCOUNT_CREDENTIALS_FILE = "account.main.credentialsFile";
    public static final String DID_REGISTRY_ADDRESS = "contract.DIDRegistry.address";
    public static final String AGREEMENT_STORE_MANAGER_ADDRESS = "contract.AgreementStoreManager.address";
    public static final String CONDITION_STORE_MANAGER_ADDRESS = "contract.ConditionStoreManager.address";
    public static final String LOCKREWARD_CONDITIONS_ADDRESS = "contract.LockRewardCondition.address";
    public static final String ESCROWREWARD_CONDITIONS_ADDRESS = "contract.EscrowReward.address";
    public static final String ESCROW_ACCESS_SS_CONDITIONS_ADDRESS = "contract.EscrowAccessSecretStoreTemplate.address";
    public static final String TEMPLATE_STORE_MANAGER_ADDRESS = "contract.TemplateStoreManager.address";
    public static final String ACCESS_SS_CONDITIONS_ADDRESS = "contract.AccessSecretStoreCondition.address";
    public static final String TOKEN_ADDRESS = "contract.OceanToken.address";
    public static final String DISPENSER_ADDRESS = "contract.Dispenser.address";
    public static final String COMPUTE_EXECUTION_CONDITION_ADDRESS = "contract.ComputeExecutionCondition.address";
    public static final String ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS = "contract.EscrowComputeExecutionTemplate.address";
    public static final String CONSUME_BASE_PATH = "consume.basePath";

    private String keeperUrl;
    private BigInteger keeperGasLimit;
    private BigInteger keeperGasPrice;
    private int keeperTxAttempts;
    private long keeperTxSleepDuration;
    private String aquariusUrl;
    private String secretStoreUrl;
    private String providerAddress;
    private String mainAccountAddress;
    private String mainAccountPassword;
    private String mainAccountCredentialsFile;
    private String didRegistryAddress;
    private String agreementStoreManagerAddress;
    private String conditionStoreManagerAddress;
    private String escrowRewardAddress;
    private String escrowAccessSecretStoreTemplateAddress;
    private String lockRewardAddress;
    private String accessSsConditionsAddress;
    private String tokenAddress;
    private String templateStoreManagerAddress;
    private String dispenserAddress;
    private String computeExecutionConditionAddress;
    private String escrowComputeExecutionTemplateAddress;
    private String consumeBasePath;

    /**
     * Class to hold the result of a Configuration's validation
     */
    public static class OceanConfigValidation {

        private Boolean valid = true;
        private List<String> errors = new ArrayList<>();

        public Boolean isValid() {
            return valid;
        }

        public void setValid(Boolean valid) {
            this.valid = valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void addErrorMessage(String error) {
            errors.add(error);
        }

        public String errorsToString() {
            return String.join("; ", this.errors);
        }

    }


    /**
     * Validates that all the needed properties are set in the configuration
     *
     * @param oceanConfig the configuration
     * @return an OceanConfigValidation object that indicates if the configuration is valid
     */
    public static OceanConfigValidation validate(OceanConfig oceanConfig) {

        OceanConfigValidation validation = new OceanConfigValidation();

        if (oceanConfig.getDidRegistryAddress() == null || oceanConfig.getDidRegistryAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of DIDRegistry Contract must be set with the property "
                    + OceanConfig.DID_REGISTRY_ADDRESS);
        }


        if (oceanConfig.getAgreementStoreManagerAddress() == null || oceanConfig.getAgreementStoreManagerAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of agreementStoreManager Contract must be set with the property "
                    + OceanConfig.AGREEMENT_STORE_MANAGER_ADDRESS);
        }
        if (oceanConfig.getConditionStoreManagerAddress() == null || oceanConfig.getConditionStoreManagerAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of conditionStoreManager Contract must be set with the property "
                    + OceanConfig.CONDITION_STORE_MANAGER_ADDRESS);
        }

        if (oceanConfig.getEscrowRewardConditionsAddress() == null || oceanConfig.getEscrowRewardConditionsAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of EscrowReward Contract must be set with the property "
                    + OceanConfig.ESCROWREWARD_CONDITIONS_ADDRESS);
        }

        if (oceanConfig.getEscrowAccessSecretStoreTemplateAddress() == null || oceanConfig.getEscrowAccessSecretStoreTemplateAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of EscrowAccessSecretStoreTemplate Contract must be set with the property "
                    + OceanConfig.ESCROW_ACCESS_SS_CONDITIONS_ADDRESS);
        }

        if (oceanConfig.getLockrewardConditionsAddress() == null || oceanConfig.getLockrewardConditionsAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of LockReward Contract must be set with the property "
                    + OceanConfig.LOCKREWARD_CONDITIONS_ADDRESS);
        }

        if (oceanConfig.getAccessSsConditionsAddress() == null || oceanConfig.getAccessSsConditionsAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of AccessSecretsToreConditions Contract must be set with the property "
                    + OceanConfig.ACCESS_SS_CONDITIONS_ADDRESS);
        }

        if (oceanConfig.getTemplateStoreManagerAddress() == null || oceanConfig.getTemplateStoreManagerAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of TemplateStoreManager Contract must be set with the property "
                    + OceanConfig.TEMPLATE_STORE_MANAGER_ADDRESS);
        }

        if (oceanConfig.getComputeExecutionConditionAddress() == null || oceanConfig.getComputeExecutionConditionAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of ComputeExecutionCondition Contract must be set with the property "
                    + OceanConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS);
        }

        if (oceanConfig.getEscrowComputeExecutionTemplateAddress()== null || oceanConfig.getEscrowComputeExecutionTemplateAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of EscrowComputeExecutionTemplate Contract must be set with the property "
                    + OceanConfig.ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS);
        }

        if (oceanConfig.getMainAccountAddress() == null || oceanConfig.getMainAccountAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of the Main Account must be set with the property "
                    + OceanConfig.MAIN_ACCOUNT_ADDRESS);
        }

        if (oceanConfig.getMainAccountPassword() == null) {
            validation.setValid(false);
            validation.addErrorMessage("The Password of the Main Account must be set with the property "
                    + OceanConfig.MAIN_ACCOUNT_PASSWORD);
        }

        if (oceanConfig.getMainAccountCredentialsFile() == null) {
            validation.setValid(false);
            validation.addErrorMessage("The Credentials File of the Main Account must be set with the property "
                    + OceanConfig.MAIN_ACCOUNT_CREDENTIALS_FILE);
        }

        if (oceanConfig.getProviderAddress() == null || oceanConfig.getProviderAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of the Provider must be set with the property "
                    + OceanConfig.PROVIDER_ADDRESS);
        }

        return validation;
    }

    public String getKeeperUrl() {
        return keeperUrl;
    }

    public OceanConfig setKeeperUrl(String keeperUrl) {
        this.keeperUrl = keeperUrl;
        return this;
    }

    public BigInteger getKeeperGasLimit() {
        return keeperGasLimit;
    }

    public OceanConfig setKeeperGasLimit(BigInteger keeperGasLimit) {
        this.keeperGasLimit = keeperGasLimit;
        return this;
    }

    public BigInteger getKeeperGasPrice() {
        return keeperGasPrice;
    }

    public OceanConfig setKeeperGasPrice(BigInteger keeperGasPrice) {
        this.keeperGasPrice = keeperGasPrice;
        return this;
    }

    public int getKeeperTxAttempts() {
        return keeperTxAttempts;
    }

    public OceanConfig setKeeperTxAttempts(int keeperTxAttempts) {
        this.keeperTxAttempts = keeperTxAttempts;
        return this;
    }

    public long getKeeperTxSleepDuration() {
        return keeperTxSleepDuration;
    }

    public OceanConfig setKeeperTxSleepDuration(long keeperTxSleepDuration) {
        this.keeperTxSleepDuration = keeperTxSleepDuration;
        return this;
    }

    public String getAquariusUrl() {
        return aquariusUrl;
    }

    public OceanConfig setAquariusUrl(String address) {
        this.aquariusUrl = address;
        return this;
    }

    public String getSecretStoreUrl() {
        return secretStoreUrl;
    }

    public OceanConfig setSecretStoreUrl(String secretStoreUrl) {
        this.secretStoreUrl = secretStoreUrl;
        return this;
    }

    public String getDidRegistryAddress() {
        return didRegistryAddress;
    }

    public OceanConfig setDidRegistryAddress(String address) {
        this.didRegistryAddress = address;
        return this;
    }

    public String getEscrowRewardConditionsAddress() {
        return escrowRewardAddress;
    }

    public OceanConfig setEscrowRewardConditionsAddress(String address) {
        this.escrowRewardAddress = address;
        return this;
    }

    public String getAgreementStoreManagerAddress() {
        return agreementStoreManagerAddress;
    }

    public OceanConfig setAgreementStoreManagerAddress(String address) {
        this.agreementStoreManagerAddress = address;
        return this;
    }

    public String getConditionStoreManagerAddress() {
        return conditionStoreManagerAddress;
    }

    public OceanConfig setConditionStoreManagerAddress(String address) {
        this.conditionStoreManagerAddress = address;
        return this;
    }

    public String getLockrewardConditionsAddress() {
        return lockRewardAddress;
    }

    public OceanConfig setLockrewardConditionsAddress(String address) {
        this.lockRewardAddress = address;
        return this;
    }

    public String getAccessSsConditionsAddress() {
        return accessSsConditionsAddress;
    }

    public OceanConfig setAccessSsConditionsAddress(String address) {
        this.accessSsConditionsAddress = address;
        return this;
    }

    public String getConsumeBasePath() {
        return consumeBasePath;
    }

    public OceanConfig setConsumeBasePath(String consumeBasePath) {
        this.consumeBasePath = consumeBasePath;
        return this;
    }

    public String getMainAccountAddress() {
        return mainAccountAddress;
    }

    public OceanConfig setMainAccountAddress(String mainAccountAddress) {
        this.mainAccountAddress = mainAccountAddress;
        return this;
    }

    public String getMainAccountPassword() {
        return mainAccountPassword;
    }

    public OceanConfig setMainAccountPassword(String mainAccountPassword) {
        this.mainAccountPassword = mainAccountPassword;
        return this;
    }

    public String getMainAccountCredentialsFile() {
        return mainAccountCredentialsFile;
    }

    public OceanConfig setMainAccountCredentialsFile(String mainAccountCredentialsFile) {
        this.mainAccountCredentialsFile = mainAccountCredentialsFile;
        return this;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public OceanConfig setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
        return this;
    }

    public String getTemplateStoreManagerAddress() {
        return templateStoreManagerAddress;
    }

    public OceanConfig setTemplateStoreManagerAddress(String templateStoreManagerAddress) {
        this.templateStoreManagerAddress = templateStoreManagerAddress;
        return this;
    }


    public String getDispenserAddress() {
        return dispenserAddress;
    }

    public OceanConfig setDispenserAddress(String dispenserAddress) {
        this.dispenserAddress = dispenserAddress;
        return this;
    }

    public String getEscrowAccessSecretStoreTemplateAddress() {
        return escrowAccessSecretStoreTemplateAddress;
    }

    public void setEscrowAccessSecretStoreTemplateAddress(String escrowAccessSecretStoreTemplateAddress) {
        this.escrowAccessSecretStoreTemplateAddress = escrowAccessSecretStoreTemplateAddress;
    }

    public String getProviderAddress() {
        return providerAddress;
    }

    public void setProviderAddress(String providerAddress) {
        this.providerAddress = providerAddress;
    }

    public String getComputeExecutionConditionAddress() {
        return computeExecutionConditionAddress;
    }

    public void setComputeExecutionConditionAddress(String computeExecutionConditionAddress) {
        this.computeExecutionConditionAddress = computeExecutionConditionAddress;
    }

    public String getEscrowComputeExecutionTemplateAddress() {
        return escrowComputeExecutionTemplateAddress;
    }

    public void setEscrowComputeExecutionTemplateAddress(String escrowComputeExecutionTemplate) {
        this.escrowComputeExecutionTemplateAddress = escrowComputeExecutionTemplate;
    }
}
