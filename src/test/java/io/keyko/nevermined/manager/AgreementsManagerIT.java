package io.keyko.nevermined.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.api.NeverminedAPI;
import io.keyko.nevermined.api.config.NeverminedConfig;
import io.keyko.nevermined.contracts.*;
import io.keyko.nevermined.core.conditions.LockPaymentConditionPayable;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.models.AssetRewards;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.asset.OrderResult;
import io.keyko.nevermined.models.service.Condition;
import io.keyko.nevermined.models.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AgreementsManagerIT {
    private static final Logger log = LogManager.getLogger(AgreementsManagerIT.class);
    private static AgreementsManager agreementsManager;
    private static ConditionsManager conditionsManager;
    private static KeeperService keeperPublisher;
    private static KeeperService keeperConsumer;
    private static MetadataApiService metadataApiService;
    private static final Config config = ConfigFactory.load();
    private static DIDRegistry didRegistry;
    private static AccessCondition accessCondition;
    private static AccessTemplate accessTemplate;
    private static EscrowPaymentCondition escrowPaymentCondition;
    private static LockPaymentConditionPayable lockPaymentCondition;
    private static AgreementStoreManager agreementsStoreManager;
    private static ConditionStoreManager conditionStoreManager;
    private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;
    private static NeverminedAPI neverminedAPIPublisher;
    private static NeverminedAPI neverminedAPIConsumer;
    private static ProviderConfig providerConfig;


    @BeforeClass
    public static void setUp() throws Exception {
        log.debug("Setting Up DTO's");

        METADATA_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));

        metadataApiService = ManagerHelper.getMetadataService(config);
        String metadataUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/ddo/{did}";
        String provenanceUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/provenance/{did}";
        String gatewayUrl = config.getString("gateway.url");
        String consumeUrl = gatewayUrl + "/api/v1/gateway/services/access";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, gatewayUrl, provenanceUrl, secretStoreEndpoint, providerAddress);

        neverminedAPIPublisher = NeverminedAPI.getInstance(config);
        keeperPublisher = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.main);
        keeperConsumer = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "2");

        neverminedAPIConsumer = ManagerHelper.getNeverminedAPI(config, ManagerHelper.VmClient.parity, "2");

        neverminedAPIConsumer.getTokensAPI().request(BigInteger.TEN);

        agreementsManager = AgreementsManager.getInstance(keeperConsumer, metadataApiService);
        conditionsManager = ConditionsManager.getInstance(keeperConsumer, metadataApiService);
        accessCondition = ManagerHelper.loadAccessConditionContract(keeperConsumer, config.getString("contract.AccessCondition.address"));
        didRegistry = ManagerHelper.loadDIDRegistryContract(keeperConsumer, config.getString("contract.DIDRegistry.address"));
        accessTemplate = ManagerHelper.loadAccessTemplate(keeperConsumer, config.getString("contract.AccessTemplate.address"));
        escrowPaymentCondition = ManagerHelper.loadEscrowPaymentConditionContract(keeperConsumer, config.getString("contract.EscrowPaymentCondition.address"));
        lockPaymentCondition = ManagerHelper.loadLockPaymentCondition(keeperConsumer, config.getString("contract.LockPaymentCondition.address"));
        agreementsStoreManager = ManagerHelper.loadAgreementStoreManager(keeperConsumer, config.getString("contract.AgreementStoreManager.address"));
        conditionStoreManager = ManagerHelper.loadConditionStoreManager(keeperConsumer, config.getString("contract.ConditionStoreManager.address"));
        agreementsManager.setAgreementStoreManagerContract(agreementsStoreManager);
        agreementsManager.setLockCondition(lockPaymentCondition);
        agreementsManager.setAccessCondition(accessCondition);
        agreementsManager.setAccessTemplate(accessTemplate);
        agreementsManager.setEscrowCondition(escrowPaymentCondition);
        agreementsManager.setConditionStoreManagerContract(conditionStoreManager);
        conditionsManager.setAgreementStoreManagerContract(agreementsStoreManager);
        conditionsManager.setDidRegistryContract(didRegistry);
        conditionsManager.setEscrowCondition(escrowPaymentCondition);
        conditionsManager.setAccessTemplate(accessTemplate);

    }

    @Test
    public void status() throws Exception {
        providerConfig.setSecretStoreEndpoint(config.getString("secretstore.url"));
        AssetMetadata metadata = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_JSON_CONTENT);
        AssetRewards assetRewards = new AssetRewards(neverminedAPIPublisher.getMainAccount().getAddress(), "1");
