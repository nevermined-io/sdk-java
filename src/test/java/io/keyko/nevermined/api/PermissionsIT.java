package io.keyko.nevermined.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.nevermined.api.config.NeverminedConfig;
import io.keyko.nevermined.models.Balance;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.service.ProviderConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PermissionsIT {

    private static final Logger log = LogManager.getLogger(PermissionsIT.class);

    private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;
    private static AssetMetadata metadataBase;

    private static ProviderConfig providerConfig;
    private static NeverminedAPI neverminedAPI;
    private static NeverminedAPI neverminedAPIConsumer;

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

        neverminedAPI = NeverminedAPI.getInstance(config);

        assertNotNull(neverminedAPI.getAssetsAPI());
        assertNotNull(neverminedAPI.getMainAccount());

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
        properties.put(NeverminedConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, config.getString("account.parity.file2"));
        properties.put(NeverminedConfig.DID_REGISTRY_ADDRESS, config.getString("contract.DIDRegistry.address"));
        properties.put(NeverminedConfig.AGREEMENT_STORE_MANAGER_ADDRESS, config.getString("contract.AgreementStoreManager.address"));
        properties.put(NeverminedConfig.CONDITION_STORE_MANAGER_ADDRESS, config.getString("contract.ConditionStoreManager.address"));
        properties.put(NeverminedConfig.LOCKREWARD_CONDITIONS_ADDRESS, config.getString("contract.LockRewardCondition.address"));
        properties.put(NeverminedConfig.ESCROWREWARD_CONDITIONS_ADDRESS, config.getString("contract.EscrowReward.address"));
        properties.put(NeverminedConfig.ESCROW_ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.EscrowAccessSecretStoreTemplate.address"));
        properties.put(NeverminedConfig.ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.AccessSecretStoreCondition.address"));
        properties.put(NeverminedConfig.TEMPLATE_STORE_MANAGER_ADDRESS, config.getString("contract.TemplateStoreManager.address"));
        properties.put(NeverminedConfig.TOKEN_ADDRESS, config.getString("contract.OceanToken.address"));
        properties.put(NeverminedConfig.DISPENSER_ADDRESS, config.getString("contract.Dispenser.address"));
        properties.put(NeverminedConfig.PROVIDER_ADDRESS, config.getString("provider.address"));

        properties.put(NeverminedConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.ComputeExecutionCondition.address"));
        properties.put(NeverminedConfig.ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.EscrowComputeExecutionTemplate.address"));

        neverminedAPIConsumer = NeverminedAPI.getInstance(properties);

        neverminedAPIConsumer.getTokensAPI().request(BigInteger.TEN);
        Balance balance = neverminedAPIConsumer.getAccountsAPI().balance(neverminedAPIConsumer.getMainAccount());
        log.debug("Account " + neverminedAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

    }

    @Test
    public void checkPermissions() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();

        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String ownerAddress = neverminedAPI.getAssetsAPI().owner(ddo.getDid());
        assertEquals(neverminedAPI.getMainAccount().address, ownerAddress);

        Boolean consumerPermission = neverminedAPI.getAssetsAPI().getPermissions(ddo.getDid(), neverminedAPIConsumer.getMainAccount().address);
        assertEquals(false, consumerPermission);

        neverminedAPI.getAssetsAPI().delegatePermissions(ddo.getDid(), neverminedAPIConsumer.getMainAccount().address);
        consumerPermission = neverminedAPI.getAssetsAPI().getPermissions(ddo.getDid(), neverminedAPIConsumer.getMainAccount().address);
        assertEquals(true, consumerPermission);

        neverminedAPI.getAssetsAPI().revokePermissions(ddo.getDid(), neverminedAPIConsumer.getMainAccount().address);
        consumerPermission = neverminedAPI.getAssetsAPI().getPermissions(ddo.getDid(), neverminedAPIConsumer.getMainAccount().address);
        assertEquals(false, consumerPermission);

    }

    @Test
    @Ignore
    public void transferOwnership() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();

        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String ownerAddress = neverminedAPI.getAssetsAPI().owner(ddo.getDid());
        assertEquals(neverminedAPI.getMainAccount().address, ownerAddress);

        Boolean consumerPermission = neverminedAPI.getAssetsAPI().getPermissions(ddo.getDid(), neverminedAPIConsumer.getMainAccount().address);
        assertEquals(false, consumerPermission);

        neverminedAPI.getAssetsAPI().transferOwnership(ddo.getDid(), neverminedAPIConsumer.getMainAccount().address);
        ownerAddress = neverminedAPI.getAssetsAPI().owner(ddo.getDid());
        assertEquals(neverminedAPIConsumer.getMainAccount().address, ownerAddress);

        neverminedAPIConsumer.getAssetsAPI().transferOwnership(ddo.getDid(), neverminedAPI.getMainAccount().address);
        ownerAddress = neverminedAPIConsumer.getAssetsAPI().owner(ddo.getDid());
        assertEquals(neverminedAPI.getMainAccount().address, ownerAddress);


    }


}
