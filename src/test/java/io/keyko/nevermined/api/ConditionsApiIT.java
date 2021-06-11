package io.keyko.nevermined.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.nevermined.api.config.NeverminedConfig;
import io.keyko.nevermined.core.sla.handlers.ServiceAgreementHandler;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.service.AgreementStatus;
import io.keyko.nevermined.models.service.Condition;
import io.keyko.nevermined.models.service.ProviderConfig;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ConditionsApiIT {

    private static NeverminedAPI neverminedAPI;
    private static NeverminedAPI neverminedAPIConsumer;
    private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;
    private static AssetMetadata metadataBase;
    private static ProviderConfig providerConfig;
    private static Config config;

    @BeforeClass
    public static void setUp() throws Exception {


        config = ConfigFactory.load();
        METADATA_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
        metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_JSON_CONTENT);

        String metadataUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/ddo/{did}";
        String provenanceUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/provenance/{did}";
        String consumeUrl = config.getString("gateway.url") + "/api/v1/gateway/services/access";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);
        neverminedAPIConsumer = NeverminedAPI.getInstance(config);
        Properties properties = new Properties();
        properties.put(NeverminedConfig.KEEPER_URL, config.getString("keeper.url"));
        properties.put(NeverminedConfig.KEEPER_GAS_LIMIT, config.getString("keeper.gasLimit"));
        properties.put(NeverminedConfig.KEEPER_GAS_PRICE, config.getString("keeper.gasPrice"));
        properties.put(NeverminedConfig.KEEPER_TX_ATTEMPTS, config.getString("keeper.tx.attempts"));
        properties.put(NeverminedConfig.KEEPER_TX_SLEEPDURATION, config.getString("keeper.tx.sleepDuration"));
        properties.put(NeverminedConfig.METADATA_URL, config.getString("metadata.url"));
        properties.put(NeverminedConfig.SECRETSTORE_URL, config.getString("secretstore.url"));
        properties.put(NeverminedConfig.CONSUME_BASE_PATH, config.getString("consume.basePath"));
        properties.put(NeverminedConfig.MAIN_ACCOUNT_ADDRESS, config.getString("account.parity.address2"));
        properties.put(NeverminedConfig.MAIN_ACCOUNT_PASSWORD, config.getString("account.parity.password2"));
        properties.put(NeverminedConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, config.getString("account.parity.credentialsFile2"));
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


        neverminedAPI = NeverminedAPI.getInstance(properties);
        neverminedAPIConsumer.getTokensAPI().request(BigInteger.TEN);
        neverminedAPI.getTokensAPI().request(BigInteger.TEN);

        assertNotNull(neverminedAPI.getAssetsAPI());
        assertNotNull(neverminedAPI.getMainAccount());

    }


    // TODO: Review what's happening with this test
    @Ignore
    @Test
    public void executeConditions() throws Exception {
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String agreementId = ServiceAgreementHandler.generateSlaId();
        assertTrue(neverminedAPI.getAgreementsAPI().create(ddo.getDID(), agreementId, 1, neverminedAPIConsumer.getMainAccount().address));
        AgreementStatus initialStatus = neverminedAPI.getAgreementsAPI().status(agreementId);
        assertEquals(BigInteger.ONE, initialStatus.conditions.get(0).conditions.get("lockPayment"));
        assertEquals(BigInteger.ONE, initialStatus.conditions.get(0).conditions.get("access"));
        assertEquals(BigInteger.ONE, initialStatus.conditions.get(0).conditions.get("escrowPayment"));

        final List<BigInteger> amounts = Arrays.asList(BigInteger.TEN, BigInteger.TWO);
        final List<String> receivers = Arrays.asList(
                config.getString("account.parity.address2"),
                NeverminedConfig.PROVIDER_ADDRESS);

        neverminedAPI.getConditionsAPI().lockPayment(agreementId);
        AgreementStatus statusAfterLockReward = neverminedAPI.getAgreementsAPI().status(agreementId);
        assertEquals(BigInteger.TWO, statusAfterLockReward.conditions.get(0).conditions.get(
                Condition.ConditionTypes.lockPayment.toString()));
        assertEquals(BigInteger.ONE, statusAfterLockReward.conditions.get(0).conditions.get(
                Condition.ConditionTypes.escrowPayment.toString()));

        int retries= 10;
        long sleepSeconds= 1l;

        for (int counter=0; counter< retries; counter++)    {
            try {
                neverminedAPI.getConditionsAPI().grantAccess(agreementId, ddo.getDID(), neverminedAPIConsumer.getMainAccount().address);
                break;
            } catch (Exception e)   {
                TimeUnit.SECONDS.sleep(sleepSeconds);
            }
        }

        AgreementStatus statusAfterAccessGranted = neverminedAPI.getAgreementsAPI().status(agreementId);
        assertEquals(BigInteger.TWO, statusAfterAccessGranted.conditions.get(0).conditions.get(Condition.ConditionTypes.lockPayment.toString()));
        assertEquals(BigInteger.TWO, statusAfterAccessGranted.conditions.get(0).conditions.get(Condition.ConditionTypes.access.toString()));
        assertEquals(BigInteger.ONE, statusAfterAccessGranted.conditions.get(0).conditions.get(Condition.ConditionTypes.escrowPayment.toString()));


        neverminedAPI.getConditionsAPI().releaseReward(agreementId);
        AgreementStatus statusAfterReleaseReward = neverminedAPI.getAgreementsAPI().status(agreementId);
        assertEquals(BigInteger.TWO, statusAfterReleaseReward.conditions.get(0).conditions.get(Condition.ConditionTypes.lockPayment.toString()));
        assertEquals(BigInteger.TWO, statusAfterReleaseReward.conditions.get(0).conditions.get(Condition.ConditionTypes.access.toString()));
        assertEquals(BigInteger.TWO, statusAfterReleaseReward.conditions.get(0).conditions.get(Condition.ConditionTypes.escrowPayment.toString()));



    }
}
