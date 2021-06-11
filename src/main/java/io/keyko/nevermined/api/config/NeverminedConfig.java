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
    public static final String FAUCET_URL = "faucet.url";
    public static final String PROVIDER_ADDRESS = "provider.address";
    public static final String MAIN_ACCOUNT_ADDRESS = "account.main.address";
    public static final String MAIN_ACCOUNT_PASSWORD = "account.main.password";
    public static final String MAIN_ACCOUNT_CREDENTIALS_FILE = "account.main.credentialsFile";
    public static final String DID_REGISTRY_ADDRESS = "contract.DIDRegistry.address";
    public static final String AGREEMENT_STORE_MANAGER_ADDRESS = "contract.AgreementStoreManager.address";
    public static final String CONDITION_STORE_MANAGER_ADDRESS = "contract.ConditionStoreManager.address";
    public static final String TEMPLATE_STORE_MANAGER_ADDRESS = "contract.TemplateStoreManager.address";
    public static final String NEVERMINED_TOKEN_ADDRESS = "contract.NeverminedToken.address";
    public static final String DISPENSER_ADDRESS = "contract.Dispenser.address";

    public static final String LOCKPAYMENT_CONDITIONS_ADDRESS = "contract.LockPaymentCondition.address";
    public static final String ACCESS_CONDITION_ADDRESS = "contract.AccessCondition.address";
    public static final String ESCROWPAYMENT_CONDITIONS_ADDRESS = "contract.EscrowPaymentCondition.address";
    public static final String COMPUTE_EXECUTION_CONDITION_ADDRESS = "contract.ComputeExecutionCondition.address";
    public static final String TRANSFER_NFT_CONDITION_ADDRESS = "contract.TransferNFTCondition.address";
    public static final String TRANSFER_DID_CONDITION_ADDRESS = "contract.TransferDIDOwnershipCondition.address";
    public static final String NFT_ACCESS_CONDITION_ADDRESS = "contract.NFTAccessCondition.address";
    public static final String NFT_HOLDER_CONDITION_ADDRESS = "contract.NFTHolderCondition.address";

    public static final String ACCESS_TEMPLATE_ADDRESS = "contract.AccessTemplate.address";
    public static final String ESCROW_COMPUTE_EXECUTION_TEMPLATE_ADDRESS = "contract.EscrowComputeExecutionTemplate.address";
    public static final String NFT_SALES_TEMPLATE_ADDRESS = "contract.NFTSalesTemplate.address";
    public static final String NFT_ACCESS_TEMPLATE_ADDRESS = "contract.NFTAccessTemplate.address";
    public static final String DID_SALES_TEMPLATE_ADDRESS = "contract.DIDSalesTemplate.address";

    public static final String CONSUME_BASE_PATH = "consume.basePath";

    private String keeperUrl;
    private BigInteger keeperGasLimit;
    private BigInteger keeperGasPrice;
    private int keeperTxAttempts;
    private long keeperTxSleepDuration;
    private String metadataUrl;
    private String secretStoreUrl;
    private String faucetUrl;
    private String providerAddress;
    private String mainAccountAddress;
    private String mainAccountPassword;
    private String mainAccountCredentialsFile;
    private String didRegistryAddress;
    private String agreementStoreManagerAddress;
    private String conditionStoreManagerAddress;
    private String escrowPaymentAddress;;
    private String lockPaymentAddress;
    private String accessConditionsAddress;
    private String transferNFTConditionAddress;
    private String transferDIDConditionAddress;
    private String nftAccessConditionAddress;
    private String nftHolderConditionAddress;

    private String tokenAddress;
    private String templateStoreManagerAddress;
    private String dispenserAddress;
    private String computeExecutionConditionAddress;

    private String accessTemplateAddress;
    private String escrowComputeExecutionTemplateAddress;
    private String nftSalesTemplateAddress;
    private String nftAccessTemplateAddress;
    private String didSalesTemplateAddress;

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

        if (neverminedConfig.getEscrowPaymentConditionConditionsAddress() == null || neverminedConfig.getEscrowPaymentConditionConditionsAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of EscrowPaymentCondition Contract must be set with the property "
                    + NeverminedConfig.ESCROWPAYMENT_CONDITIONS_ADDRESS);
        }

        if (neverminedConfig.getAccessTemplateAddress() == null || neverminedConfig.getAccessTemplateAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of AccessTemplate Contract must be set with the property "
                    + NeverminedConfig.ACCESS_TEMPLATE_ADDRESS);
        }

        if (neverminedConfig.getNFTSalesTemplateAddress() == null || neverminedConfig.getNFTSalesTemplateAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of NFTSalesTemplate Contract must be set with the property "
                    + NeverminedConfig.NFT_SALES_TEMPLATE_ADDRESS);
        }

        if (neverminedConfig.getNFTAccessTemplateAddress() == null || neverminedConfig.getNFTAccessTemplateAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of NFTAccessTemplate Contract must be set with the property "
                    + NeverminedConfig.NFT_ACCESS_TEMPLATE_ADDRESS);
        }

        if (neverminedConfig.getDIDSalesTemplateAddress() == null || neverminedConfig.getDIDSalesTemplateAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of DIDSalesTemplate Contract must be set with the property "
                    + NeverminedConfig.DID_SALES_TEMPLATE_ADDRESS);
        }

        if (neverminedConfig.getLockPaymentConditionsAddress() == null || neverminedConfig.getLockPaymentConditionsAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of LockReward Contract must be set with the property "
                    + NeverminedConfig.LOCKPAYMENT_CONDITIONS_ADDRESS);
        }

        if (neverminedConfig.getAccessConditionsAddress() == null || neverminedConfig.getAccessConditionsAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of AccessSecretsToreConditions Contract must be set with the property "
                    + NeverminedConfig.ACCESS_CONDITION_ADDRESS);
        }

        if (neverminedConfig.getTransferNFTConditionAddress() == null || neverminedConfig.getTransferNFTConditionAddress().isEmpty()) {
            validation.setValid(false);
            validation.addErrorMessage("The Address of TransferNFTCondition Contract must be set with the property "
                    + NeverminedConfig.TRANSFER_NFT_CONDITION_ADDRESS);
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
                    + NeverminedConfig.ESCROW_COMPUTE_EXECUTION_TEMPLATE_ADDRESS);
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

    public NeverminedConfig setFaucetUrl(String faucetUrl) {
        this.faucetUrl = faucetUrl;
        return this;
    }

    public String getFaucetUrl() {
        return faucetUrl;
    }

    public String getDidRegistryAddress() {
        return didRegistryAddress;
    }

    public NeverminedConfig setDidRegistryAddress(String address) {
        this.didRegistryAddress = address;
        return this;
    }

    public String getEscrowPaymentConditionConditionsAddress() {
        return escrowPaymentAddress;
    }

    public NeverminedConfig setEscrowPaymentConditionConditionsAddress(String address) {
        this.escrowPaymentAddress = address;
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

    public String getLockPaymentConditionsAddress() {
        return lockPaymentAddress;
    }

    public NeverminedConfig setLockPaymentConditionsAddress(String address) {
        this.lockPaymentAddress = address;
        return this;
    }

    public String getAccessConditionsAddress() {
        return accessConditionsAddress;
    }

    public NeverminedConfig setAccessConditionsAddress(String address) { this.accessConditionsAddress = address; return this; }

    public String getTransferNFTConditionAddress() { return transferNFTConditionAddress; }

    public String getNFTAccessConditionAddress() {
        return nftAccessConditionAddress;
    }

    public NeverminedConfig setNFTAccessConditionAddress(String address) { this.nftAccessConditionAddress = address; return this; }

    public String getNFTHolderConditionAddress() {
        return nftHolderConditionAddress;
    }

    public NeverminedConfig setNFTHolderConditionAddress(String address) { this.nftHolderConditionAddress = address; return this; }


    public NeverminedConfig setTransferNFTConditionAddress(String address) {
        this.transferNFTConditionAddress = address;
        return this;
    }

    public String getTransferDIDConditionAddress() {
        return transferDIDConditionAddress;
    }

    public NeverminedConfig setTransferDIDConditionAddress(String address) {
        this.transferDIDConditionAddress = address;
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

    public String getAccessTemplateAddress() {
        return accessTemplateAddress;
    }

    public void setAccessTemplateAddress(String escrowAccessSecretStoreTemplateAddress) {
        this.accessTemplateAddress = escrowAccessSecretStoreTemplateAddress;
    }

    public String getNFTSalesTemplateAddress() {
        return nftSalesTemplateAddress;
    }

    public void setNFTSalesTemplateAddress(String address) {
        this.nftSalesTemplateAddress = address;
    }

    public String getNFTAccessTemplateAddress() {
        return nftAccessTemplateAddress;
    }

    public void setNFTAccessTemplateAddress(String address) {
        this.nftAccessTemplateAddress = address;
    }

    public String getDIDSalesTemplateAddress() {
        return didSalesTemplateAddress;
    }

    public void setDIDSalesTemplateAddress(String address) {
        this.didSalesTemplateAddress = address;
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