//        assetRewards.tokenAddress = config.getString("contract.NeverminedToken.address");
        DDO ddo = neverminedAPIPublisher.getAssetsAPI().create(metadata, providerConfig, assetRewards);
        DID did = new DID(ddo.id);

        log.debug("DDO registered!");
        neverminedAPIConsumer.getAccountsAPI().requestTokens(BigInteger.valueOf(9000000));
        log.info("Consumer balance: " + neverminedAPIConsumer.getAccountsAPI().balance(neverminedAPIConsumer.getMainAccount()));
        OrderResult orderResult = neverminedAPIConsumer.getAssetsAPI().order(did, Service.DEFAULT_ACCESS_INDEX);

        final InputStream inputStream = neverminedAPIConsumer.getAssetsAPI().downloadBinary(
                orderResult.getServiceAgreementId(),
                did,
                Service.DEFAULT_ACCESS_INDEX,
                0);

        assertTrue(inputStream.readAllBytes().length > 0);

        final String serviceAgreementId = orderResult.getServiceAgreementId();
        TimeUnit.SECONDS.sleep(6l);
        AgreementStatus status = agreementsManager.getStatus(serviceAgreementId);
        assertEquals(orderResult.getServiceAgreementId(), status.agreementId);

        conditionsManager.releaseReward(serviceAgreementId);

        assertEquals(Condition.ConditionStatus.Fulfilled.getStatus(), status.conditions.get(0).conditions.get(Condition.ConditionTypes.lockPayment.toString()));
        assertEquals(Condition.ConditionStatus.Fulfilled.getStatus(), status.conditions.get(0).conditions.get(Condition.ConditionTypes.access.toString()));
        assertEquals(Condition.ConditionStatus.Fulfilled.getStatus(), status.conditions.get(0).conditions.get(Condition.ConditionTypes.escrowPayment.toString()));
        assertEquals(true, status.conditionsFulfilled);

    }

    @Test
    public void status2() throws Exception {
        // 0x00Bd138aBD70e2F00903268F3Db08f2D25677C9e
        final NeverminedAPI apiConsumer = ManagerHelper.getNeverminedAPI(config, ManagerHelper.VmClient.parity, "2");
        // 0xa99d43d86a0758d5632313b8fa3972b6088a21bb
        final NeverminedAPI apiPublisher = ManagerHelper.getNeverminedAPI(config, ManagerHelper.VmClient.parity, "3");

        providerConfig.setSecretStoreEndpoint(config.getString("secretstore.url"));
//        String assetId = "8dca838f94414febe3b250268b4455c2041d5de9affb001968f7f6a632bd213f";
//        DID did = DID.getFromHash(assetId);
//        final DDO ddo = apiConsumer.getAssetsAPI().resolve(did);

        AssetMetadata metadata = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_JSON_CONTENT);
        final DDO ddo = apiPublisher.getAssetsAPI().create(metadata, providerConfig);
        final DID did = new DID(ddo.id);


        apiConsumer.getAccountsAPI().requestTokens(BigInteger.valueOf(9000000));
        log.info("Consumer balance: " + apiConsumer.getAccountsAPI().balance(apiConsumer.getMainAccount()));
        OrderResult orderResult = apiConsumer.getAssetsAPI().order(did, Service.DEFAULT_ACCESS_INDEX);

        final InputStream inputStream = apiConsumer.getAssetsAPI().downloadBinary(
                orderResult.getServiceAgreementId(),
                did,
                Service.DEFAULT_ACCESS_INDEX,
                0);

        assertTrue(inputStream.readAllBytes().length > 0);

        final String serviceAgreementId = orderResult.getServiceAgreementId();
        TimeUnit.SECONDS.sleep(6l);
        final Agreement agreement = agreementsManager.getAgreement(serviceAgreementId);
        AgreementStatus status = agreementsManager.getStatus(serviceAgreementId);
        assertEquals(orderResult.getServiceAgreementId(), status.agreementId);

//        conditionsManager.releaseReward(serviceAgreementId);
//        escrowPayment.fulfill(serviceAgreementId, )

        assertEquals(Condition.ConditionStatus.Fulfilled.getStatus(), status.conditions.get(0).conditions.get(Condition.ConditionTypes.lockPayment.toString()));
        assertEquals(Condition.ConditionStatus.Fulfilled.getStatus(), status.conditions.get(0).conditions.get(Condition.ConditionTypes.access.toString()));
        assertEquals(Condition.ConditionStatus.Fulfilled.getStatus(), status.conditions.get(0).conditions.get(Condition.ConditionTypes.escrowPayment.toString()));
        assertEquals(true, status.conditionsFulfilled);

    }
}
