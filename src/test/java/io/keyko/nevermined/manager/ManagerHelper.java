package io.keyko.nevermined.manager;

import io.keyko.nevermined.api.NeverminedAPI;
import io.keyko.nevermined.core.conditions.LockPaymentConditionPayable;
import io.keyko.nevermined.exceptions.InitializationException;
import io.keyko.nevermined.exceptions.InvalidConfiguration;
import io.keyko.secretstore.core.EvmDto;
import io.keyko.secretstore.core.SecretStoreDto;
import com.typesafe.config.Config;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.api.config.NeverminedConfig;
import io.keyko.nevermined.contracts.*;
import io.keyko.nevermined.external.MetadataApiService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;

public abstract class ManagerHelper {

    private static final Logger log = LogManager.getLogger(ManagerHelper.class);

    public enum VmClient { ganache, parity, main}

    public static KeeperService getKeeper(Config config) throws IOException, CipherException {
        return getKeeper(config, VmClient.ganache);
    }

    public static KeeperService getKeeper(String url, String address, String password, String file, BigInteger gasLimit, BigInteger gasPrice, int attempts, long sleepDuration) throws IOException, CipherException {
        KeeperService keeper= KeeperService.getInstance(url, address, password, file, attempts, sleepDuration);

        keeper.setGasLimit(gasLimit)
                .setGasPrice(gasPrice);

        return keeper;
    }

    public static KeeperService getKeeper(Config config, VmClient client) throws IOException, CipherException {

         return getKeeper(
                config.getString("keeper.url"),
                config.getString("account." + client.toString() + ".address"),
                config.getString("account." + client.toString() + ".password"),
                config.getString("account." + client.toString() + ".credentialsFile"),
                BigInteger.valueOf(config.getLong("keeper.gasLimit")),
                BigInteger.valueOf(config.getLong("keeper.gasPrice")),
                config.getInt("keeper.tx.attempts"),
                config.getLong("keeper.tx.sleepDuration")
         );


    }


    public static KeeperService getKeeper(Config config, VmClient client, String nAddress) throws IOException, CipherException {
        KeeperService keeper= KeeperService.getInstance(
                config.getString("keeper.url"),
                config.getString("account." + client.toString() + ".address" + nAddress),
                config.getString("account." + client.toString() + ".password" + nAddress),
                config.getString("account." + client.toString() + ".credentialsFile" + nAddress),
                config.getInt("keeper.tx.attempts"),
                config.getLong("keeper.tx.sleepDuration")
        );

        keeper.setGasLimit(BigInteger.valueOf(config.getLong("keeper.gasLimit")))
                .setGasPrice(BigInteger.valueOf(config.getLong("keeper.gasPrice")));

        return keeper;
    }

