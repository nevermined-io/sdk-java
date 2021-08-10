package io.keyko.nevermined.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.keyko.common.web3.KeeperService;
import io.keyko.nevermined.contracts.DIDRegistry;
import io.keyko.nevermined.exceptions.NFTException;
import io.keyko.nevermined.external.MetadataApiService;
import io.keyko.nevermined.models.Account;
import io.keyko.nevermined.models.DDO;
import io.keyko.nevermined.models.DID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AssetsManagerIT {

    private static final Logger log = LogManager.getLogger(AssetsManagerIT.class);

    private static AssetsManager manager;
    private static KeeperService keeper;
    private static MetadataApiService metadataApiService;
    private static NeverminedManager neverminedManager;

    private static final String DDO_JSON_SAMPLE = "src/test/resources/examples/ddo-example.json";
    private static String DDO_JSON_CONTENT;

    private static final Config config = ConfigFactory.load();


    @BeforeClass
    public static void setUp() throws Exception {
        log.debug("Setting Up DTO's");

        Account mainAccount = new Account(
                config.getString("account.main.address"),
                config.getString("account.main.password"));


        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.main);
        metadataApiService = ManagerHelper.getMetadataService(config);
        DIDRegistry didRegistry = ManagerHelper.loadDIDRegistryContract(
                keeper, config.getString("contract.DIDRegistry.address"));

        manager = AssetsManager.getInstance(keeper, metadataApiService);
        manager.setMainAccount(mainAccount);
        manager.setDidRegistryContract(didRegistry);

        SecretStoreManager secretStore= ManagerHelper.getSecretStoreController(config, ManagerHelper.VmClient.parity);
        manager.setSecretStoreManager(secretStore);

        neverminedManager = NeverminedManager.getInstance(keeper, metadataApiService);
        neverminedManager.setMainAccount(mainAccount);
        neverminedManager.setDidRegistryContract(didRegistry);

        DDO_JSON_CONTENT = new String(Files.readAllBytes(Paths.get(DDO_JSON_SAMPLE)));

    }


    @Test
    public void mintAndBurn() throws NFTException, Exception {
        String myAddress = keeper.getAddress();
        String someoneAddress = "0x00a329c0648769A73afAc7F9381E08FB43dBEA72";

        DID seed= DID.builder();
        DID did = neverminedManager.hashDID(seed.getHash(), neverminedManager.getMainAccount().getAddress());
        String checksum = "0xd190bc85ee50643baffe7afe84ec6a9dd5212b67223523cd8e4d88f9069255fb";

        DDO ddoBase = DDO.fromJSON(new TypeReference<DDO>() {}, DDO_JSON_CONTENT);
        ddoBase.id = did.toString();
        String newUrl= config.getString("metadata-internal.url") + "/api/v1/metadata/assets/ddo/" + did.toString();

        ddoBase.services.get(0).serviceEndpoint = newUrl;
        metadataApiService.createDDO(ddoBase);

        boolean didRegistered= neverminedManager.registerMintableDID(
                seed.getHash(), newUrl, checksum, Arrays.asList(), BigInteger.valueOf(100), BigInteger.ZERO);
        assertTrue(didRegistered);

        assertEquals(BigInteger.ZERO, manager.balance(myAddress, did));
        assertTrue(manager.mint(did, BigInteger.TEN));
        assertEquals(BigInteger.TEN, manager.balance(myAddress, did));
        assertTrue(manager.burn(did, BigInteger.ONE));
        assertEquals(BigInteger.valueOf(9), manager.balance(myAddress, did));
        assertTrue(manager.transfer(did, someoneAddress, BigInteger.ONE));
        assertEquals(BigInteger.valueOf(8), manager.balance(myAddress, did));
        assertEquals(BigInteger.ONE, manager.balance(someoneAddress, did));
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