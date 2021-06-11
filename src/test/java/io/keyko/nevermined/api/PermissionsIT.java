package io.keyko.nevermined.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.nevermined.api.config.NeverminedConfig;
import io.keyko.nevermined.manager.ManagerHelper;
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
        String gatewayUrl = config.getString("gateway.url");
        String consumeUrl = gatewayUrl + "/api/v1/gateway/services/access";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, gatewayUrl, provenanceUrl, secretStoreEndpoint, providerAddress);
        neverminedAPI = NeverminedAPI.getInstance(config);
        neverminedAPIConsumer = ManagerHelper.getNeverminedAPI(config, ManagerHelper.VmClient.parity, "2");

        neverminedAPIConsumer.getTokensAPI().request(BigInteger.TEN);
        Balance balance = neverminedAPIConsumer.getAccountsAPI().balance(neverminedAPIConsumer.getMainAccount());
        log.debug("Account " + neverminedAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

    }

    @Test
    public void checkPermissions() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();

        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String ownerAddress = neverminedAPI.getAssetsAPI().owner(ddo.getDID());
        assertEquals(neverminedAPI.getMainAccount().address, ownerAddress);

        Boolean consumerPermission = neverminedAPI.getAssetsAPI().getPermissions(ddo.getDID(), neverminedAPIConsumer.getMainAccount().address);
        assertEquals(false, consumerPermission);

        neverminedAPI.getAssetsAPI().delegatePermissions(ddo.getDID(), neverminedAPIConsumer.getMainAccount().address);
        consumerPermission = neverminedAPI.getAssetsAPI().getPermissions(ddo.getDID(), neverminedAPIConsumer.getMainAccount().address);
        assertEquals(true, consumerPermission);

        neverminedAPI.getAssetsAPI().revokePermissions(ddo.getDID(), neverminedAPIConsumer.getMainAccount().address);
        consumerPermission = neverminedAPI.getAssetsAPI().getPermissions(ddo.getDID(), neverminedAPIConsumer.getMainAccount().address);
        assertEquals(false, consumerPermission);

    }

    @Test
    @Ignore
    public void transferOwnership() throws Exception {

        metadataBase.attributes.main.dateCreated = new Date();

        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String ownerAddress = neverminedAPI.getAssetsAPI().owner(ddo.getDID());
        assertEquals(neverminedAPI.getMainAccount().address, ownerAddress);

        Boolean consumerPermission = neverminedAPI.getAssetsAPI().getPermissions(ddo.getDID(), neverminedAPIConsumer.getMainAccount().address);
        assertEquals(false, consumerPermission);

        neverminedAPI.getAssetsAPI().transferOwnership(ddo.getDID(), neverminedAPIConsumer.getMainAccount().address);
        ownerAddress = neverminedAPI.getAssetsAPI().owner(ddo.getDID());
        assertEquals(neverminedAPIConsumer.getMainAccount().address, ownerAddress);

        neverminedAPIConsumer.getAssetsAPI().transferOwnership(ddo.getDID(), neverminedAPI.getMainAccount().address);
        ownerAddress = neverminedAPIConsumer.getAssetsAPI().owner(ddo.getDID());
        assertEquals(neverminedAPI.getMainAccount().address, ownerAddress);


    }


}