    public static NeverminedAPI getNeverminedAPI(Config config, VmClient client, String nAddress) throws InvalidConfiguration, InitializationException {

        Properties properties = new Properties();
        properties.put(NeverminedConfig.MAIN_ACCOUNT_ADDRESS, config.getString("account." + client.toString() + ".address" + nAddress));
        properties.put(NeverminedConfig.MAIN_ACCOUNT_PASSWORD, config.getString("account." + client.toString() + ".password" + nAddress));
        properties.put(NeverminedConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, config.getString("account." + client.toString() + ".credentialsFile" + nAddress));

        properties.put(NeverminedConfig.KEEPER_URL, config.getString("keeper.url"));
        properties.put(NeverminedConfig.KEEPER_GAS_LIMIT, config.getString("keeper.gasLimit"));
        properties.put(NeverminedConfig.KEEPER_GAS_PRICE, config.getString("keeper.gasPrice"));
        properties.put(NeverminedConfig.KEEPER_TX_ATTEMPTS, config.getString("keeper.tx.attempts"));
        properties.put(NeverminedConfig.KEEPER_TX_SLEEPDURATION, config.getString("keeper.tx.sleepDuration"));
        properties.put(NeverminedConfig.METADATA_URL, config.getString("metadata.url"));
        properties.put(NeverminedConfig.SECRETSTORE_URL, config.getString("secretstore.url"));
        properties.put(NeverminedConfig.CONSUME_BASE_PATH, config.getString("consume.basePath"));
        properties.put(NeverminedConfig.PROVIDER_ADDRESS, config.getString("provider.address"));

        properties.put(NeverminedConfig.DID_REGISTRY_ADDRESS, config.getString("contract.DIDRegistry.address"));
        properties.put(NeverminedConfig.AGREEMENT_STORE_MANAGER_ADDRESS, config.getString("contract.AgreementStoreManager.address"));
        properties.put(NeverminedConfig.CONDITION_STORE_MANAGER_ADDRESS, config.getString("contract.ConditionStoreManager.address"));
        properties.put(NeverminedConfig.TEMPLATE_STORE_MANAGER_ADDRESS, config.getString("contract.TemplateStoreManager.address"));
        properties.put(NeverminedConfig.NEVERMINED_TOKEN_ADDRESS, config.getString("contract.NeverminedToken.address"));
        properties.put(NeverminedConfig.DISPENSER_ADDRESS, config.getString("contract.Dispenser.address"));

        properties.put(NeverminedConfig.ACCESS_TEMPLATE_ADDRESS, config.getString("contract.AccessTemplate.address"));
        properties.put(NeverminedConfig.ESCROW_COMPUTE_EXECUTION_TEMPLATE_ADDRESS, config.getString("contract.EscrowComputeExecutionTemplate.address"));
        properties.put(NeverminedConfig.DID_SALES_TEMPLATE_ADDRESS, config.getString("contract.DIDSalesTemplate.address"));
        properties.put(NeverminedConfig.NFT_SALES_TEMPLATE_ADDRESS, config.getString("contract.NFTSalesTemplate.address"));
        properties.put(NeverminedConfig.NFT_ACCESS_TEMPLATE_ADDRESS, config.getString("contract.NFTAccessTemplate.address"));

        properties.put(NeverminedConfig.LOCKPAYMENT_CONDITIONS_ADDRESS, config.getString("contract.LockPaymentCondition.address"));
        properties.put(NeverminedConfig.ESCROWPAYMENT_CONDITIONS_ADDRESS, config.getString("contract.EscrowPaymentCondition.address"));
        properties.put(NeverminedConfig.ACCESS_CONDITION_ADDRESS, config.getString("contract.AccessCondition.address"));
        properties.put(NeverminedConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.ComputeExecutionCondition.address"));
        properties.put(NeverminedConfig.TRANSFER_NFT_CONDITION_ADDRESS, config.getString("contract.TransferNFTCondition.address"));
        properties.put(NeverminedConfig.TRANSFER_DID_CONDITION_ADDRESS, config.getString("contract.TransferDIDOwnershipCondition.address"));
        properties.put(NeverminedConfig.NFT_ACCESS_CONDITION_ADDRESS, config.getString("contract.NFTAccessCondition.address"));
        properties.put(NeverminedConfig.NFT_HOLDER_CONDITION_ADDRESS, config.getString("contract.NFTHolderCondition.address"));

        return NeverminedAPI.getInstance(properties);

    }

    public static MetadataApiService getMetadataService(Config config) {
        return MetadataApiService.getInstance(config.getString("metadata.url"));
    }

    public static SecretStoreDto getSecretStoreDto(Config config) {
        return SecretStoreDto.builder(config.getString("secretstore.url"));
    }

    public static EvmDto getEvmDto(Config config, VmClient client) {
        return EvmDto.builder(
                config.getString("keeper.url"),
                config.getString("account." + client.toString() + ".address"),
                config.getString("account." + client.toString() + ".password")
        );
    }

    public static SecretStoreManager getSecretStoreController(Config config, VmClient client) {
        return SecretStoreManager.getInstance(getSecretStoreDto(config),getEvmDto(config, client));
    }

    public static SecretStoreManager getSecretStoreController(Config config, EvmDto evmDto) {
        return SecretStoreManager.getInstance(getSecretStoreDto(config), evmDto);
    }

    /**
     * Returns a Properties object with the entries necessary to run the integration tests
     * @param config
     * @return
     */
    public static Properties getDefaultProperties(Config config, String numAddress)    {
        Properties properties = new Properties();
        properties.put(NeverminedConfig.KEEPER_URL, config.getString("keeper.url"));
        properties.put(NeverminedConfig.KEEPER_GAS_LIMIT, config.getString("keeper.gasLimit"));
        properties.put(NeverminedConfig.KEEPER_GAS_PRICE, config.getString("keeper.gasPrice"));
        properties.put(NeverminedConfig.KEEPER_TX_ATTEMPTS, config.getString("keeper.tx.attempts"));
        properties.put(NeverminedConfig.KEEPER_TX_SLEEPDURATION, config.getString("keeper.tx.sleepDuration"));
        properties.put(NeverminedConfig.METADATA_URL, config.getString("metadata.url"));
        properties.put(NeverminedConfig.SECRETSTORE_URL, config.getString("secretstore.url"));
        properties.put(NeverminedConfig.CONSUME_BASE_PATH, config.getString("consume.basePath"));
        properties.put(NeverminedConfig.MAIN_ACCOUNT_ADDRESS, config.getString("account.parity.address" + numAddress));
        properties.put(NeverminedConfig.MAIN_ACCOUNT_PASSWORD, config.getString("account.parity.password" + numAddress));
        properties.put(NeverminedConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, config.getString("account.parity.credentialsFile" + numAddress));
        properties.put(NeverminedConfig.DID_REGISTRY_ADDRESS, config.getString("contract.DIDRegistry.address"));
        properties.put(NeverminedConfig.AGREEMENT_STORE_MANAGER_ADDRESS, config.getString("contract.AgreementStoreManager.address"));
        properties.put(NeverminedConfig.CONDITION_STORE_MANAGER_ADDRESS, config.getString("contract.ConditionStoreManager.address"));
        properties.put(NeverminedConfig.LOCKPAYMENT_CONDITIONS_ADDRESS, config.getString("contract.LockPaymentCondition.address"));
        properties.put(NeverminedConfig.ESCROWPAYMENT_CONDITIONS_ADDRESS, config.getString("contract.EscrowPaymentCondition.address"));
        properties.put(NeverminedConfig.ACCESS_CONDITION_ADDRESS, config.getString("contract.AccessCondition.address"));
        properties.put(NeverminedConfig.TEMPLATE_STORE_MANAGER_ADDRESS, config.getString("contract.TemplateStoreManager.address"));
        properties.put(NeverminedConfig.NEVERMINED_TOKEN_ADDRESS, config.getString("contract.NeverminedToken.address"));
        properties.put(NeverminedConfig.DISPENSER_ADDRESS, config.getString("contract.Dispenser.address"));
        properties.put(NeverminedConfig.PROVIDER_ADDRESS, config.getString("provider.address"));
        properties.put(NeverminedConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.ComputeExecutionCondition.address"));
        return properties;
    }

    public static NeverminedToken loadNeverminedTokenContract(KeeperService keeper, String address) {
        return NeverminedToken.load(
                address,
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider());
    }


    public static Dispenser loadDispenserContract(KeeperService keeper, String address) {

        return Dispenser.load(
                address,
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }


    public static DIDRegistry loadDIDRegistryContract(KeeperService keeper, String address) {

        return DIDRegistry.load(
                address,
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }


    public static AccessTemplate loadAccessTemplate(KeeperService keeper, String address) throws Exception, IOException, CipherException {
        return AccessTemplate.load(
                address,
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider());
    }

    public static EscrowPaymentCondition loadEscrowPaymentConditionContract(KeeperService keeper, String address) {
        return EscrowPaymentCondition.load(
                address,
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    public static LockPaymentConditionPayable loadLockPaymentCondition(KeeperService keeper, String address) {
        return LockPaymentConditionPayable.load(address,
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    public static AccessCondition loadAccessConditionContract(KeeperService keeper, String address) {
        return AccessCondition.load(address,
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
                );
    }

    public static TemplateStoreManager loadTemplateStoreManager(KeeperService keeper, String address) {
        return TemplateStoreManager.load(address,
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    public static AgreementStoreManager loadAgreementStoreManager(KeeperService keeper, String address) {
        return AgreementStoreManager.load(address,
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    public static ConditionStoreManager loadConditionStoreManager(KeeperService keeper, String address) {
        return ConditionStoreManager.load(address,
                keeper.getWeb3(),
//                keeper.getCredentials(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }


    public static TemplateStoreManager deployTemplateStoreManager(KeeperService keeper) throws Exception {
        log.debug("Deploying TemplateStoreManager with address: " + keeper.getCredentials().getAddress());
        return TemplateStoreManager.deploy(
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider())
                .send();
    }

    public static AccessTemplate deployAccessTemplate(KeeperService keeper) throws Exception {
        log.debug("Deploying AccessTemplate with address: " + keeper.getCredentials().getAddress());
        return AccessTemplate.deploy(
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider())
                .send();
    }
}
