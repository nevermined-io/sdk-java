package io.keyko.nevermined.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import io.keyko.nevermined.models.asset.AssetMetadata;
import io.keyko.nevermined.models.service.ProviderConfig;
import io.keyko.nevermined.models.service.Service;


@Ignore
public class FilesApiIT {
    private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static NeverminedAPI neverminedAPI;
    private static ProviderConfig providerConfig;
    private static AssetMetadata metadataBase;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUp() throws Exception {
        Config config = ConfigFactory.load();

        String metadataUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/ddo/{did}";
        String provenanceUrl = config.getString("metadata-internal.url") + "/api/v1/metadata/assets/provenance/{did}";
        String gatewayUrl = config.getString("gateway.url");
        String consumeUrl = gatewayUrl + "/api/v1/gateway/services/access";
        String secretStoreEndpoint = config.getString("secretstore.url");
        String providerAddress = config.getString("provider.address");

        providerConfig = new ProviderConfig(consumeUrl, metadataUrl, gatewayUrl, provenanceUrl, secretStoreEndpoint, providerAddress);
        neverminedAPI = NeverminedAPI.getInstance(config);

        String metadataJsonContent = new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
        metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {
        }, metadataJsonContent);
    }

    @Test
    public void uploadFilecoin() throws Exception {
        File file = tempFolder.newFile();
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write("Hello, Nevermined!".getBytes());
        outputStream.close();

        String url = neverminedAPI.getFilesAPI().uploadFilecoin(file.getAbsolutePath(), providerConfig);
        assertEquals("cid://QmSJA3xNH62sj4xggZZzCp2VXpsXbkR9zYoqNYXp3c4xuN", url);
    }

    @Test
    public void creatAssetWithCid() throws Exception {
        // upload to filecoin
        File file = tempFolder.newFile();
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write("aaa".getBytes());
        outputStream.close();
        String url = neverminedAPI.getFilesAPI().uploadFilecoin(file.getAbsolutePath(), providerConfig);

        // Register asset
        metadataBase.attributes.main.dateCreated = new Date();
        metadataBase.attributes.main.files.get(0).url = url;
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);

        DID did = new DID(ddo.id);
        DDO resolvedDDO = neverminedAPI.getAssetsAPI().resolve(did);
        assertEquals(ddo.id, resolvedDDO.id);
    }

    @Test
    public void consumeAssetWithCid() throws Exception {
        // upload to filecoin
        File file = tempFolder.newFile();
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write("bbb".getBytes());
        outputStream.close();
        String url = neverminedAPI.getFilesAPI().uploadFilecoin(file.getAbsolutePath(), providerConfig);

        // Register asset
        metadataBase.attributes.main.dateCreated = new Date();
        metadataBase.attributes.main.files.get(0).url = url;
        DDO ddo = neverminedAPI.getAssetsAPI().create(metadataBase, providerConfig);

        // Download asset
        DID did = new DID(ddo.id);
        Boolean downloaded = neverminedAPI.getAssetsAPI().ownerDownload(
            did, Service.DEFAULT_ACCESS_INDEX, tempFolder.getRoot().getAbsolutePath());
        assertTrue(downloaded);

        String expectedFolderName = "datafile." + did.getHash() + ".0";
        Path expectedDestinationPath = Paths.get(tempFolder.getRoot().getAbsolutePath(), expectedFolderName);
        File downloadedFolder = expectedDestinationPath.toFile();
        assertEquals(1, downloadedFolder.listFiles().length);

        File downloadedFile = downloadedFolder.listFiles()[0];
        String downloadedFileContent = FileUtils.readFileToString(downloadedFile, "UTF-8");
        assertEquals("bbb", downloadedFileContent);
    }
}
