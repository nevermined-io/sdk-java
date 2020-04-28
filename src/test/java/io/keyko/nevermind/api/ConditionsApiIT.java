package io.keyko.nevermind.api;

import com.fasterxml.jackson.core.type.TypeReference;
import io.keyko.nevermind.api.config.NevermindConfig;
import io.keyko.nevermind.core.sla.handlers.ServiceAgreementHandler;
import io.keyko.nevermind.models.DDO;
import io.keyko.nevermind.models.asset.AssetMetadata;
import io.keyko.nevermind.models.service.AgreementStatus;
import io.keyko.nevermind.models.service.Condition;
import io.keyko.nevermind.models.service.ProviderConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ConditionsApiIT {

    private static NevermindAPI nevermindAPI;
    private static NevermindAPI nevermindAPIConsumer;
    private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;
    private static AssetMetadata metadataBase;
    private static ProviderConfig providerConfig;


    @BeforeClass
    public static void setUp() throws Exception {


        Config config = ConfigFactory.load();
        METADATA_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
        metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_JSON_CONTENT);

        String metadataUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/ddo/{did}";
        String provenanceUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/provenance/{did}";
        String consumeUrl = config.getString("gateway.url") + "/api/v1/gateway/services/consume";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);
        nevermindAPIConsumer = NevermindAPI.getInstance(config);
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
        properties.put(NevermindConfig.ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.AccessSecretStoreCondition.address"));
        properties.put(NevermindConfig.TEMPLATE_STORE_MANAGER_ADDRESS, config.getString("contract.TemplateStoreManager.address"));
        properties.put(NevermindConfig.TOKEN_ADDRESS, config.getString("contract.OceanToken.address"));
        properties.put(NevermindConfig.DISPENSER_ADDRESS, config.getString("contract.Dispenser.address"));
        properties.put(NevermindConfig.PROVIDER_ADDRESS, config.getString("provider.address"));


        nevermindAPI = NevermindAPI.getInstance(properties);
        nevermindAPIConsumer.getTokensAPI().request(BigInteger.TEN);
        nevermindAPI.getTokensAPI().request(BigInteger.TEN);


        assertNotNull(nevermindAPI.getAssetsAPI());
        assertNotNull(nevermindAPI.getMainAccount());

    }


    // TODO: Review what's happening with this test
    @Ignore
    @Test
    public void executeConditions() throws Exception {
        DDO ddo = nevermindAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String agreementId = ServiceAgreementHandler.generateSlaId();
        assertTrue(nevermindAPI.getAgreementsAPI().create(ddo.getDid(), agreementId, 1, nevermindAPIConsumer.getMainAccount().address));
        AgreementStatus initialStatus = nevermindAPI.getAgreementsAPI().status(agreementId);
        assertEquals(BigInteger.ONE, initialStatus.conditions.get(0).conditions.get("lockReward"));
        assertEquals(BigInteger.ONE, initialStatus.conditions.get(0).conditions.get("accessSecretStore"));
        assertEquals(BigInteger.ONE, initialStatus.conditions.get(0).conditions.get("escrowReward"));

        nevermindAPI.getConditionsAPI().lockReward(agreementId, BigInteger.TEN);
        AgreementStatus statusAfterLockReward = nevermindAPI.getAgreementsAPI().status(agreementId);
        assertEquals(BigInteger.TWO, statusAfterLockReward.conditions.get(0).conditions.get(
                Condition.ConditionTypes.lockReward.toString()));
      //  assertEquals(BigInteger.TWO, statusAfterLockReward.conditions.get(0).conditions.get("accessSecretStore"));
        assertEquals(BigInteger.ONE, statusAfterLockReward.conditions.get(0).conditions.get(
                Condition.ConditionTypes.escrowReward.toString()));

        int retries= 10;
        long sleepSeconds= 1l;

        for (int counter=0; counter< retries; counter++)    {
            try {
                nevermindAPI.getConditionsAPI().grantAccess(agreementId, ddo.getDid(), nevermindAPIConsumer.getMainAccount().address);
                break;
            } catch (Exception e)   {
                TimeUnit.SECONDS.sleep(sleepSeconds);
            }
        }

        AgreementStatus statusAfterAccessGranted = nevermindAPI.getAgreementsAPI().status(agreementId);
        assertEquals(BigInteger.TWO, statusAfterAccessGranted.conditions.get(0).conditions.get(Condition.ConditionTypes.lockReward.toString()));
        assertEquals(BigInteger.TWO, statusAfterAccessGranted.conditions.get(0).conditions.get(Condition.ConditionTypes.accessSecretStore.toString()));
        assertEquals(BigInteger.ONE, statusAfterAccessGranted.conditions.get(0).conditions.get(Condition.ConditionTypes.escrowReward.toString()));


        nevermindAPI.getConditionsAPI().releaseReward(agreementId, BigInteger.TEN);
        AgreementStatus statusAfterReleaseReward = nevermindAPI.getAgreementsAPI().status(agreementId);
        assertEquals(BigInteger.TWO, statusAfterReleaseReward.conditions.get(0).conditions.get(Condition.ConditionTypes.lockReward.toString()));
        assertEquals(BigInteger.TWO, statusAfterReleaseReward.conditions.get(0).conditions.get(Condition.ConditionTypes.accessSecretStore.toString()));
        assertEquals(BigInteger.TWO, statusAfterReleaseReward.conditions.get(0).conditions.get(Condition.ConditionTypes.escrowReward.toString()));



    }
}
