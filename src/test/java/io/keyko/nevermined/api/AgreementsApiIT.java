package io.keyko.nevermined.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.nevermined.core.sla.handlers.ServiceAgreementHandler;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.service.ProviderConfig;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AgreementsApiIT {

    private static NeverminedAPI neverminedAPI;
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

        String metadataUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/ddo/{did}";
        String provenanceUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/provenance/{did}";
        String consumeUrl = config.getString("gateway.url") + "/api/v1/gateway/services/consume";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, provenanceUrl, secretStoreEndpoint, providerAddress);
        neverminedAPI = NeverminedAPI.getInstance(config);

        assertNotNull(neverminedAPI.getAssetsAPI());
        assertNotNull(neverminedAPI.getMainAccount());

    }

    @Test
    public void create() throws Exception {
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String agreementId = ServiceAgreementHandler.generateSlaId();
        assertTrue(neverminedAPI.getAgreementsAPI().create(ddo.getDid(), agreementId, 3, neverminedAPI.getMainAccount().address));
        neverminedAPI.getAgreementsAPI().status(agreementId);
    }
}
