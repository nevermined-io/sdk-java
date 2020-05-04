package io.keyko.nevermined.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.AssetMetadata;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SecretStoreApiIT {


    private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;
    private static AssetMetadata metadataBase;

    private static NeverminedAPI neverminedAPIPublisher;
    private static NeverminedAPI neverminedAPIConsumer;

    // This test only made sense when Secret Store `acl_contract` is set to "none"
    // If not the complete encryption/decryption is tested in the purchase/order

    @BeforeClass
    @Ignore
    public static void setUp() throws Exception {

        METADATA_JSON_CONTENT =  new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
        metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {}, METADATA_JSON_CONTENT);

        Config config = ConfigFactory.load();
        neverminedAPIPublisher = NeverminedAPI.getInstance(config);

        String consumerAddress= config.getString("account.parity.address2");
        String consumerPassword= config.getString("account.parity.password2");
        String consumerFile= config.getString("account.parity.file2");

        Config consumerConfig= config.withValue("account.main.address",
                ConfigValueFactory.fromAnyRef(consumerAddress))
                .withValue("account.main.password", ConfigValueFactory.fromAnyRef(consumerPassword))
                .withValue("account.main.credentialsFile", ConfigValueFactory.fromAnyRef(consumerFile));

        neverminedAPIConsumer = NeverminedAPI.getInstance(consumerConfig);

        assertNotNull(neverminedAPIPublisher.getSecretStoreAPI());
        assertNotNull(neverminedAPIPublisher.getMainAccount());

    }

    @Test
    @Ignore
    public void encrypt() throws Exception{

        String filesJson = metadataBase.toJson(metadataBase.attributes.main.files);
        String did = DID.builder().getHash();

        String encryptedDocument = neverminedAPIPublisher.getSecretStoreAPI().encrypt(did, filesJson, 0);

        String decryptedDocument = neverminedAPIConsumer.getSecretStoreAPI().decrypt(did, encryptedDocument);

        assertEquals(filesJson, decryptedDocument);

    }
}
