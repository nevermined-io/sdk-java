package com.oceanprotocol.squid.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.squid.api.config.OceanConfig;
import com.oceanprotocol.squid.models.Balance;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.service.ProviderConfig;
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
    private static OceanAPI oceanAPI;
    private static OceanAPI oceanAPIConsumer;

    private static Config config;

    @BeforeClass
    public static void setUp() throws Exception {

        config = ConfigFactory.load();

        METADATA_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
        metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, METADATA_JSON_CONTENT);

        String metadataUrl = config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/ddo/{did}";
        String provenanceUrl = config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/provenance/{did}";
        String consumeUrl = config.getString("brizo.url") + "/api/v1/brizo/services/consume";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);

        oceanAPI = OceanAPI.getInstance(config);

        assertNotNull(oceanAPI.getAssetsAPI());
        assertNotNull(oceanAPI.getMainAccount());

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

        properties.put(OceanConfig.COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.ComputeExecutionCondition.address"));
        properties.put(OceanConfig.ESCROW_COMPUTE_EXECUTION_CONDITION_ADDRESS, config.getString("contract.EscrowComputeExecutionTemplate.address"));

        oceanAPIConsumer = OceanAPI.getInstance(properties);

        oceanAPIConsumer.getTokensAPI().request(BigInteger.TEN);
        Balance balance = oceanAPIConsumer.getAccountsAPI().balance(oceanAPIConsumer.getMainAccount());
        log.debug("Account " + oceanAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

    }

    @Test
    public void checkPermissions() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();

        DDO ddo = oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String ownerAddress = oceanAPI.getAssetsAPI().owner(ddo.getDid());
        assertEquals(oceanAPI.getMainAccount().address, ownerAddress);

        Boolean consumerPermission = oceanAPI.getAssetsAPI().getPermissions(ddo.getDid(), oceanAPIConsumer.getMainAccount().address);
        assertEquals(false, consumerPermission);

        oceanAPI.getAssetsAPI().delegatePermissions(ddo.getDid(), oceanAPIConsumer.getMainAccount().address);
        consumerPermission = oceanAPI.getAssetsAPI().getPermissions(ddo.getDid(), oceanAPIConsumer.getMainAccount().address);
        assertEquals(true, consumerPermission);

        oceanAPI.getAssetsAPI().revokePermissions(ddo.getDid(), oceanAPIConsumer.getMainAccount().address);
        consumerPermission = oceanAPI.getAssetsAPI().getPermissions(ddo.getDid(), oceanAPIConsumer.getMainAccount().address);
        assertEquals(false, consumerPermission);

    }

    @Test
    @Ignore
    public void transferOwnership() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();

        DDO ddo = oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String ownerAddress = oceanAPI.getAssetsAPI().owner(ddo.getDid());
        assertEquals(oceanAPI.getMainAccount().address, ownerAddress);

        Boolean consumerPermission = oceanAPI.getAssetsAPI().getPermissions(ddo.getDid(), oceanAPIConsumer.getMainAccount().address);
        assertEquals(false, consumerPermission);

        oceanAPI.getAssetsAPI().transferOwnership(ddo.getDid(), oceanAPIConsumer.getMainAccount().address);
        ownerAddress = oceanAPI.getAssetsAPI().owner(ddo.getDid());
        assertEquals(oceanAPIConsumer.getMainAccount().address, ownerAddress);

        oceanAPIConsumer.getAssetsAPI().transferOwnership(ddo.getDid(), oceanAPI.getMainAccount().address);
        ownerAddress = oceanAPIConsumer.getAssetsAPI().owner(ddo.getDid());
        assertEquals(oceanAPI.getMainAccount().address, ownerAddress);


    }


}
