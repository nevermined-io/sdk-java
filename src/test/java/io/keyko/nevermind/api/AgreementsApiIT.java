package io.keyko.nevermind.api;

import com.fasterxml.jackson.core.type.TypeReference;
import io.keyko.nevermind.core.sla.handlers.ServiceAgreementHandler;
import io.keyko.nevermind.models.DDO;
import io.keyko.nevermind.models.asset.AssetMetadata;
import io.keyko.nevermind.models.service.ProviderConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AgreementsApiIT {

    private static OceanAPI oceanAPI;
    private static AssetMetadata metadataBase;
    private static DDO ddoBase;
    private static ProviderConfig providerConfig;

    private static final String DDO_JSON_SAMPLE = "src/test/resources/examples/ddo-example.json";
    private static String DDO_JSON_CONTENT;
    private static final String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;



    @BeforeClass
    public static void setUp() throws Exception {


        Config config = ConfigFactory.load();

        // Pre-parsing of json's and models
        DDO_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(DDO_JSON_SAMPLE)));
        ddoBase = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);

        METADATA_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
        metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {}, METADATA_JSON_CONTENT);

        String metadataUrl = config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/ddo/{did}";
        String provenanceUrl = config.getString("aquarius-internal.url") + "/api/v1/aquarius/assets/provenance/{did}";
        String consumeUrl = config.getString("brizo.url") + "/api/v1/brizo/services/consume";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);
        oceanAPI = OceanAPI.getInstance(config);

        assertNotNull(oceanAPI.getAssetsAPI());
        assertNotNull(oceanAPI.getMainAccount());

    }

    @Test
    public void create() throws Exception {
        DDO ddo = oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String agreementId = ServiceAgreementHandler.generateSlaId();
        assertTrue(oceanAPI.getAgreementsAPI().create(ddo.getDid(), agreementId, 3, oceanAPI.getMainAccount().address));
        oceanAPI.getAgreementsAPI().status(agreementId);
    }
}
