package io.keyko.nevermined.api.config;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that keeps all the configurations to initialize the API
 */
public class NeverminedConfig {

    public static final String KEEPER_URL = "keeper.url";
    public static final String KEEPER_GAS_LIMIT = "keeper.gasLimit";
    public static final String KEEPER_GAS_PRICE = "keeper.gasPrice";
    public static final String KEEPER_TX_ATTEMPTS = "keeper.tx.attempts";
    public static final String KEEPER_TX_SLEEPDURATION = "keeper.tx.sleepDuration";
    public static final String METADATA_URL = "metadata.url";
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
    private String metadataUrl;
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
    public static class NeverminedConfigValidation {

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
     * @param neverminedConfig the configuration
     * @return an NeverminedConfigValidation object that indicates if the configuration is valid
     */
    public static NeverminedConfigValidation validate(NeverminedConfig neverminedConfig) {

        NeverminedConfigValidation validation = new NeverminedConfigValidation();

        if (neverminedConfig.getDidRegistryAddress() == null || neverminedConfig.getDidRegistryAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of DIDRegistry Contract must be set with the property "
                    + NeverminedConfig.DID_REGISTRY_ADDRESS);
        }


        if (neverminedConfig.getAgreementStoreManagerAddress() == null || neverminedConfig.getAgreementStoreManagerAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of agreementStoreManager Contract must be set with the property "
                    + NeverminedConfig.AGREEMENT_STORE_MANAGER_ADDRESS);
        }
        if (neverminedConfig.getConditionStoreManagerAddress() == null || neverminedConfig.getConditionStoreManagerAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of conditionStoreManager Contract must be set with the property "
                    + NeverminedConfig.CONDITION_STORE_MANAGER_ADDRESS);
        }

        if (neverminedConfig.getEscrowRewardConditionsAddress() == null || neverminedConfig.getEscrowRewardConditionsAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of EscrowReward Contract must be set with the property "
                    + NeverminedConfig.ESCROWREWARD_CONDITIONS_ADDRESS);
        }

        if (neverminedConfig.getEscrowAccessSecretStoreTemplateAddress() == null || neverminedConfig.getEscrowAccessSecretStoreTemplateAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of EscrowAccessSecretStoreTemplate Contract must be set with the property "
                    + NeverminedConfig.ESCROW_ACCESS_SS_CONDITIONS_ADDRESS);
        }

        if (neverminedConfig.getLockrewardConditionsAddress() == null || neverminedConfig.getLockrewardConditionsAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of LockReward Contract must be set with the property "
                    + NeverminedConfig.LOCKREWARD_CONDITIONS_ADDRESS);
        }

        if (neverminedConfig.getAccessSsConditionsAddress() == null || neverminedConfig.getAccessSsConditionsAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of AccessSecretsToreConditions Contract must be set with the property "
                    + NeverminedConfig.ACCESS_SS_CONDITIONS_ADDRESS);
        }

        if (neverminedConfig.getTemplateStoreManagerAddress() == null || neverminedConfig.getTemplateStoreManagerAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of TemplateStoreManager Contract must be set with the property "
                    + NeverminedConfig.TEMPLATE_STORE_MANAGER_ADDRESS);
        }

        if (neverminedConfig.getComputeExecutionConditionAddress() == null || neverminedConfig.getComputeExecutionConditionAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of ComputeExecutionCondition Contract must be set with the property "
                    + NeverminedConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS);
        }

        if (neverminedConfig.getEscrowComputeExecutionTemplateAddress()== null || neverminedConfig.getEscrowComputeExecutionTemplateAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of EscrowComputeExecutionTemplate Contract must be set with the property "
                    + NeverminedConfig.ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS);
        }

        if (neverminedConfig.getMainAccountAddress() == null || neverminedConfig.getMainAccountAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of the Main Account must be set with the property "
                    + NeverminedConfig.MAIN_ACCOUNT_ADDRESS);
        }

        if (neverminedConfig.getMainAccountPassword() == null) {
            validation.setValid(false);
            validation.addErrorMessage("The Password of the Main Account must be set with the property "
                    + NeverminedConfig.MAIN_ACCOUNT_PASSWORD);
        }

        if (neverminedConfig.getMainAccountCredentialsFile() == null) {
            validation.setValid(false);
            validation.addErrorMessage("The Credentials File of the Main Account must be set with the property "
                    + NeverminedConfig.MAIN_ACCOUNT_CREDENTIALS_FILE);
        }

        if (neverminedConfig.getProviderAddress() == null || neverminedConfig.getProviderAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of the Provider must be set with the property "
                    + NeverminedConfig.PROVIDER_ADDRESS);
        }

        return validation;
    }

    public String getKeeperUrl() {
        return keeperUrl;
    }

    public NeverminedConfig setKeeperUrl(String keeperUrl) {
        this.keeperUrl = keeperUrl;
        return this;
    }

    public BigInteger getKeeperGasLimit() {
        return keeperGasLimit;
    }

    public NeverminedConfig setKeeperGasLimit(BigInteger keeperGasLimit) {
        this.keeperGasLimit = keeperGasLimit;
        return this;
    }

    public BigInteger getKeeperGasPrice() {
        return keeperGasPrice;
    }

    public NeverminedConfig setKeeperGasPrice(BigInteger keeperGasPrice) {
        this.keeperGasPrice = keeperGasPrice;
        return this;
    }

    public int getKeeperTxAttempts() {
        return keeperTxAttempts;
    }

    public NeverminedConfig setKeeperTxAttempts(int keeperTxAttempts) {
        this.keeperTxAttempts = keeperTxAttempts;
        return this;
    }

    public long getKeeperTxSleepDuration() {
        return keeperTxSleepDuration;
    }

    public NeverminedConfig setKeeperTxSleepDuration(long keeperTxSleepDuration) {
        this.keeperTxSleepDuration = keeperTxSleepDuration;
        return this;
    }

    public String getMetadataUrl() {
        return metadataUrl;
    }

    public NeverminedConfig setMetadataUrl(String address) {
        this.metadataUrl = address;
        return this;
    }

    public String getSecretStoreUrl() {
        return secretStoreUrl;
    }

    public NeverminedConfig setSecretStoreUrl(String secretStoreUrl) {
        this.secretStoreUrl = secretStoreUrl;
        return this;
    }

    public String getDidRegistryAddress() {
        return didRegistryAddress;
    }

    public NeverminedConfig setDidRegistryAddress(String address) {
        this.didRegistryAddress = address;
        return this;
    }

    public String getEscrowRewardConditionsAddress() {
        return escrowRewardAddress;
    }

    public NeverminedConfig setEscrowRewardConditionsAddress(String address) {
        this.escrowRewardAddress = address;
        return this;
    }

    public String getAgreementStoreManagerAddress() {
        return agreementStoreManagerAddress;
    }

    public NeverminedConfig setAgreementStoreManagerAddress(String address) {
        this.agreementStoreManagerAddress = address;
        return this;
    }

    public String getConditionStoreManagerAddress() {
        return conditionStoreManagerAddress;
    }

    public NeverminedConfig setConditionStoreManagerAddress(String address) {
        this.conditionStoreManagerAddress = address;
        return this;
    }

    public String getLockrewardConditionsAddress() {
        return lockRewardAddress;
    }

    public NeverminedConfig setLockrewardConditionsAddress(String address) {
        this.lockRewardAddress = address;
        return this;
    }

    public String getAccessSsConditionsAddress() {
        return accessSsConditionsAddress;
    }

    public NeverminedConfig setAccessSsConditionsAddress(String address) {
        this.accessSsConditionsAddress = address;
        return this;
    }

    public String getConsumeBasePath() {
        return consumeBasePath;
    }

    public NeverminedConfig setConsumeBasePath(String consumeBasePath) {
        this.consumeBasePath = consumeBasePath;
        return this;
    }

    public String getMainAccountAddress() {
        return mainAccountAddress;
    }

    public NeverminedConfig setMainAccountAddress(String mainAccountAddress) {
        this.mainAccountAddress = mainAccountAddress;
        return this;
    }

    public String getMainAccountPassword() {
        return mainAccountPassword;
    }

    public NeverminedConfig setMainAccountPassword(String mainAccountPassword) {
        this.mainAccountPassword = mainAccountPassword;
        return this;
    }

    public String getMainAccountCredentialsFile() {
        return mainAccountCredentialsFile;
    }

    public NeverminedConfig setMainAccountCredentialsFile(String mainAccountCredentialsFile) {
        this.mainAccountCredentialsFile = mainAccountCredentialsFile;
        return this;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public NeverminedConfig setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
        return this;
    }

    public String getTemplateStoreManagerAddress() {
        return templateStoreManagerAddress;
    }

    public NeverminedConfig setTemplateStoreManagerAddress(String templateStoreManagerAddress) {
        this.templateStoreManagerAddress = templateStoreManagerAddress;
        return this;
    }


    public String getDispenserAddress() {
        return dispenserAddress;
    }

    public NeverminedConfig setDispenserAddress(String dispenserAddress) {
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
