package io.keyko.nevermind.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import io.keyko.common.web3.KeeperService;
import com.oceanprotocol.secretstore.core.EvmDto;
import io.keyko.nevermind.exceptions.DDOException;
import io.keyko.nevermind.external.MetadataService;
import io.keyko.nevermind.models.DDO;
import io.keyko.nevermind.models.asset.AssetMetadata;
import io.keyko.nevermind.models.service.Service;
import io.keyko.nevermind.models.service.types.AuthorizationService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;

public class BaseManagerTest {

    private static final Logger log = LogManager.getLogger(BaseManagerTest.class);

    private static final Config config = ConfigFactory.load();

    private static final String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;

    private static io.keyko.nevermind.models.service.types.MetadataService metadataService;
    private static AssetMetadata assetMetadata;

    private static KeeperService keeperService;

    private static MetadataService metadata;
    private static SecretStoreManager secretStore;

    private static BaseManagerImplementation baseManager;

    private static final String SERVICE_AGREEMENT_ADDRESS;
    static {
        SERVICE_AGREEMENT_ADDRESS = config.getString("contract.AgreementStoreManager.address");
    }

    public static class BaseManagerImplementation extends BaseManager {


        public BaseManagerImplementation(KeeperService keeperService, MetadataService metadataService) throws IOException, CipherException {
            super(keeperService, metadataService);
        }

    }


    @BeforeClass
    public static void setUp() throws Exception {

        log.debug("Setting Up...");

        keeperService = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "");
        metadata = ManagerHelper.getMetadataService(config);

        EvmDto evmDto = ManagerHelper.getEvmDto(config, ManagerHelper.VmClient.parity);
        secretStore= ManagerHelper.getSecretStoreController(config, evmDto);

        baseManager = new BaseManagerImplementation(keeperService, metadata);
        baseManager.setSecretStoreManager(secretStore)
        .setEvmDto(evmDto);

        METADATA_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
        assetMetadata = DDO.fromJSON(new TypeReference<AssetMetadata>() {}, METADATA_JSON_CONTENT);
        metadataService = new io.keyko.nevermind.models.service.types.MetadataService(assetMetadata, "http://localhost:5000/api/v1/metadata/assets/ddo/{did}");

    }

    @Test
    public void buildDDO() throws DDOException {

        DDO ddo = baseManager.buildDDO(metadataService, null, SERVICE_AGREEMENT_ADDRESS);
        assertNotNull(ddo.proof);

    }

    @Test
    public void buildDDOWithAuthorizationService() throws Exception {

        String serviceEndpoint =  config.getString("secretstore.url");
        AuthorizationService authorizationService = new AuthorizationService(serviceEndpoint, Service.DEFAULT_AUTHORIZATION_INDEX);

        DDO ddo = baseManager.buildDDO(metadataService, authorizationService, SERVICE_AGREEMENT_ADDRESS);
        assertNotNull(ddo.proof);

    }


}
