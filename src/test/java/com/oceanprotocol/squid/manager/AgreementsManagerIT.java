package com.oceanprotocol.squid.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.keeper.contracts.*;
import com.oceanprotocol.squid.api.OceanAPI;
import com.oceanprotocol.squid.api.config.OceanConfig;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.common.web3.KeeperService;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.asset.OrderResult;
import com.oceanprotocol.squid.models.service.AgreementStatus;
import com.oceanprotocol.squid.models.service.ProviderConfig;
import com.oceanprotocol.squid.models.service.Service;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.reactivex.Flowable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class AgreementsManagerIT {
    private static final Logger log = LogManager.getLogger(AgreementsManagerIT.class);
    private static AgreementsManager agreementsManager;
    private static KeeperService keeper;
    private static AquariusService aquarius;
    private static final Config config = ConfigFactory.load();
    private static AccessSecretStoreCondition accessSecretStoreCondition;
    private static EscrowAccessSecretStoreTemplate escrowAccessSecretStoreTemplate;
    private static EscrowReward escrowReward;
    private static LockRewardCondition lockRewardCondition;
    private static AgreementStoreManager agreementsStoreManager;
    private static ConditionStoreManager conditionStoreManager;
    private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;
    private static OceanAPI oceanAPI;
    private static OceanAPI oceanAPIConsumer;
    private static ProviderConfig providerConfig;


    @BeforeClass
    public static void setUp() throws Exception {
        log.debug("Setting Up DTO's");

        METADATA_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));

        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "2");
        aquarius = ManagerHelper.getAquarius(config);
        String metadataUrl = config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/ddo/{did}";
        String provenanceUrl = config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/provenance/{did}";
        String consumeUrl = config.getString("brizo.url") + "/api/v1/brizo/services/consume";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");
        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);
        oceanAPI = OceanAPI.getInstance(config);
        Properties properties = new Properties();
        properties.put(OceanConfig.KEEPER_URL, config.getString("keeper.url"));
        properties.put(OceanConfig.KEEPER_GAS_LIMIT, config.getString("keeper.gasLimit"));
        properties.put(OceanConfig.KEEPER_GAS_PRICE, config.getString("keeper.gasPrice"));
        properties.put(OceanConfig.KEEPER_TX_ATTEMPTS, config.getString("keeper.tx.attempts"));
        properties.put(OceanConfig.KEEPER_TX_SLEEPDURATION, config.getString("keeper.tx.sleepDuration"));
        properties.put(OceanConfig.AQUARIUS_URL, config.getString("aquarius.url"));
        properties.put(OceanConfig.SECRETSTORE_URL, config.getString("secretstore.url"));
        properties.put(OceanConfig.CONSUME_BASE_PATH, config.getString("consume.basePath"));
        properties.put(OceanConfig.MAIN_ACCOUNT_ADDRESS, config.getString("account.parity.address2"));
        properties.put(OceanConfig.MAIN_ACCOUNT_PASSWORD, config.getString("account.parity.password2"));
        properties.put(OceanConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, config.getString("account.parity.file2"));
        properties.put(OceanConfig.DID_REGISTRY_ADDRESS, config.getString("contract.DIDRegistry.address"));
        properties.put(OceanConfig.AGREEMENT_STORE_MANAGER_ADDRESS, config.getString("contract.AgreementStoreManager.address"));
        properties.put(OceanConfig.CONDITION_STORE_MANAGER_ADDRESS, config.getString("contract.ConditionStoreManager.address"));
        properties.put(OceanConfig.LOCKREWARD_CONDITIONS_ADDRESS, config.getString("contract.LockRewardCondition.address"));
        properties.put(OceanConfig.ESCROWREWARD_CONDITIONS_ADDRESS, config.getString("contract.EscrowReward.address"));
        properties.put(OceanConfig.ESCROW_ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.EscrowAccessSecretStoreTemplate.address"));
        properties.put(OceanConfig.ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.AccessSecretStoreCondition.address"));
        properties.put(OceanConfig.TEMPLATE_STORE_MANAGER_ADDRESS, config.getString("contract.TemplateStoreManager.address"));
        properties.put(OceanConfig.TOKEN_ADDRESS, config.getString("contract.OceanToken.address"));
        properties.put(OceanConfig.DISPENSER_ADDRESS, config.getString("contract.Dispenser.address"));
        properties.put(OceanConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.ComputeExecutionCondition.address"));
        properties.put(OceanConfig.ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.EscrowComputeExecutionTemplate.address"));
        properties.put(OceanConfig.PROVIDER_ADDRESS, config.getString("provider.address"));
        oceanAPIConsumer = OceanAPI.getInstance(properties);
        oceanAPIConsumer.getTokensAPI().request(BigInteger.TEN);
        agreementsManager = AgreementsManager.getInstance(keeper, aquarius);
        accessSecretStoreCondition = ManagerHelper.loadAccessSecretStoreConditionContract(keeper, config.getString("contract.AccessSecretStoreCondition.address"));
        escrowAccessSecretStoreTemplate = ManagerHelper.loadEscrowAccessSecretStoreTemplate(keeper, config.getString("contract.EscrowAccessSecretStoreTemplate.address"));
        escrowReward = ManagerHelper.loadEscrowRewardContract(keeper, config.getString("contract.EscrowReward.address"));
        lockRewardCondition = ManagerHelper.loadLockRewardCondition(keeper, config.getString("contract.LockRewardCondition.address"));
        agreementsStoreManager = ManagerHelper.loadAgreementStoreManager(keeper, config.getString("contract.AgreementStoreManager.address"));
        conditionStoreManager = ManagerHelper.loadConditionStoreManager(keeper, config.getString("contract.ConditionStoreManager.address"));
        agreementsManager.setAgreementStoreManagerContract(agreementsStoreManager);
        agreementsManager.setLockRewardCondition(lockRewardCondition);
        agreementsManager.setAccessSecretStoreCondition(accessSecretStoreCondition);
        agreementsManager.setEscrowAccessSecretStoreTemplate(escrowAccessSecretStoreTemplate);
        agreementsManager.setEscrowReward(escrowReward);
        agreementsManager.setConditionStoreManagerContract(conditionStoreManager);

    }

    @Test
    public void status() throws Exception {
        providerConfig.setSecretStoreEndpoint(config.getString("secretstore.url"));
        AssetMetadata metadata = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_JSON_CONTENT);
        DDO ddo = oceanAPI.getAssetsAPI().create(metadata, providerConfig);
        DID did = new DID(ddo.id);


        log.debug("DDO registered!");
        oceanAPIConsumer.getAccountsAPI().requestTokens(BigInteger.valueOf(9000000));
        log.info("Consumer balance: " + oceanAPIConsumer.getAccountsAPI().balance(oceanAPIConsumer.getMainAccount()));
        Flowable<OrderResult> response = oceanAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_ACCESS_INDEX);

        OrderResult orderResult = response.blockingFirst();
        TimeUnit.SECONDS.sleep(5l);
        agreementsManager.getAgreement(orderResult.getServiceAgreementId());
        AgreementStatus status = agreementsManager.getStatus(orderResult.getServiceAgreementId());
        assertEquals(orderResult.getServiceAgreementId(), status.agreementId);
        assertEquals(BigInteger.TWO, status.conditions.get(0).conditions.get("lockReward"));
        assertEquals(BigInteger.TWO, status.conditions.get(0).conditions.get("accessSecretStore"));
        assertEquals(BigInteger.TWO, status.conditions.get(0).conditions.get("escrowReward"));
    }
}
