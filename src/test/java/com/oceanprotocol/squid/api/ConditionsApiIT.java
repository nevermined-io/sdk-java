package com.oceanprotocol.squid.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.squid.api.config.OceanConfig;
import com.oceanprotocol.squid.core.sla.handlers.ServiceAgreementHandler;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.service.AgreementStatus;
import com.oceanprotocol.squid.models.service.ProviderConfig;
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

    private static OceanAPI oceanAPI;
    private static OceanAPI oceanAPIConsumer;
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

        String metadataUrl = config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/ddo/{did}";
        String provenanceUrl = config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/provenance/{did}";
        String consumeUrl = config.getString("brizo.url") + "/api/v1/brizo/services/consume";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);
        oceanAPIConsumer = OceanAPI.getInstance(config);
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
        properties.put(OceanConfig.PROVIDER_ADDRESS, config.getString("provider.address"));


        oceanAPI = OceanAPI.getInstance(properties);
        oceanAPIConsumer.getTokensAPI().request(BigInteger.TEN);
        oceanAPI.getTokensAPI().request(BigInteger.TEN);


        assertNotNull(oceanAPI.getAssetsAPI());
        assertNotNull(oceanAPI.getMainAccount());

    }


    // TODO: Review what's happening with this test
    @Ignore
    @Test
    public void executeConditions() throws Exception {
        DDO ddo = oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String agreementId = ServiceAgreementHandler.generateSlaId();
        assertTrue(oceanAPI.getAgreementsAPI().create(ddo.getDid(), agreementId, 1, oceanAPIConsumer.getMainAccount().address));
        AgreementStatus initialStatus = oceanAPI.getAgreementsAPI().status(agreementId);
        assertEquals(BigInteger.ONE, initialStatus.conditions.get(0).conditions.get("lockReward"));
        assertEquals(BigInteger.ONE, initialStatus.conditions.get(0).conditions.get("accessSecretStore"));
        assertEquals(BigInteger.ONE, initialStatus.conditions.get(0).conditions.get("escrowReward"));

        oceanAPI.getConditionsAPI().lockReward(agreementId, BigInteger.TEN);
        AgreementStatus statusAfterLockReward = oceanAPI.getAgreementsAPI().status(agreementId);
        assertEquals(BigInteger.TWO, statusAfterLockReward.conditions.get(0).conditions.get("lockReward"));
      //  assertEquals(BigInteger.TWO, statusAfterLockReward.conditions.get(0).conditions.get("accessSecretStore"));
        assertEquals(BigInteger.ONE, statusAfterLockReward.conditions.get(0).conditions.get("escrowReward"));

        int retries= 10;
        long sleepSeconds= 1l;

        for (int counter=0; counter< retries; counter++)    {
            try {
                oceanAPI.getConditionsAPI().grantAccess(agreementId, ddo.getDid(), oceanAPIConsumer.getMainAccount().address);
                break;
            } catch (Exception e)   {
                TimeUnit.SECONDS.sleep(sleepSeconds);
            }
        }

        AgreementStatus statusAfterAccessGranted = oceanAPI.getAgreementsAPI().status(agreementId);
        assertEquals(BigInteger.TWO, statusAfterAccessGranted.conditions.get(0).conditions.get("lockReward"));
        assertEquals(BigInteger.TWO, statusAfterAccessGranted.conditions.get(0).conditions.get("accessSecretStore"));
        assertEquals(BigInteger.ONE, statusAfterAccessGranted.conditions.get(0).conditions.get("escrowReward"));


        oceanAPI.getConditionsAPI().releaseReward(agreementId, BigInteger.TEN);
        AgreementStatus statusAfterReleaseReward = oceanAPI.getAgreementsAPI().status(agreementId);
        assertEquals(BigInteger.TWO, statusAfterReleaseReward.conditions.get(0).conditions.get("lockReward"));
        assertEquals(BigInteger.TWO, statusAfterReleaseReward.conditions.get(0).conditions.get("accessSecretStore"));
        assertEquals(BigInteger.TWO, statusAfterReleaseReward.conditions.get(0).conditions.get("escrowReward"));



    }
}
