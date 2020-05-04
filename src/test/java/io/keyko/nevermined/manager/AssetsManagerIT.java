package io.keyko.nevermined.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class AssetsManagerIT {

    private static final Logger log = LogManager.getLogger(AssetsManagerIT.class);

    private static AssetsManager manager;
    private static KeeperService keeper;
    private static MetadataApiService metadataApiService;

    private static final String DDO_JSON_SAMPLE = "src/test/resources/examples/ddo-example.json";
    private static String DDO_JSON_CONTENT;

    private static final Config config = ConfigFactory.load();


    @BeforeClass
    public static void setUp() throws Exception {
        log.debug("Setting Up DTO's");

        keeper = ManagerHelper.getKeeper(config);
        metadataApiService = ManagerHelper.getMetadataService(config);
        manager = AssetsManager.getInstance(keeper, metadataApiService);

        SecretStoreManager secretStore= ManagerHelper.getSecretStoreController(config, ManagerHelper.VmClient.parity);
        manager.setSecretStoreManager(secretStore);

        DDO_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(DDO_JSON_SAMPLE)));

    }


    // TODO Check if this test is ok
    @Test
    public void searchAssets() throws Exception {

        DDO ddo1= DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);
        DDO ddo2= DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);
        DDO ddo3= DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);
        DDO ddo4= DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);
        DDO ddo5= DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);

        DID did1 = ddo1.generateDID();
        DID did2 = ddo2.generateDID();
        DID did3 = ddo3.generateDID();
        DID did4 = ddo4.generateDID();
        DID did5 = ddo5.generateDID();

        ddo1.id = did1.toString();
        ddo2.id = did2.toString();
        ddo3.id = did3.toString();
        ddo4.id = did4.toString();
        ddo5.id = did5.toString();

        String randomParam= UUID.randomUUID().toString().replaceAll("-","");
        log.debug("Using random param for search: " + randomParam);

        ddo1.getMetadataService().attributes.main.type= randomParam;
        ddo2.getMetadataService().attributes.main.type= randomParam;
        ddo1.getMetadataService().attributes.main.name = "random name";

        metadataApiService.createDDO(ddo1);
        metadataApiService.createDDO(ddo2);
        metadataApiService.createDDO(ddo3);
        metadataApiService.createDDO(ddo4);
        metadataApiService.createDDO(ddo5);

        List<DDO> result1= manager.searchAssets(randomParam, 10, 1).getResults();

        assertEquals(2, result1.size());
        Assert.assertEquals(randomParam,result1.get(0).getMetadataService().attributes.main.type);

    }

}