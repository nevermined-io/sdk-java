package com.oceanprotocol.squid.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.squid.core.sla.handlers.ServiceAgreementHandler;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.service.ProviderConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AgreementsApiIT {

    private static OceanAPI oceanAPI;
    private static final String DDO_JSON_SAMPLE = "https://raw.githubusercontent.com/oceanprotocol/OEPs/master/8/v0.4/ddo-example-access.json";
    private static String DDO_JSON_CONTENT;
    //private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;
    private static AssetMetadata metadataBase;
    private static ProviderConfig providerConfig;


    @BeforeClass
    public static void setUp() throws Exception {


        Config config = ConfigFactory.load();
        DDO_JSON_CONTENT=  IOUtils.toString(new URI(DDO_JSON_SAMPLE), "utf-8");
        DDO fullDDO = DDO.fromJSON(new TypeReference<DDO>() {
        }, DDO_JSON_CONTENT);


        METADATA_JSON_CONTENT = fullDDO.services.get(0).toJson();
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

    }

    @Test
    public void create() throws Exception {
        DDO ddo = oceanAPI.getAssetsAPI().create(metadataBase, providerConfig);
        String agreementId = ServiceAgreementHandler.generateSlaId();
        assertTrue(oceanAPI.getAgreementsAPI().create(ddo.getDid(), agreementId, 3, oceanAPI.getMainAccount().address));
        oceanAPI.getAgreementsAPI().status(agreementId);
    }
}
