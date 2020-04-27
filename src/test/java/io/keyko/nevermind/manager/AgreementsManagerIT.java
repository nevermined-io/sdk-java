package io.keyko.nevermind.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import io.keyko.common.helpers.EncodingHelper;
import io.keyko.common.helpers.EthereumHelper;
import io.keyko.nevermind.api.NevermindAPI;
import io.keyko.nevermind.api.config.NevermindConfig;
import io.keyko.nevermind.external.MetadataService;
import io.keyko.nevermind.models.DDO;
import io.keyko.nevermind.models.DID;
import io.keyko.nevermind.models.asset.AssetMetadata;
import io.keyko.nevermind.models.asset.OrderResult;
import io.keyko.nevermind.models.service.Agreement;
import io.keyko.nevermind.models.service.AgreementStatus;
import io.keyko.nevermind.models.service.ProviderConfig;
import io.keyko.nevermind.models.service.Service;
import io.keyko.nevermind.contracts.*;
import io.keyko.common.web3.KeeperService;
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
    private static ConditionsManager conditionsManager;
    private static KeeperService keeper;
    private static MetadataService metadataService;
    private static final Config config = ConfigFactory.load();
    private static AccessSecretStoreCondition accessSecretStoreCondition;
    private static EscrowAccessSecretStoreTemplate escrowAccessSecretStoreTemplate;
    private static EscrowReward escrowReward;
    private static LockRewardCondition lockRewardCondition;
    private static AgreementStoreManager agreementsStoreManager;
    private static ConditionStoreManager conditionStoreManager;
    private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;
    private static NevermindAPI nevermindAPI;
    private static NevermindAPI nevermindAPIConsumer;
    private static ProviderConfig providerConfig;


    @BeforeClass
    public static void setUp() throws Exception {
        log.debug("Setting Up DTO's");

        METADATA_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));

        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "2");
        metadataService = ManagerHelper.getMetadataService(config);
        String metadataUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/ddo/{did}";
        String provenanceUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/provenance/{did}";
        String consumeUrl = config.getString("gateway.url") + "/api/v1/gateway/services/consume";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");
        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);
        nevermindAPI = NevermindAPI.getInstance(config);
        Properties properties = new Properties();
        properties.put(NevermindConfig.KEEPER_URL, config.getString("keeper.url"));
        properties.put(NevermindConfig.KEEPER_GAS_LIMIT, config.getString("keeper.gasLimit"));
        properties.put(NevermindConfig.KEEPER_GAS_PRICE, config.getString("keeper.gasPrice"));
        properties.put(NevermindConfig.KEEPER_TX_ATTEMPTS, config.getString("keeper.tx.attempts"));
        properties.put(NevermindConfig.KEEPER_TX_SLEEPDURATION, config.getString("keeper.tx.sleepDuration"));
        properties.put(NevermindConfig.METADATA_URL, config.getString("metadata.url"));
        properties.put(NevermindConfig.SECRETSTORE_URL, config.getString("secretstore.url"));
        properties.put(NevermindConfig.CONSUME_BASE_PATH, config.getString("consume.basePath"));
        properties.put(NevermindConfig.MAIN_ACCOUNT_ADDRESS, config.getString("account.parity.address2"));
        properties.put(NevermindConfig.MAIN_ACCOUNT_PASSWORD, config.getString("account.parity.password2"));
        properties.put(NevermindConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, config.getString("account.parity.file2"));
        properties.put(NevermindConfig.DID_REGISTRY_ADDRESS, config.getString("contract.DIDRegistry.address"));
        properties.put(NevermindConfig.AGREEMENT_STORE_MANAGER_ADDRESS, config.getString("contract.AgreementStoreManager.address"));
        properties.put(NevermindConfig.CONDITION_STORE_MANAGER_ADDRESS, config.getString("contract.ConditionStoreManager.address"));
        properties.put(NevermindConfig.LOCKREWARD_CONDITIONS_ADDRESS, config.getString("contract.LockRewardCondition.address"));
        properties.put(NevermindConfig.ESCROWREWARD_CONDITIONS_ADDRESS, config.getString("contract.EscrowReward.address"));
        properties.put(NevermindConfig.ESCROW_ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.EscrowAccessSecretStoreTemplate.address"));
        properties.put(NevermindConfig.ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.AccessSecretStoreCondition.address"));
        properties.put(NevermindConfig.TEMPLATE_STORE_MANAGER_ADDRESS, config.getString("contract.TemplateStoreManager.address"));
        properties.put(NevermindConfig.TOKEN_ADDRESS, config.getString("contract.OceanToken.address"));
        properties.put(NevermindConfig.DISPENSER_ADDRESS, config.getString("contract.Dispenser.address"));
        properties.put(NevermindConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.ComputeExecutionCondition.address"));
        properties.put(NevermindConfig.ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.EscrowComputeExecutionTemplate.address"));
        properties.put(NevermindConfig.PROVIDER_ADDRESS, config.getString("provider.address"));
        nevermindAPIConsumer = NevermindAPI.getInstance(properties);
        nevermindAPIConsumer.getTokensAPI().request(BigInteger.TEN);
        agreementsManager = AgreementsManager.getInstance(keeper, metadataService);
        conditionsManager = ConditionsManager.getInstance(keeper, metadataService);
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
        DDO ddo = nevermindAPI.getAssetsAPI().create(metadata, providerConfig);
        DID did = new DID(ddo.id);
        String price = ddo.getMetadataService().attributes.main.price;

        log.debug("DDO registered!");
        nevermindAPIConsumer.getAccountsAPI().requestTokens(BigInteger.valueOf(9000000));
        log.info("Consumer balance: " + nevermindAPIConsumer.getAccountsAPI().balance(nevermindAPIConsumer.getMainAccount()));
        Flowable<OrderResult> response = nevermindAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_ACCESS_INDEX);

        OrderResult orderResult = response.blockingFirst();
        TimeUnit.SECONDS.sleep(5l);

        final String serviceAgreementId = orderResult.getServiceAgreementId();
        final Agreement agreement = agreementsManager.getAgreement(serviceAgreementId);
        AgreementStatus status = agreementsManager.getStatus(serviceAgreementId);
        assertEquals(orderResult.getServiceAgreementId(), status.agreementId);
        assertEquals(BigInteger.TWO, status.conditions.get(0).conditions.get("lockReward"));
        assertEquals(BigInteger.TWO, status.conditions.get(0).conditions.get("accessSecretStore"));
        assertEquals(BigInteger.TWO, status.conditions.get(0).conditions.get("escrowReward"));

/*        final byte[] saBytes = EncodingHelper.hexStringToBytes(EthereumHelper.remove0x(serviceAgreementId));
        final byte[] hashValues = lockRewardCondition.hashValues(
                ddo.proof.creator, new BigInteger(price)).send();
        final byte[] lockRewardConditionId = lockRewardCondition.generateId(saBytes, hashValues).send();

        log.debug("Generated ConditionId: " + lockRewardConditionId);
        agreement.conditions.forEach( c -> log.debug("ConditionId: " + c));*/
    }
}
