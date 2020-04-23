package io.keyko.nevermind.api;

import com.fasterxml.jackson.core.type.TypeReference;
import io.keyko.nevermind.api.config.NevermindConfig;
import io.keyko.nevermind.models.Balance;
import io.keyko.nevermind.models.DDO;
import io.keyko.nevermind.models.asset.AssetMetadata;
import io.keyko.nevermind.models.service.ProviderConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PermissionsIT {

    private static final Logger log = LogManager.getLogger(PermissionsIT.class);

    private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;
    private static AssetMetadata metadataBase;

    private static ProviderConfig providerConfig;
    private static NevermindAPI nevermindAPI;
    private static NevermindAPI nevermindAPIConsumer;

    private static Config config;

    @BeforeClass
    public static void setUp() throws Exception {

        config = ConfigFactory.load();

        METADATA_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
        metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_JSON_CONTENT);

        String metadataUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/ddo/{did}";
        String provenanceUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/provenance/{did}";
        String consumeUrl = config.getString("gateway.url") + "/api/v1/gateway/services/consume";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);

        nevermindAPI = NevermindAPI.getInstance(config);

        assertNotNull(nevermindAPI.getAssetsAPI());
        assertNotNull(nevermindAPI.getMainAccount());

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
        properties.put(NevermindConfig.PROVIDER_ADDRESS, config.getString("provider.address"));

        properties.put(NevermindConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.ComputeExecutionCondition.address"));
        properties.put(NevermindConfig.ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.EscrowComputeExecutionTemplate.address"));

        nevermindAPIConsumer = NevermindAPI.getInstance(properties);

        nevermindAPIConsumer.getTokensAPI().request(BigInteger.TEN);
        Balance balance = nevermindAPIConsumer.getAccountsAPI().balance(nevermindAPIConsumer.getMainAccount());
        log.debug("Account " + nevermindAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

    }

    @Test
    public void checkPermissions() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();

        DDO ddo = nevermindAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String ownerAddress = nevermindAPI.getAssetsAPI().owner(ddo.getDid());
        assertEquals(nevermindAPI.getMainAccount().address, ownerAddress);

        Boolean consumerPermission = nevermindAPI.getAssetsAPI().getPermissions(ddo.getDid(), nevermindAPIConsumer.getMainAccount().address);
        assertEquals(false, consumerPermission);

        nevermindAPI.getAssetsAPI().delegatePermissions(ddo.getDid(), nevermindAPIConsumer.getMainAccount().address);
        consumerPermission = nevermindAPI.getAssetsAPI().getPermissions(ddo.getDid(), nevermindAPIConsumer.getMainAccount().address);
        assertEquals(true, consumerPermission);

        nevermindAPI.getAssetsAPI().revokePermissions(ddo.getDid(), nevermindAPIConsumer.getMainAccount().address);
        consumerPermission = nevermindAPI.getAssetsAPI().getPermissions(ddo.getDid(), nevermindAPIConsumer.getMainAccount().address);
        assertEquals(false, consumerPermission);

    }

    @Test
    @Ignore
    public void transferOwnership() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();

        DDO ddo = nevermindAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String ownerAddress = nevermindAPI.getAssetsAPI().owner(ddo.getDid());
        assertEquals(nevermindAPI.getMainAccount().address, ownerAddress);

        Boolean consumerPermission = nevermindAPI.getAssetsAPI().getPermissions(ddo.getDid(), nevermindAPIConsumer.getMainAccount().address);
        assertEquals(false, consumerPermission);

        nevermindAPI.getAssetsAPI().transferOwnership(ddo.getDid(), nevermindAPIConsumer.getMainAccount().address);
        ownerAddress = nevermindAPI.getAssetsAPI().owner(ddo.getDid());
        assertEquals(nevermindAPIConsumer.getMainAccount().address, ownerAddress);

        nevermindAPIConsumer.getAssetsAPI().transferOwnership(ddo.getDid(), nevermindAPI.getMainAccount().address);
        ownerAddress = nevermindAPIConsumer.getAssetsAPI().owner(ddo.getDid());
        assertEquals(nevermindAPI.getMainAccount().address, ownerAddress);


    }


}
