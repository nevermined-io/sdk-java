package io.keyko.ocean.manager;

import io.keyko.common.helpers.CryptoHelper;
import io.keyko.ocean.api.OceanAPI;
import io.keyko.ocean.api.config.OceanConfig;
import io.keyko.ocean.exceptions.EthereumException;
import io.keyko.ocean.keeper.contracts.*;
import com.oceanprotocol.secretstore.core.EvmDto;
import com.oceanprotocol.secretstore.core.SecretStoreDto;
import io.keyko.ocean.external.AquariusService;
import io.keyko.common.web3.KeeperService;
import com.typesafe.config.Config;
import io.keyko.ocean.models.service.template.TemplateSEA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public abstract class ManagerHelper {

    private static final Logger log = LogManager.getLogger(ManagerHelper.class);

    public enum VmClient { ganache, parity}

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
                config.getString("account." + client.toString() + ".file"),
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
                config.getString("account." + client.toString() + ".file" + nAddress),
                config.getInt("keeper.tx.attempts"),
                config.getLong("keeper.tx.sleepDuration")
        );

        keeper.setGasLimit(BigInteger.valueOf(config.getLong("keeper.gasLimit")))
                .setGasPrice(BigInteger.valueOf(config.getLong("keeper.gasPrice")));

        return keeper;
    }

    public static AquariusService getAquarius(Config config) {
        return AquariusService.getInstance(config.getString("aquarius.url"));
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

    public static boolean prepareEscrowTemplate(OceanAPI oceanAPI, String templateId, String conditionAddress, String owner, String templateName) throws EthereumException, InterruptedException {

        BigInteger numberTemplates= oceanAPI.getTemplatesAPI().getListSize();
        log.debug("Number of existing templates: " + numberTemplates.toString());

        try {
            log.debug("Registering actor type");
            oceanAPI.getTemplatesAPI().registerActorType("consumer");
        } catch (EthereumException ex)  {}

        TemplateSEA template= oceanAPI.getTemplatesAPI().getTemplate(templateId);

        if (template.state.compareTo(TemplateSEA.TemplateState.Uninitialized.getStatus()) == 0) {
            log.debug("Proposing template: " + templateId);
            oceanAPI.getTemplatesAPI().propose(
                    CryptoHelper.keccak256(templateId),
                    Arrays.asList(conditionAddress),
                    Arrays.asList(CryptoHelper.keccak256(owner)),
                    templateName);

        }

        for (int counter= 0; counter<10; counter++) {
            log.debug("Waiting for the template proposal ...");
            template= oceanAPI.getTemplatesAPI().getTemplate(templateId);
            if (template.state.compareTo(TemplateSEA.TemplateState.Proposed.getStatus()) == 0) {
                log.debug("Template " + templateId + " in Proposed state");
                break;
            }
            ManagerHelper.class.wait(1000L);
        }

        final boolean isApproved = oceanAPI.getTemplatesAPI().isApproved(templateId);
        if (!isApproved) {
            log.debug("Approving template: " + templateId);
            oceanAPI.getTemplatesAPI().approve(templateId);
        }
        return true;
    }

    /**
     * Returns a Properties object with the entries necessary to run the integration tests
     * @param config
     * @return
     */
    public static Properties getDefaultProperties(Config config, String numAddress)    {
        Properties properties = new Properties();
        properties.put(OceanConfig.KEEPER_URL, config.getString("keeper.url"));
        properties.put(OceanConfig.KEEPER_GAS_LIMIT, config.getString("keeper.gasLimit"));
        properties.put(OceanConfig.KEEPER_GAS_PRICE, config.getString("keeper.gasPrice"));
        properties.put(OceanConfig.KEEPER_TX_ATTEMPTS, config.getString("keeper.tx.attempts"));
        properties.put(OceanConfig.KEEPER_TX_SLEEPDURATION, config.getString("keeper.tx.sleepDuration"));
        properties.put(OceanConfig.AQUARIUS_URL, config.getString("aquarius.url"));
        properties.put(OceanConfig.SECRETSTORE_URL, config.getString("secretstore.url"));
        properties.put(OceanConfig.CONSUME_BASE_PATH, config.getString("consume.basePath"));
        properties.put(OceanConfig.MAIN_ACCOUNT_ADDRESS, config.getString("account.parity.address" + numAddress));
        properties.put(OceanConfig.MAIN_ACCOUNT_PASSWORD, config.getString("account.parity.password" + numAddress));
        properties.put(OceanConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, config.getString("account.parity.file" + numAddress));
        properties.put(OceanConfig.DID_REGISTRY_ADDRESS, config.getString("contract.DIDRegistry.address"));
        properties.put(OceanConfig.AGREEMENT_STORE_MANAGER_ADDRESS, config.getString("contract.AgreementStoreManager.address"));
        properties.put(OceanConfig.CONDITION_STORE_MANAGER_ADDRESS, config.getString("contract.ConditionStoreManager.address"));
        properties.put(OceanConfig.LOCKREWARD_CONDITIONS_ADDRESS, config.getString("contract.LockRewardCondition.address"));
        properties.put(OceanConfig.ESCROWREWARD_CONDITIONS_ADDRESS, config.getString("contract.EscrowReward.address"));
        properties.put(OceanConfig.ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.AccessSecretStoreCondition.address"));
        properties.put(OceanConfig.TEMPLATE_STORE_MANAGER_ADDRESS, config.getString("contract.TemplateStoreManager.address"));
        properties.put(OceanConfig.TOKEN_ADDRESS, config.getString("contract.OceanToken.address"));
        properties.put(OceanConfig.DISPENSER_ADDRESS, config.getString("contract.Dispenser.address"));
        properties.put(OceanConfig.PROVIDER_ADDRESS, config.getString("provider.address"));
        properties.put(OceanConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.ComputeExecutionCondition.address"));
        return properties;
    }

    public static OceanToken loadOceanTokenContract(KeeperService keeper, String address) {
        return OceanToken.load(
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


    public static EscrowReward loadEscrowRewardContract(KeeperService keeper, String address) {
        return EscrowReward.load(
                address,
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    public static LockRewardCondition loadLockRewardCondition(KeeperService keeper, String address) {
        return LockRewardCondition.load(address,
                keeper.getWeb3(),
                keeper.getTxManager(),
                keeper.getContractGasProvider()
        );
    }

    public static AccessSecretStoreCondition loadAccessSecretStoreConditionContract(KeeperService keeper, String address) {
        return AccessSecretStoreCondition.load(address,
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

}
